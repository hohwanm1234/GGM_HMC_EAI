/*
 * Copyright none.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of . ("Confidential Information").
 */


import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.json.JSONObject;
import org.json.XMLParserConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.xconnect.eai.server.common.util.CommonUtils;
import com.xconnect.eai.server.common.vo.InterfaceRequestMessage;
import com.xconnect.eai.server.common.vo.InterfaceResponseMessage;
import com.xconnect.eai.server.component.webservice.client.ErpCanias;
import com.xconnect.eai.server.service.developer.WSDLSchemaService;

/**
 * <pre>
 * com.xconnect.eai.test 
 *    |_ TestWS.java
 * 
 * </pre>
 * 
 * @date : 2019. 11. 25. �삤�썑 3:39:03
 * @version :
 * @author : A931744
 */
public class TestWS {

	private static final Logger LOG = LoggerFactory.getLogger(TestWS.class);

	@Test
	public void test() {
		String originalStr = "Á÷¿ø_°æºñ½Ç"; // �뀒�뒪�듃  
		String [] charSet = {"utf-8","euc-kr","ksc5601","iso-8859-1","x-windows-949"}; 
		for (int i=0; i<charSet.length; i++) { 
			for (int j=0; j<charSet.length; j++) { 
				try {
					System.out.println("[" + charSet[i] +"," + charSet[j] +"] = " + new String(originalStr.getBytes(charSet[i]), charSet[j])); 
				} catch (UnsupportedEncodingException e) {e.printStackTrace(); } 
			} 
		}
		
		

		
		
		
	}
	
	@SuppressWarnings("rawtypes")
	//@Test
	public void soap_test() throws Exception {

		String soapResponse = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n" + 
				"   <soapenv:Body>\r\n" + 
				"      <ns1:callServiceResponse soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:ns1=\"http://webservice.ias.com\">\r\n" + 
				"         <callServiceReturn href=\"#id0\"/>\r\n" + 
				"      </ns1:callServiceResponse>\r\n" + 
				"      <multiRef id=\"id0\" soapenc:root=\"0\" soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xsi:type=\"ns2:CaniasResponse\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:ns2=\"http://10.182.62.10:8080\">\r\n" + 
				"         <Response href=\"#id1\"/>\r\n" + 
				"         <ExtraVariables href=\"#id2\"/>\r\n" + 
				"         <Messages href=\"#id3\"/>\r\n" + 
				"         <SYSStatus xsi:type=\"xsd:int\">0</SYSStatus>\r\n" + 
				"         <SYSStatusError xsi:type=\"xsd:string\"/>\r\n" + 
				"         <RequestId xsi:type=\"xsd:int\">0</RequestId>\r\n" + 
				"      </multiRef>\r\n" + 
				"      <multiRef id=\"id1\" soapenc:root=\"0\" soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xsi:type=\"ns3:StringResponse\" xmlns:ns3=\"http://10.182.62.10:8080\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n" + 
				"         <Value xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?>&lt;TBLRETURN>&lt;/TBLRETURN></Value>\r\n" + 
				"         <Comressed xsi:type=\"xsd:boolean\">false</Comressed>\r\n" + 
				"      </multiRef>\r\n" + 
				"      <multiRef id=\"id3\" soapenc:root=\"0\" soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xsi:type=\"ns4:StringResponse\" xmlns:ns4=\"http://10.182.62.10:8080\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n" + 
				"         <Value xsi:type=\"xsd:string\"/>\r\n" + 
				"         <Comressed xsi:type=\"xsd:boolean\">false</Comressed>\r\n" + 
				"      </multiRef>\r\n" + 
				"      <multiRef id=\"id2\" soapenc:root=\"0\" soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xsi:type=\"ns5:StringResponse\" xmlns:ns5=\"http://10.182.62.10:8080\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n" + 
				"         <Value xsi:type=\"xsd:string\"><![CDATA[<EXTRAVARIABLES><VARIABLE><NAME>''</NAME><TYPE>NOTFOUND</TYPE><VALUE>''</VALUE></VARIABLE></EXTRAVARIABLES>]]></Value>\r\n" + 
				"         <Comressed xsi:type=\"xsd:boolean\">false</Comressed>\r\n" + 
				"      </multiRef>\r\n" + 
				"   </soapenv:Body>\r\n" + 
				"</soapenv:Envelope>";

		try {
			
			CamelContext ctx = new DefaultCamelContext();
			Exchange exchange = new DefaultExchange(ctx);
			
			
			String resultXml = soapResponse;

			InterfaceRequestMessage bodyMsg = new InterfaceRequestMessage();
			
			
//			if(endpoint.isVerbose()) LOG.debug("[WebServiceProducer] >> process :: resultXml = " + resultXml);
			JSONObject jsonObj = org.json.XML.toJSONObject(resultXml, XMLParserConfiguration.KEEP_STRINGS);
			@SuppressWarnings("rawtypes")
			Map resultMap = new Gson().fromJson(jsonObj.toString(), Map.class);

//			Map result = resultMap.stream().filter(x -> x.get("name").equals(searchText)).findAny().get();
//	        System.out.println(result);
			
//			Map replM	=	new HashMap();
//			Object result2	=	WSDLSchemaService.expandHref(resultMap,resultMap,replM);
//			for(Object okey:replM.keySet()) {
//				Object vData	=	replM.get(okey);
//				((Map)okey).clear();
//				((Map)okey).putAll((Map)vData);
//				((Map)vData).clear();
//			}

			// xmlns attribute 제거
			WSDLSchemaService.normalizeElement(resultMap,null,null);
			
			Map replM	=	new HashMap();
			List<Map> listMultRef	= (List) CommonUtils.getValueByDepthKey(resultMap, "Envelope.Body.multiRef", "");	
			for(Map data:listMultRef) {
				
			}

			// Response Message setting
			InterfaceResponseMessage responseMsg = new InterfaceResponseMessage(bodyMsg);
			responseMsg.setHeader(bodyMsg.getHeader());
			responseMsg.setPayload(resultMap);
			responseMsg.setDuration(System.currentTimeMillis() - bodyMsg.getStart_tm());
			responseMsg.setIfResult("S");
			responseMsg.setIfFailMsg("");
			exchange.getIn().setBody(responseMsg);
			
			
			
			ErpCanias	ec	= new ErpCanias();
//			System.out.println(ec.hmcToCaniasResponse(exchange,null));
			
			
			
			
			
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static int countLines(String str) {
	    if(str == null || str.isEmpty())
	    {
	        return 0;
	    }
	    int lines = 1;
	    int pos = 0;
	    while ((pos = str.indexOf("\n", pos) + 1) != 0) {
	        lines++;
	    }
	    return lines;
	}
	
	
	
}
