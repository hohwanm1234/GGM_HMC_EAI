/*
 * Copyright Hyundai AutoEver.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Hyundai AutoEver. ("Confidential Information").
 */
package com.xconnect.eai.external.component; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.xconnect.eai.server.common.util.CamelHelper;
import com.xconnect.eai.server.common.util.CommonUtils;


public class FTPDataToJDBC {
	private boolean verbose	=	true;
	private static final Logger LOG = LoggerFactory.getLogger(FTPDataToJDBC.class);
	
	private DataSource dataSrc;
	private DataSourceTransactionManager dataSourceTransactionManager;
	private DefaultTransactionDefinition paramTransactionDefinition = new DefaultTransactionDefinition();
	private TransactionStatus status;
	private NamedParameterJdbcTemplate npjt;
	
	public void setDataSource(String dataSource) {
		dataSrc = CamelHelper.getInstance().getContext().getBean(dataSource + "_source", DataSource.class);
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSrc);
		
		dataSourceTransactionManager = new DataSourceTransactionManager();
		dataSourceTransactionManager.setDataSource(dataSrc);
		
		npjt = jdbcTemplate;
	}
	
	public static Map<String, Object> parseCsv(String contents, String[] column, String dataName, boolean isFirstHeader) throws Exception {
		try {
			final String token	=	"\t";
			String[] lines = contents.split("\\r?\\n");
			String[] columns = null;
			if(column != null && column.length > 0) columns = column;
			if(isFirstHeader && columns == null) {
				String header = lines[0];
				columns = header.trim().split(token);
			}
			
			List<HashMap<String, Object>> contentsMapList = new ArrayList<HashMap<String, Object>>();
			for(int i=0; i < lines.length; i++) {
				if(isFirstHeader && i == 0) continue;
				HashMap<String, Object> contentMap = new HashMap<>();
				String[] line = lines[i].trim().split(token);
				for(int l=0; l < columns.length; l++) {
					contentMap.put(columns[l], line[l]);
				}
				contentsMapList.add(contentMap);
			}
			
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(dataName, contentsMapList);
			
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	

	public void process(Object objectExchange, Map dummy) {
		try {
			if(verbose) LOG.debug("[FTPDataToJDBC]----------------------------------------------------start");
			
			// 0 data get
			Exchange exchange	=	(Exchange) objectExchange;
			String text			=	exchange.getIn().getBody(String.class);

			String strDatasource =  CommonUtils.getProperty(exchange, "DataSource");
			String strTableName =  CommonUtils.getProperty(exchange, "TableName");
			setDataSource(strDatasource);
			
			String fieldStrings[]	=	null;
			if(exchange.getIn().getHeaders()!=null) {
				String fieldString = "";
				fieldString = exchange.getIn().getHeader("fieldString", String.class);
				fieldStrings	=	fieldString!=null?fieldString.split(","):null;
			}
			
			boolean  isFirstLine = exchange.getIn().getHeader("isFirstLine", boolean.class);
			
			Map<String, Object> contentList = null;			
			contentList = parseCsv(text, fieldStrings, "list", isFirstLine);
			
			if(contentList!=null)
				exchange.getIn().setBody(contentList.get("list"));
			if(contentList.get("list") == null ) return ;

			// data insert
			List<Map> dataList	=	(List<Map>) contentList.get("list");
			int[] affected = null;
			boolean insert_fail = false;
			String insert_fail_msg = "";
			status = dataSourceTransactionManager.getTransaction(paramTransactionDefinition);

			try {
				boolean bDeldat	= true;
				String tableName = "dbo.ZHHRSIEMENSCARD"; 
				if(!strTableName.contentEquals("")) tableName = strTableName;
				
				String[] columnKeyArry 	  = { "pt_id", "Point_name","Date_occurred","Time_occurred"};
				String[] columnInsertArry = { "pt_id", "Point_name","Date_occurred","Time_occurred","Message","Card_no","Emp_id","point_no"};
	                
				String deleteQuery	=	"DELETE FROM "+tableName +" WHERE 1=1 ";
				String insertQuery	=	"INSERT INTO "+tableName +" ( "+" pt_id	,Point_name,Date_occurred,Time_occurred,Message,Card_no,Emp_id, flag "+" ) "+
										" Values ( :pt_id , :point_name, :date_occurred, :time_occurred, :message,  :card_no, :emp_id, :point_no )";
				
				List<Map<String, Object>> deleteDataMapList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> insertDataMapList = new ArrayList<Map<String, Object>>();
				
				// make delete query
				String updateConditionDetail	=	"";
				for(String keyField:columnKeyArry) {
					if(!keyField.isEmpty()) {
						updateConditionDetail = updateConditionDetail + " AND "+keyField+" = :"+keyField.toLowerCase();
					}
				}
				
				deleteQuery	=	deleteQuery + updateConditionDetail; 
				
				// insert 중복체크
				setFirst(dataList,dataList);
				
				String inGate	=	"ＩＮ";
				String outGate	=	"ＯＵＴ";
//				String inGate	=	"6-1-3(S/P-1)－ＩＮ";
//				String outGate	=	"6-1-3(S/P-1)－ＯＵＴ";
				
				

				for(Map delCondition : dataList) {
					if(delCondition.get("pt_id") == null || delCondition.get("pt_id").equals("") ) continue;					

					if(delCondition.get("card_no") == null || delCondition.get("card_no").equals(""))
						continue;

					if(delCondition.get("emp_id") == null || delCondition.get("emp_id").equals("") || delCondition.get("emp_id").equals("0"))
						continue;
					
					String pointName	=	(String) delCondition.get("point_name");
					if(pointName.contentEquals("point_name")) continue;
					
					if(!pointName.contains(inGate) && !pointName.contains(outGate)) {
						continue;
					}
					
					Map keyFieldDataSet		=	new HashMap();
					Map insertFieldDataSet	=	new HashMap();
					for(String keyField:columnKeyArry) {
						keyFieldDataSet.put(keyField.toLowerCase(), delCondition.get(keyField.toLowerCase()));
					}
					
					
					String skipFlag	= "";
					if(delCondition.get("skip")!=null)
						skipFlag	=	delCondition.get("skip").toString();
					
					if(!skipFlag.equals("S"))
					for(String inField:columnInsertArry) {
						insertFieldDataSet.put(inField.toLowerCase(), delCondition.get(inField.toLowerCase()));
					}					
					
					if(deleteDataMapList.size() > 10) break;
					deleteDataMapList.add(keyFieldDataSet);
					

					//if(!skipFlag.equals("S"))
						insertDataMapList.add(insertFieldDataSet);
				}
				

				exchange.setProperty("total", dataList.size());
				exchange.setProperty("processTotal", insertDataMapList.size());
				//LOG.debug("+-[ (Target) DB Batch delete ]---------------------------------");
				//affected = npjt.batchUpdate(deleteQuery, SqlParameterSourceUtils.createBatch(deleteDataMapList.toArray(new Map[0])));
				//LOG.debug("+-[ (Target) DB Batch delete ]---------------------------------");
				
				LOG.debug("+-[ (Target) DB Batch Insert ]---------------------------------");
				LOG.debug("+-[ (Target) DB Batch Count  ]------["+insertDataMapList.size()+"]----");
				
				affected = npjt.batchUpdate(insertQuery, SqlParameterSourceUtils.createBatch(insertDataMapList.toArray(new Map[0])));
				LOG.debug("+-[ (Target) DB Batch Insert ]---------------------------------");
				
				dataSourceTransactionManager.commit(status);
				// commit
			} catch (Exception e) {
				dataSourceTransactionManager.rollback(status);
				LOG.debug("+-[ Exception ]---------------------------------["+e.getLocalizedMessage()+"]");
			}			

		} catch (Exception e) {
			LOG.error("[StandardInterfaceMessageParser] >> soapMessageParser :: Error > " + CommonUtils.getPrintStackTrace(e));
			throw new RuntimeException("Soap Message가 없습니다.");
		}
	}
	
	

	
	public void setFirst(List<Map> dataList,List<Map> dataList2) {
		for(Map record : dataList2) {
			for(Map delCondition : dataList) {
				String[] columnKeyArry = { "pt_id", "Point_name","Date_occurred","Time_occurred"};

				String strInkey1	=	(String) record.get("pt_id");
				String strInkey2	=	(String) record.get("point_name");
				String strInkey3	=	(String) record.get("date_occurred");
				String strInkey4	=	(String) record.get("time_occurred");
				String inValue		=	record.get("message")+""+record.get("card_no")+record.get("emp_id");


				String strkey1	=	(String) delCondition.get("pt_id");
				String strkey2	=	(String) delCondition.get("point_name");
				String strkey3	=	(String) delCondition.get("date_occurred");
				String strkey4	=	(String) delCondition.get("time_occurred");
				String compareValue	=	delCondition.get("message")+""+delCondition.get("card_no")+delCondition.get("emp_id");

				String skipFlag	= "";
				if(delCondition.get("skip")!=null)
					skipFlag	=	delCondition.get("skip").toString();
				
				String skipFlag2	= "";
				if(record.get("skip")!=null)
					skipFlag2	=	record.get("skip").toString();				
				
				if(skipFlag2.contentEquals(""))
					record.put("skip", "O");
				
				if(strInkey1.equals(strkey1)
						&& strInkey2.equals(strkey2)
						&& strInkey3.equals(strkey3)
						&& strInkey4.equals(strkey4) && (delCondition != record) && skipFlag.equals("")) {
					delCondition.put("skip", "S");
				}
			}
		}
	}
	
}
