package com.nh.crawler.template;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DefaultMapRowMapper implements RowMapper {

	private String[] columnNames = null;
	
	private String[] columnClassName = null;
	
	public DefaultMapRowMapper(){}
	
	public DefaultMapRowMapper(String columnClassName[]){
		this.columnClassName = columnClassName;
	}
	
	public Object mapRow(ResultSet set) throws SQLException {
		if(columnNames == null){
			ResultSetMetaData metaData = set.getMetaData();
			int size = metaData.getColumnCount();
			columnNames = new String[size];
			for(int i=0; i<size; i++){
				columnNames[i] = metaData.getColumnName(i+1);
			}
		}
		
		Map<String,Object> map = new HashMap<String,Object>();
		int size = columnNames.length;
		for(int i=0; i<size; i++){
			if(columnClassName == null){
				map.put(columnNames[i], set.getObject(i+1));
			}else{
				if("String".equals(columnClassName[i])){
					map.put(columnNames[i], set.getString(i+1));
				}else if("Integer".equals(columnClassName[i])){
					map.put(columnNames[i], set.getInt(i+1));
				}else if("Long".equals(columnClassName[i])){
					map.put(columnNames[i], set.getLong(i+1));
				}else if("Double".equals(columnClassName[i])){
					map.put(columnNames[i], set.getDouble(i+1));
				}else if("Date".equals(columnClassName[i])){
					map.put(columnNames[i], set.getDate(i+1));
				}else if("Float".equals(columnClassName[i])){
					map.put(columnNames[i], set.getFloat(i+1));
				}else if("Timestamp".equals(columnClassName[i])){
					map.put(columnNames[i], set.getTimestamp(i+1));
				}else if("Boolean".equals(columnClassName[i])){
					map.put(columnNames[i], set.getBoolean(i+1));
				}else if("Short".equals(columnClassName[i])){
					map.put(columnNames[i], set.getShort(i+1));
				}else if("Byte".equals(columnClassName[i])){
					map.put(columnNames[i], set.getByte(i+1));
				}else if("BigDecimal".equals(columnClassName[i])){
					map.put(columnNames[i], set.getBigDecimal(i+1));
				}
			}
		}
		return map;
	}

}
