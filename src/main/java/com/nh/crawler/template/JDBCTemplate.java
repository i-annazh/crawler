package com.nh.crawler.template;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import com.mysql.jdbc.Statement;



public class JDBCTemplate {

	private static final Logger log  = Logger.getLogger(JDBCTemplate.class);
	
	private Connection connection = null;
	
	public JDBCTemplate(Connection connection){
		this.connection = connection;
	}
	
	@SuppressWarnings("rawtypes")
	public List queryForList(String sql, RowMapper rowMapper){
		return queryForList(sql, null, rowMapper);
	}
	
	@SuppressWarnings("rawtypes")
	public List queryForList(String sql, Object[] param, Class cls){
		return queryForList(sql, param, new DefaultClassRowMapper(cls));
	}
	
	public List<Object> queryForList(String sql, Object[] param, RowMapper rowMapper){
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		try {
			if(log.isInfoEnabled()){
				log.info("sql:"+sql);
				log.info("param:"+ToStringBuilder.reflectionToString(param));
			}
			ps = connection.prepareStatement(sql);
			setParam(ps, param);
			ps.execute();
			resultSet = ps.getResultSet();
			List<Object> ls = new ArrayList<Object>();
			while(resultSet.next()){
				ls.add(rowMapper.mapRow(resultSet));
			}
			return ls;
		} catch (SQLException e){
			return null;
		}finally{
			if(resultSet != null){
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				resultSet = null;
			}
			if(ps != null){
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				ps = null;
			}
		}
	}
	
	/**
	 * 查询一条记录，如果有多条，则返回第一条
	 * @param sql
	 * @param rowMapper
	 * @return
	 */
	public Object queryForObject(String sql, RowMapper rowMapper){
		return queryForObject(sql, null, rowMapper);
	}
	
	/**
	 * 查询一条记录，如果有多条，则返回第一条
	 * @param sql
	 * @param param
	 * @param rowMapper
	 * @return
	 */
	public Object queryForObject(String sql, Object[] param, RowMapper rowMapper){
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		try {
			if(log.isInfoEnabled()){
				log.info("sql:"+sql);
				log.info("param:"+ToStringBuilder.reflectionToString(param));
			}
			ps = connection.prepareStatement(sql);
			setParam(ps, param);
			ps.execute();
			resultSet = ps.getResultSet();
			if(resultSet.next()){
				return rowMapper.mapRow(resultSet);
			}
			return null;
		} catch (SQLException e){
			return null;
		}finally{
			if(resultSet != null){
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				resultSet = null;
			}
			if(ps != null){
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				ps = null;
			}
		}
	}
	
	public void insert(String sql, Object[] param){
		execute(sql, param);
	}
	
	public long insertGetId(String sql, Object[] param){

        PreparedStatement ps = null;  
        ResultSet resultSet = null;
        
        try {
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);  
            setParam(ps, param);  
            ps.executeUpdate();  
            resultSet = ps.getGeneratedKeys();  
              
            Object retId = null;  
            if (resultSet.next())  
                retId = resultSet.getObject(1);  
            else{  
            	log.error("sql插入异常" + param[0]);
                return 0L;
            }
            
            return Long.parseLong(retId.toString());  
              
        } catch (Exception e) {
        	log.error("sql:"+ e);
        	System.out.println(e);
            return 0L;
        } finally {
        	
        	if(resultSet != null){
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				resultSet = null;
			}
        	
        	if(ps != null){
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				ps = null;
			} 
        }  
	}
	
	public void update(String sql, Object[] param){
		execute(sql, param);
	}

	public void execute(String sql){
		execute(sql, null);
	}
	
	public void execute(String sql, Object[] param){
		PreparedStatement ps = null;
		try {
			if(log.isInfoEnabled()){
				log.info("sql:"+sql);
				log.info("param:"+ToStringBuilder.reflectionToString(param));
			}
			ps = connection.prepareStatement(sql);
			setParam(ps, param);
			ps.execute();
		} catch (SQLException e){
			log.error("execute" + sql + e);
			System.out.println(e);
		}finally{
			if(ps != null){
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				ps = null;
			}
		}
	}
	
	public void batchExecute(String sql, List<Object[]> paramList){
		if(paramList == null || paramList.size() == 0){
			return ;
		}
		PreparedStatement ps = null;
		Object[] param = null;
		try {
			boolean autoCommit = connection.getAutoCommit();
			if(autoCommit){
				connection.setAutoCommit(false);
			}
			ps = connection.prepareStatement(sql);
			for(int i=0; i<paramList.size(); i++){
				if(log.isDebugEnabled()){
					log.info("sql:"+sql);
					log.info("param:"+ToStringBuilder.reflectionToString(param));
				}
				param = paramList.get(i);
				setParam(ps, param);
				ps.addBatch();
			}
			ps.executeBatch();
			if(autoCommit){
				connection.setAutoCommit(autoCommit);
			}
		} catch (SQLException e){
			if(log.isDebugEnabled()){
				log.info("sql:\t"+sql);
				log.info("param:\t"+java.util.Arrays.toString(param));
			}
		}finally{
			if(ps != null){
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				ps = null;
			}
		}
	}
	
	private void setParam(PreparedStatement ps, Object[] param) throws SQLException{
		if(param != null && param.length > 0){
			for(int i=0; i<param.length; i++){
				ps.setObject(i+1, param[i]);
			}
		}
	}
}
