/*
 * Copyright Hyundai AutoEver.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Hyundai AutoEver. ("Confidential Information").
 */
package com.xconnect.eai.external.HmcEaiConnect; 

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;

import com.xconnect.eai.server.common.constant.SystemCommonConstants;
import com.xconnect.eai.server.common.util.CommonUtils;


public class GGMMessageMapper {
	
	/**
	 * <pre>
	 * 1. 개요 : EAI Adapter 앞에서 인터페이스 값을 자동매핑 처리
	 *          body ifid 값을 -> 자체 ifid 로 설정
	 * 2. 처리내용 : 
	 * </pre>
	 * @Method Name : process
	 * @date : 2020. 12. 18.
	 * @author : 6801740
	 * @history : 
	 * <pre>
	 *	-----------------------------------------------------------------------
	 *	변경일	         작성자                변경내용  
	 *	----------- ------------------- ---------------------------------------
	 *	2020. 12. 18.		6801740				최초 작성 
	 *	-----------------------------------------------------------------------
	 * <pre>
	 *
	 * @param objectExchange
	 * @param inSoapRawMap
	 * @throws Exception
	 */ 	
	public void process(Object objectExchange, Map<String, Object> inSoapRawMap) throws Exception {
		try {
			/*
			 * Interface ID 만 매핑
			 */
			Map<String, Object> header = new HashMap<>();
			Map<String, Object> soapRawMap 		= (Map<String, Object>) inSoapRawMap;
			Map<String, Object> soapHeaderMap	= null;
			try {
				soapHeaderMap	= (Map<String, Object>)((Map)soapRawMap.get("Envelope")).get("Header");
			} catch(Exception e) {}
			Map<String, Object> soapBodyMap = (Map<String, Object>)((Map)soapRawMap.get("Envelope")).get("Body");
			if(soapHeaderMap == null)
				soapHeaderMap 		= new HashMap<String, Object>();
			Exchange exchange	=	(Exchange) objectExchange;
			exchange.setProperty("HEADER_YN", "N");
			
			// 0: soapHeaderMap 값을 바탕으로 기본갑셋팅
			String requstPath	=	"request.header.";
			soapHeaderMap.put(SystemCommonConstants.HEADER_KEY_IFID	,CommonUtils.getValueByDepthKeyNullFree(soapBodyMap, requstPath+SystemCommonConstants.HEADER_KEY_IFID, ""));
			soapHeaderMap.put(SystemCommonConstants.HEADER_KEY_IFVER ,CommonUtils.getValueByDepthKeyNullFree(soapBodyMap, requstPath+SystemCommonConstants.HEADER_KEY_IFVER, ""));
			soapHeaderMap.put(SystemCommonConstants.HEADER_KEY_TX_ID ,CommonUtils.getValueByDepthKeyNullFree(soapBodyMap, requstPath+SystemCommonConstants.HEADER_KEY_TX_ID, ""));

			soapHeaderMap.put(SystemCommonConstants.HEADER_KEY_SENDER_GROUP,CommonUtils.getValueByDepthKeyNullFree(soapBodyMap, requstPath+SystemCommonConstants.HEADER_KEY_SENDER_GROUP, ""));
			soapHeaderMap.put(SystemCommonConstants.HEADER_KEY_SENDER,CommonUtils.getValueByDepthKeyNullFree(soapBodyMap, requstPath+SystemCommonConstants.HEADER_KEY_SENDER, ""));
			soapHeaderMap.put(SystemCommonConstants.HEADER_KEY_RECEIVER_GROUP,CommonUtils.getValueByDepthKeyNullFree(soapBodyMap, requstPath+SystemCommonConstants.HEADER_KEY_RECEIVER_GROUP, ""));
			soapHeaderMap.put(SystemCommonConstants.HEADER_KEY_RECEIVER,CommonUtils.getValueByDepthKeyNullFree(soapBodyMap, requstPath+SystemCommonConstants.HEADER_KEY_RECEIVER, ""));
			((Map)soapRawMap.get("Envelope")).put("Header",soapHeaderMap);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}	
	
}
