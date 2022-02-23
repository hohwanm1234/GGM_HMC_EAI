/*
 * Copyright Hyundai AutoEver.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Hyundai AutoEver. ("Confidential Information").
 */
package com.xconnect.eai.external.HmcEaiConnect; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;
import org.json.XML;
import org.json.XMLParserConfiguration;

import com.google.gson.Gson;
import com.xconnect.eai.server.common.constant.SystemCommonConstants;
import com.xconnect.eai.server.common.util.CommonUtils;
import com.xconnect.eai.server.common.vo.InterfaceMessage;
import com.xconnect.eai.server.common.vo.InterfaceResponseMessage;
import com.xconnect.eai.web.common.util.Gzip;


public class Sample {
	
	// TestCase 
	// 1) xml 받아서 canias 로 바꾸기
	// hmc-> canias callService
	public String process(Object objectExchange, Map dummy) {
		String returnMsg	=	"";
		String outZip	=	"";
		String outDatatype = "";		
		try {
			/*
			 * Interface ID 만 매핑
			 */
			boolean verbose	=	true;
			Exchange exchange	=	(Exchange) objectExchange;
			String soapBodyStr 	= 	exchange.getIn().getBody(String.class);
			
			if(verbose) System.out.println("-v2-----------------------------------------------------");
			if(verbose) System.out.println("-v2-----------------------------------------------------");
			if(verbose) System.out.println("-v2-----------------------------------------------------");
			if(verbose) System.out.println("-v2-----------------------------------------------------");
			if(verbose) System.out.println("-v2-----------------------------------------------------");
			if(verbose) System.out.println("-v2-----------------------------------------------------");
			System.out.println("inputvalue =[[[[[[ " + soapBodyStr);

			String soapAction 	= 	"";			
			
			if(verbose) System.out.println("-v2-----------------------------------------------------");
			if(verbose) System.out.println("soapAction = " + soapAction);
			if(verbose) System.out.println("soapBodyStr = " + soapBodyStr);
			if(verbose) System.out.println("------------------------------------------------------");

			
			if(soapBodyStr != null && !soapBodyStr.isEmpty()) {
				 soapBodyStr	=	StringEscapeUtils.escapeXml11(soapBodyStr);
				JSONObject jsonObj = XML.toJSONObject(soapBodyStr, XMLParserConfiguration.KEEP_STRINGS);
				String jsonString = jsonObj.toString();
				Map<String, Object> soapRawMap 		= new Gson().fromJson(jsonString, Map.class);
				String soap_env_prefix = "";
				for(Iterator<String> key_iter = soapRawMap.keySet().iterator(); key_iter.hasNext();) {
					String key = key_iter.next();
					if(key.endsWith("Envelope")) {
						soap_env_prefix = key.split(":")[0];
						break;
					}
				}

				List<String> tns_prefixList = new ArrayList<String>();
				for(Iterator<String> key_iter = ((Map)soapRawMap.get(soap_env_prefix + ":Envelope")).keySet().iterator(); key_iter.hasNext();) {
					String key = key_iter.next();
					if(key.startsWith("xmlns") && key.indexOf(":") > 0) {
						tns_prefixList.add( key.split(":")[1] );
					}
				}
				
				tns_prefixList.add( "tns" );
				// ns prefix 제거...
				for(String remove_prefix : tns_prefixList)
					jsonString = jsonString.replaceAll(remove_prefix + ":", "");

				soapRawMap = new Gson().fromJson(jsonString, Map.class);
				Map<String, Object> soapHeaderMap	= new HashMap<>();	
				//}//

				//			InterfaceResponseMessage out	=	 (InterfaceResponseMessage) exchange.getIn().getBody();
				//			Map<String, Object> soapBodyMap 		=	out.getRequest_body();
				Map<String, Object> soapBodyMap = (Map<String, Object>)((Map)soapRawMap.get("Envelope")).get("Body");

				String company	= (String) CommonUtils.getValueByDepthKey(soapBodyMap, "ToHKMCGDIH.COMPANY", "");
				String sender	= (String) CommonUtils.getValueByDepthKey(soapBodyMap, "ToHKMCGDIH.SENDER", "");
				String ifid	= (String) CommonUtils.getValueByDepthKey(soapBodyMap, "ToHKMCGDIH.IFID", "");
				String documenttype	= (String) CommonUtils.getValueByDepthKey(soapBodyMap, "ToHKMCGDIH.DOCUMENTTYPE", "");
				String target_system	= (String) CommonUtils.getValueByDepthKey(soapBodyMap, "ToHKMCGDIH.TARGET_SYSTEM", "");
				String zip	= (String) CommonUtils.getValueByDepthKey(soapBodyMap, "ToHKMCGDIH.ZIP", "");
				String datatype	= (String) CommonUtils.getValueByDepthKey(soapBodyMap, "ToHKMCGDIH.DATATYPE", "");

				outZip	=	zip;
				outDatatype = datatype;


				String data	=	"";
				try {
					if(CommonUtils.getValueByDepthKey(soapBodyMap, "ToHKMCGDIH.IN_DATA.InputData", "")!=null) {
						Object datag	=  CommonUtils.getValueByDepthKey(soapBodyMap, "ToHKMCGDIH.IN_DATA.InputData", "");
						if(datag instanceof Map) {
							data	=	CommonUtils.MapToXml((Map<String, Object>) datag);
						}else {
						
						data	=	(String) datag;
						}
					}
					String outData	=	base64ZipConversionXML(zip+datatype,data,zip,datatype);
					exchange.getIn().setBody(outData);
					exchange.getOut().setBody(outData);
					
				}catch(Exception ex) {
					returnMsg	=	data;
					throw new Exception("Message Type Check["+zip+","+datatype+"]");
					
				}

				//			return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n" + 
				//					"   <soapenv:Body>\r\n" + 
				//					"      <ns1:loginResponse soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:ns1=\"http://webservice.ias.com\">\r\n" + 
				//					"         <loginReturn href=\"#id0\"/>\r\n" + 
				//					"      </ns1:loginResponse>\r\n" + 
				//					"      <multiRef id=\"id0\" soapenc:root=\"0\" soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xsi:type=\"ns2:LoginResponse\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:ns2=\"http://10.182.62.10:8080\">\r\n" + 
				//					"         <SessionId xsi:type=\"xsd:string\"/>\r\n" + 
				//					"         <ErrorMessage xsi:type=\"xsd:string\">call action failed : Connection refused to host: 10.43.148.10; nested exception is:\r\n" + 
				//					"        java.net.ConnectException: Connection timed out: connect</ErrorMessage>\r\n" + 
				//					"         <ContactNum xsi:type=\"xsd:string\"/>\r\n" + 
				//					"         <EncryptionKey xsi:type=\"xsd:string\"/>\r\n" + 
				//					"         <Success xsi:type=\"xsd:boolean\">false</Success>\r\n" + 
				//					"         <SecurityKey xsi:type=\"xsd:string\"/>\r\n" + 
				//					"      </multiRef>\r\n" + 
				//					"   </soapenv:Body>\r\n" + 
				//					"</soapenv:Envelope>";
			}
		} catch (Exception e) {
			
			if(returnMsg.startsWith("<")) {
				//returnMsg	=	StringEscapeUtils.escapeHtml4(returnMsg);
			}
			
			String samplexml		
		       =	"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tns=\"http://10.182.62.15:7011/services/receive\">\r\n" + 
				"   <soapenv:Header/>\r\n" + 
				"   <soapenv:Body>\r\n" + 
				"      <tns:ToHKMCGDIHResponse>\r\n" + 
				"         <IFRESULT>E</IFRESULT>\r\n" + 
				"         <ZIP>"+outZip+"</ZIP>\r\n" + 
				"         <DATATYPE>"+outDatatype+"</DATATYPE>\r\n" + 
				"         <IFMESSAGE>"+e.getMessage()+"</IFMESSAGE>\r\n" + 
				"         <OUT_DATA>\r\n" + 
				"            <outData>" +returnMsg+"</outData>\r\n" + 
				"         </OUT_DATA>\r\n" + 
				"      </tns:ToHKMCGDIHResponse>\r\n" + 
				"   </soapenv:Body>\r\n" + 
				"</soapenv:Envelope>";				
			Exchange exchange	=	(Exchange) objectExchange;
			exchange.getIn().setBody(samplexml);
			exchange.getOut().setBody(samplexml);
			
		
			e.printStackTrace(System.out);
			exchange.setException(null);
			return samplexml;
		}
		return "";
	}	
	
	
	
	public static String base64ZipConversionXML(String flag, Object src,String ZIP,String DATATYPE) throws Exception {
		String receiveData	=	"";
		try {
			boolean bzip	=	false;
			receiveData =	(String)src;
			if(flag.startsWith("ZIP")) {
				bzip	=	true;
				// uncompresss
				String inDatastr	=	(String)src;
				receiveData			=	Gzip.decompress64(inDatastr);
			}
			
			if(flag.endsWith("JSON")) {
				receiveData			=	CommonUtils.JsonToXml(receiveData);
			}else if(flag.endsWith("XML")) {
				//receiveData	= StringEscapeUtils.unescapeHtml4(receiveData);
			}
			
			JSONObject jsonObj = org.json.XML.toJSONObject(receiveData, XMLParserConfiguration.KEEP_STRINGS);
			Map resultMap = new Gson().fromJson(jsonObj.toString(), Map.class);			

			if(!receiveData.isEmpty() && jsonObj.isEmpty()) throw new Exception("Type check error");
			
			
			Object data	=	CommonUtils.getValueByDepthKey(resultMap, "ROOT.inputs", "");
			Object outData	=	null;
			if(data instanceof List) {
				outData	=	listOutChange((List<Map>) data);
			}else if(data instanceof Map) {
				outData	=	mapOutChange((Map) data);
			}else {
				outData	=	data;
			}
			CommonUtils.setValueAtDepthKey(resultMap, "ROOT.inputs",outData, "");
			
			String xmldata	=	CommonUtils.MapToXml((Map<String, Object>) resultMap);
			String srcString	=	base64ZipConversionZip(flag,xmldata);
		
			String samplexml		
			       =	"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tns=\"http://10.182.62.15:7011/services/receive\">\r\n" + 
					"   <soapenv:Header/>\r\n" + 
					"   <soapenv:Body>\r\n" + 
					"      <tns:ToHKMCGDIHResponse>\r\n" + 
					"         <IFRESULT>Z</IFRESULT>\r\n" + 
					"         <ZIP>"+ZIP+"</ZIP>\r\n" + 
					"         <DATATYPE>"+DATATYPE+"</DATATYPE>\r\n" + 
					"         <IFMESSAGE>"+"Success"+"</IFMESSAGE>\r\n" + 
					"         <OUT_DATA>\r\n" + 
					"            <outData>" +srcString+"</outData>\r\n" + 
					"         </OUT_DATA>\r\n" + 
					"      </tns:ToHKMCGDIHResponse>\r\n" + 
					"   </soapenv:Body>\r\n" + 
					"</soapenv:Envelope>";			
			return samplexml;
		}catch(Exception e) {
			e.printStackTrace(System.out);
			throw e;
			//return receiveData;
		}

	}	
	
	
	public static String base64ZipConversionZip(String flag, String src) {
		String receiveData	=	"";
		try {
			receiveData	=	src;
			if(flag.endsWith("JSON")) {
				receiveData	=	CommonUtils.XmlToJson(src);
				//receiveData	=	"{\"ROOT\":{\"inputs\":["+receiveData+"]}}";
			}else {
				if(!receiveData.startsWith("<?xml")) {
					receiveData	= "<?xml version=\"1.0\"?>"+receiveData;
				}
				//receiveData	= StringEscapeUtils.escapeHtml4(receiveData);
			}
			
			if(flag.startsWith("ZIP")) {
				if(flag.endsWith("XML")) {
//					if(!receiveData.startsWith("<?xml")) {
//						receiveData	= "<?xml version=\"1.0\"?>"+receiveData;
//					}					
				}
				receiveData			=	Gzip.compress64(receiveData);
			}
		}catch(Exception e) {
			e.printStackTrace(System.out);
			//LOG.error("[CommonUtils] >> getValueByDepthKey :: Error > " + CommonUtils.getPrintStackTrace(e));
			return receiveData;
		}

		return receiveData;
	}		
	
	public static List listOutChange(List<Map> mapList) {
		List outMapList	=	new ArrayList<Map>();
		for(Map inMap:mapList) {
			outMapList.add(mapOutChange(inMap));
		}
		return outMapList;
	}	
	
	public static Map mapOutChange(Map map) {
		Map outMap	=	new HashMap();
		for(Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			Object val = map.get(key);
			
			outMap.put("O_"+key, val);
		}
		return outMap;
	}	
}
