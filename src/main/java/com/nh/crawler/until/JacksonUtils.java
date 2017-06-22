package com.nh.crawler.until;

import org.codehaus.jackson.map.ObjectMapper;
//Map转成String，String转map。传输过程中以String传输。Jackson为一种编码格式
public class JacksonUtils {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public static ObjectMapper getObjectmapper() {
		return objectMapper;
	}

	public static String getJsonString(Object obj){
		if(obj == null){
			return null;
		}
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
