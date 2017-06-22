package com.nh.crawler.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



import java.util.List;
import java.util.Map;

import com.nh.crawler.conf.ConstantI;
import com.nh.crawler.template.DefaultMapRowMapper;
import com.nh.crawler.template.JDBCTemplate;

public class Db {
	
	public static Connection getSqlCon(){
		
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
			
			return DriverManager.getConnection(ConstantI.MYSQL_URL, 
					ConstantI.MYSQL_USER, 
					ConstantI.MYSQL_PWD);
			
		} catch (Exception e) {
			System.out.print(e);
			return null;
		}
	}
	
	
	
	
	public static long insertIntoDbItem(String name, float price,
			String merchant_name, String item_url, String item_extend,
			int status_flag, int create_time, int update_time, String item_from, int score, String item_id, String img_url) {

		Connection con = getSqlCon();
		JDBCTemplate jdbcTemplate = null;
		jdbcTemplate = new JDBCTemplate(con);

		String sql = "INSERT INTO crawler.goods_detail_info(name, price, merchant_name,"
				+ " item_url, item_extend, status_flag, create_time, update_time, item_from, score, item_id, img_url) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			return jdbcTemplate.insertGetId(sql, new Object[] { name, price,
					merchant_name, item_url, item_extend, status_flag,
					create_time, update_time, item_from, score, item_id, img_url});
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return 0L;
	}
	
	/* 判断存在否 没有的话在插入 */
	public static long isExistDbItemUrl(String source, String itemKey){
		
		Connection con = getSqlCon();
		JDBCTemplate jdbcTemplate = null;
		jdbcTemplate = new JDBCTemplate(con);

		String sql = "select id from crawler.goods_detail_info where item_id = ?";
		try {
			DefaultMapRowMapper defaultRowMapper = new DefaultMapRowMapper();
			Object  obj = jdbcTemplate.queryForObject(sql, new Object[]{itemKey}, defaultRowMapper);
			
			if(null == obj){
				return 0L;
			}
			
			return Long.parseLong(((Map<String, Object>)obj).get("id").toString());
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return 0L;
	}
	
	/* 判断存在否 没有的话在插入 */
	public static boolean isExistDbNav(String source, String url){
		
		Connection con = getSqlCon();
		JDBCTemplate jdbcTemplate = null;
		jdbcTemplate = new JDBCTemplate(con);

		String sql = "select id from crawler.navigation where source = ? AND url = ?";
		try {
			DefaultMapRowMapper defaultRowMapper = new DefaultMapRowMapper();
			Object  obj = jdbcTemplate.queryForObject(sql, new Object[]{source, url}, defaultRowMapper);
			
			if(null == obj){
				return false;
			}
			
			return true;
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return false;
	}
	
	//更新Navigation status_flag字段
	public static void updateDbNavFlag(String url){
		Connection con = getSqlCon();
		JDBCTemplate jdbcTemplate = null;
		jdbcTemplate = new JDBCTemplate(con);
		
		String sql = "UPDATE crawler.navigation SET status_flag=1 where url = ?";
		try {
			jdbcTemplate.execute(sql, new Object[]{url});
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return;
		
	}
	
	
	public static void insertIntoDbNav(String source,
			String category, String url, int status_flag,
			long create_time, long update_time) {

		Connection con = getSqlCon();
		JDBCTemplate jdbcTemplate = null;
		jdbcTemplate = new JDBCTemplate(con);

		String sql = "INSERT INTO crawler.navigation(source, category, url,"
				+ " status_flag, create_time, update_time) VALUES (?,?,?,?,?,?)";

		try {
			jdbcTemplate.insert(sql, new Object[] { source, category,
					url, status_flag, create_time, update_time });
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return;
	}
	
	public static int hashFunc(String key){  
	    int arraySize = 11113;          //数组大小一般取质数  
	    int hashCode = 0;  
	    for(int i=0;i<key.length();i++){        //从字符串的左边开始计算  
	        int letterValue = key.charAt(i) - 96;//将获取到的字符串转换成数字，比如a的码值是97，则97-96=1 就代表a的值，同理b=2；  
	        hashCode = ((hashCode << 5) + letterValue) % arraySize;//防止编码溢出，对每步结果都进行取模运算  
	    }  
	    return hashCode;  
	} 
	
	public static int getUrlId(String url){
		
		Connection con = getSqlCon();
		JDBCTemplate jdbcTemplate = null;
		jdbcTemplate = new JDBCTemplate(con);

		String sql = "select id from goods_detail_info where item_url = ?";

		try {
			DefaultMapRowMapper defaultRowMapper = new DefaultMapRowMapper();
			Object obj = jdbcTemplate.queryForObject(sql, new  Object[]{url}, defaultRowMapper);
			
			if(null == obj){
				return 0;
			}
			
			Map<String, Object> resultMap = (Map<String, Object>)obj;
			
			return Integer.parseInt(resultMap.get("id").toString());

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return 0;
	}
	
	public static int getCreateTime(int id){
		
		Connection con = getSqlCon();
		JDBCTemplate jdbcTemplate = null;
		jdbcTemplate = new JDBCTemplate(con);

		String sql = "select create_time from goods_detail_info where id = ?";

		try {
			DefaultMapRowMapper defaultRowMapper = new DefaultMapRowMapper();
			Object obj = jdbcTemplate.queryForObject(sql, new  Object[]{id}, defaultRowMapper);
			
			if(null == obj){
				return 0;
			}
			
			Map<String, Object> resultMap = (Map<String, Object>)obj;
			
			return Integer.parseInt(resultMap.get("create_time").toString());

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return 0;
	}

	public static void insertIntoDbTrend(int goodId,
			int commentNum,
			float goodRate,
			int goodCommentNum,
			int middleCommentNum,
			int badCommentNum,
			float price, 
			int createTime) {

		Connection con = getSqlCon();
		JDBCTemplate jdbcTemplate = null;
		jdbcTemplate = new JDBCTemplate(con);

		String sql = "INSERT INTO crawler.goods_trend(good_id,comment_num,"
				+ " good_rate, good_comment_num, mid_comment_num, bad_comment_num, price, create_time)"
				+ " VALUES (?,?,?,?,?,?,?,?)";

		try {
			jdbcTemplate.insert(sql, new Object[] {goodId,commentNum, goodRate,goodCommentNum,
					middleCommentNum, badCommentNum, price, createTime});
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return;
	}
	
	
	public static String getCategoryUrl(String source, String category){
		
		Connection con = getSqlCon();
		JDBCTemplate jdbcTemplate = null;
		jdbcTemplate = new JDBCTemplate(con);

		String sql = "select url from navigation where source = ? and category = ?";

		try {
			DefaultMapRowMapper defaultRowMapper = new DefaultMapRowMapper();
			Object obj = jdbcTemplate.queryForObject(sql, new  Object[]{source, category}, defaultRowMapper);
			
			if(null == obj){
				return null;
			}
			
			Map<String, Object> resultMap = (Map<String, Object>)obj;
			
			return resultMap.get("url").toString();

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	
	public static void updateDbNavUrl(String oldurl, String newurl){
		Connection con = getSqlCon();
		JDBCTemplate jdbcTemplate = null;
		jdbcTemplate = new JDBCTemplate(con);
		
		String sql = "UPDATE crawler.navigation SET url=? where url = ?";
		try {
			jdbcTemplate.execute(sql, new Object[]{newurl, oldurl});
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return;
		
	}
	
	
	public static void updateGoodDetailUrl(String oldurl, String newurl){
		Connection con = getSqlCon();
		JDBCTemplate jdbcTemplate = null;
		jdbcTemplate = new JDBCTemplate(con);
		
		String sql = "UPDATE crawler.goods_detail_info SET item_from=? where item_from = ?";
		try {
			jdbcTemplate.execute(sql, new Object[]{newurl, oldurl});
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return;
		
	}

}
