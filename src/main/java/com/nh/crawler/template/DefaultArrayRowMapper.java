package com.nh.crawler.template;


import java.sql.ResultSet;
//定义数据库返回数据类型。以map形式返回
import java.sql.SQLException;

public class DefaultArrayRowMapper implements RowMapper {

	private int size = -1;
	
	private String[] columnClassName = null;
	
	public DefaultArrayRowMapper(){}
	
	public DefaultArrayRowMapper(String columnClassName[]){
		this.columnClassName = columnClassName;
	}
	
	public Object mapRow(ResultSet set) throws SQLException {
		if(size == -1){
			size = set.getMetaData().getColumnCount();
		}
		
		Object[] rowValues = new Object[size];
		for(int i=0; i<size; i++){
			if(columnClassName == null){
				rowValues[i] = set.getObject(i+1);
			}else{
				if("String".equals(columnClassName[i])){
					rowValues[i] = set.getString(i+1);
				}else if("Integer".equals(columnClassName[i])){
					rowValues[i] = set.getInt(i+1);
				}else if("Long".equals(columnClassName[i])){
					rowValues[i] = set.getLong(i+1);
				}else if("Double".equals(columnClassName[i])){
					rowValues[i] = set.getDouble(i+1);
				}else if("Date".equals(columnClassName[i])){
					rowValues[i] = set.getDate(i+1);
				}else if("Float".equals(columnClassName[i])){
					rowValues[i] = set.getFloat(i+1);
				}else if("Timestamp".equals(columnClassName[i])){
					rowValues[i] = set.getTimestamp(i+1);
				}else if("Boolean".equals(columnClassName[i])){
					rowValues[i] = set.getBoolean(i+1);
				}else if("Short".equals(columnClassName[i])){
					rowValues[i] = set.getShort(i+1);
				}else if("Byte".equals(columnClassName[i])){
					rowValues[i] = set.getByte(i+1);
				}else if("BigDecimal".equals(columnClassName[i])){
					rowValues[i] = set.getBigDecimal(i+1);
				}
			}
		}
		return rowValues;
	}

}
