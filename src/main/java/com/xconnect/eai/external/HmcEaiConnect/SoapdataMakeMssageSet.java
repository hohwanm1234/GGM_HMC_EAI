/*
 * Copyright Hyundai AutoEver.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Hyundai AutoEver. ("Confidential Information").
 */
package com.xconnect.eai.external.HmcEaiConnect; 

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.json.JSONObject;
import org.json.XML;
import org.json.XMLParserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.xconnect.eai.server.common.constant.SystemCommonConstants;
import com.xconnect.eai.server.common.util.CommonUtils;
import com.xconnect.eai.server.common.vo.InterfaceRequestMessage;


public class SoapdataMakeMssageSet {
	private boolean verbose	=	true;
	private static final Logger LOG = LoggerFactory.getLogger(SoapdataMakeMssageSet.class);

	public void process(Object objectExchange, Map dummy) {
		try {
			if(verbose) LOG.debug("----------------------------------------------------1--");
			Exchange exchange						=	(Exchange) objectExchange;
			Map<String, Object> headerMap			= exchange.getIn().getHeaders();
			String soapBodyStr 						= exchange.getIn().getBody(String.class);
			if(verbose) LOG.debug("----------------------------------------------------2--");
			String soapAction 						= "";
			try {
				soapAction = headerMap.get("soapAction").toString();
			} catch (Exception e) {
				throw e;
			}
			if(verbose) LOG.debug("----------------------------------------------------3--");

			exchange.setProperty(SystemCommonConstants.CAMEL_PROPERTY_ORIGINAL_MSG, soapBodyStr);

			if(verbose) LOG.debug("------------------------------------------------------");
			if(verbose) LOG.debug("soapAction = " + soapAction);
			if(verbose) LOG.debug("soapBodyStr = " + soapBodyStr);
			if(verbose) LOG.debug("------------------------------------------------------");

			if(soapBodyStr != null && !soapBodyStr.isEmpty()) {
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
				// ns prefix 제거...
				for(String remove_prefix : tns_prefixList)
					jsonString = jsonString.replaceAll(remove_prefix + ":", "");

				soapRawMap = new Gson().fromJson(jsonString, Map.class);
				Map<String, Object> soapHeaderMap	= new HashMap<>();

				try {
					soapHeaderMap	= (Map<String, Object>)((Map)soapRawMap.get("Envelope")).get("Header");
				} catch(Exception e) {}
				Map<String, Object> soapBodyMap = (Map<String, Object>)((Map)soapRawMap.get("Envelope")).get("Body");

				if(CommonUtils.getValueByDepthKey(soapBodyMap, "request.Header.ifId", "") !=null) {
					if(soapHeaderMap == null)
						soapHeaderMap 		= new HashMap<String, Object>();

					String ifid	= (String) CommonUtils.getValueByDepthKey(soapBodyMap, "request.Header.ifId", "");
					String ifVer	= (String) CommonUtils.getValueByDepthKey(soapBodyMap, "request.Header.ifVer", "");
					soapHeaderMap.put(SystemCommonConstants.HEADER_KEY_IFID,ifid);
					soapHeaderMap.put(SystemCommonConstants.HEADER_KEY_IFVER,ifVer);
				}

				if(verbose) LOG.debug("soapBodyMap = " + soapBodyMap);
				if(verbose) LOG.debug("soapHeaderMap = " + soapHeaderMap);


				//////////////////////////////////////////////////////////////////////////
				// ## Soap Header Parsing & Interface Message 조합
				InterfaceRequestMessage interfaceRequestMessage = new InterfaceRequestMessage();			
				Object ifId 				= soapHeaderMap.get(SystemCommonConstants.HEADER_KEY_IFID) == null ? soapAction : soapHeaderMap.get(SystemCommonConstants.HEADER_KEY_IFID);
				Object ifVer 				= soapHeaderMap.get(SystemCommonConstants.HEADER_KEY_IFVER) == null ? "v001" : soapHeaderMap.get(SystemCommonConstants.HEADER_KEY_IFVER);
				Object ifTrackingId 		= soapHeaderMap.get(SystemCommonConstants.HEADER_KEY_TX_ID);
				Object ifDateTime			= soapHeaderMap.get(SystemCommonConstants.HEADER_KEY_DATETIME);
				Object ifSenderGrp			= soapHeaderMap.get(SystemCommonConstants.HEADER_KEY_SENDER_GROUP);
				Object ifSender				= soapHeaderMap.get(SystemCommonConstants.HEADER_KEY_SENDER);
				Object ifReceiverGrp		= soapHeaderMap.get(SystemCommonConstants.HEADER_KEY_RECEIVER_GROUP);
				Object ifReceiver			= soapHeaderMap.get(SystemCommonConstants.HEADER_KEY_RECEIVER);

				interfaceRequestMessage.setIfId( ifId.toString() );
				interfaceRequestMessage.setIfVer( (ifVer == null) ? "" : ifVer.toString());
				interfaceRequestMessage.setIfDateTime( (ifDateTime == null) ? new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) : ifDateTime.toString());
				interfaceRequestMessage.setIfTrackingId( (ifTrackingId == null) ? UUID.randomUUID().toString() : ifTrackingId.toString());
				interfaceRequestMessage.setIfSenderGrp( (ifSenderGrp == null) ? "" : ifSenderGrp.toString());
				interfaceRequestMessage.setIfSender( (ifSender == null) ? "" : ifSender.toString());
				interfaceRequestMessage.setIfReceiverGrp( (ifReceiverGrp == null) ? "" : ifReceiverGrp.toString());
				interfaceRequestMessage.setIfReceiver( (ifReceiver == null) ? "" : ifReceiver.toString());
				interfaceRequestMessage.setIfResult("");
				interfaceRequestMessage.setIfFailMsg("");
				interfaceRequestMessage.setBody(soapBodyMap);

				exchange.setProperty(SystemCommonConstants.REQUEST_DATA_TYPE, "soap");
				exchange.getIn().setHeaders(exchange.getIn().getHeaders());
				exchange.getIn().setBody(interfaceRequestMessage);
				Map searchMap	=   new HashMap();
				if(CommonUtils.getValueByDepthKey(soapBodyMap, "request.T_ITEM", "")!=null) 
					searchMap	=	(Map) CommonUtils.getValueByDepthKey(soapBodyMap, "request.T_ITEM", "");
				exchange.setProperty("CALL_INTERFACE_SEARCH_BODY", searchMap);

			    LOG.debug("[DbBatchProducer] >> (SELECT) properties => " +  exchange.getProperty("CALL_INTERFACE_SEARCH_BODY"));
				
				// Property Setting (Standard Common Header)
				exchange.setProperty(SystemCommonConstants.CAMEL_PROPERTY_COMMON_HEADER, interfaceRequestMessage.getHeader());
			} else {
				throw new RuntimeException("Soap Message가 없습니다.");
			}

		} catch (Exception e) {
			LOG.error("[StandardInterfaceMessageParser] >> soapMessageParser :: Error > " + CommonUtils.getPrintStackTrace(e));
			throw new RuntimeException("Soap Message가 없습니다.");
		}
	}	
	
}
