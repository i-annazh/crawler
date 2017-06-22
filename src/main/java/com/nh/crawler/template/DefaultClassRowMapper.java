package com.nh.crawler.template;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DefaultClassRowMapper implements RowMapper {

	@SuppressWarnings({ "unused", "rawtypes" })
	private Class cls;
	
	private int clsType;
	
	public static final int CLASS_TYPE_BOOLEAN = 1;
	public static final int CLASS_TYPE_SHORT = 2;
	public static final int CLASS_TYPE_INT = 3;
	public static final int CLASS_TYPE_LONG = 4;
	public static final int CLASS_TYPE_FLOAT = 5;
	public static final int CLASS_TYPE_DOUBLE = 6;
	public static final int CLASS_TYPE_STRING = 7;
	public static final int CLASS_TYPE_CUSTOM = 8;
	
	@SuppressWarnings("rawtypes")
	public DefaultClassRowMapper(Class cls){
		this.cls = cls;
		if(cls.equals(Boolean.class)){
			clsType = CLASS_TYPE_BOOLEAN;
		}else if(cls.equals(Short.class)){
			clsType = CLASS_TYPE_SHORT;
		}else if(cls.equals(Integer.class)){
			clsType = CLASS_TYPE_INT;
		}else if(cls.equals(Long.class)){
			clsType = CLASS_TYPE_LONG;
		}else if(cls.equals(Float.class)){
			clsType = CLASS_TYPE_FLOAT;
		}else if(cls.equals(Double.class)){
			clsType = CLASS_TYPE_DOUBLE;
		}else if(cls.equals(String.class)){
			clsType = CLASS_TYPE_STRING;
		}else{
			clsType = CLASS_TYPE_CUSTOM;
		}

	}
	
	public Object mapRow(ResultSet set) throws SQLException {
		Object result = null;
		switch(clsType){
			case CLASS_TYPE_BOOLEAN:
				result = set.getBoolean(1);
				break;
			case CLASS_TYPE_SHORT:
				result = set.getShort(1);
				break;
			case CLASS_TYPE_INT:
				result = set.getInt(1);
				break;
			case CLASS_TYPE_LONG:
				result = set.getLong(1);
				break;
			case CLASS_TYPE_FLOAT:
				result = set.getFloat(1);
				break;
			case CLASS_TYPE_DOUBLE:
				result = set.getDouble(1);
				break;
			case CLASS_TYPE_STRING:
				result = set.getString(1);
				break;
			case CLASS_TYPE_CUSTOM:
				
				break;
		
		}
		return result;
	}

}
