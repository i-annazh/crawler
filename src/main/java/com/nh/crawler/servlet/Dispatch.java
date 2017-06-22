package com.nh.crawler.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.nh.crawler.source.AbstractFilterSave;
import com.nh.crawler.source.ExtractElementI;
import com.nh.crawler.source.JindongImp;
import com.nh.crawler.until.JacksonUtils;
//为每个商品的url算出一个hash值。目的是加快查询
public class Dispatch extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9146542223635750671L;
	
	private static final String CHARACTER_ENCODING = "UTF-8";


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp){
		
		@SuppressWarnings("rawtypes")
		Enumeration p = req.getParameterNames();
		String pn = null;
		@SuppressWarnings("rawtypes")
		Map map = new HashMap();
		String result = null;
		int returnCode = 0;
		
		long startTime = System.currentTimeMillis();
		
		while(p.hasMoreElements()){
			pn = (String)p.nextElement();
			if(StringUtils.isNotBlank(pn)){
				map.put(pn, req.getParameter(pn));
			}
		}
		
		try {
			String act = map.get("act").toString();
			System.out.println(act);
			if(act.equals("1")){
				String url = java.net.URLDecoder.decode(map.get("url").toString(), "utf-8");
				System.out.println("url"+url);
				String source = java.net.URLDecoder.decode(map.get("source").toString(),"utf-8");
				System.out.println("source" +source);
				String category = java.net.URLDecoder.decode(map.get("category").toString(), "utf-8");
				System.out.println("category"+category);
				
				Thread t = new Thread(new Runnable(){  
		            public void run(){
		            	
		            	AbstractFilterSave ab= FactoryInstance.getInstatnce(source);
		            	
		            	if(ab.getSourceNname().equals("亚马逊"))
		            		ab.updateNavigation(category);
		            	
						//收到请求后开始爬取
						ab.extractListPage(url, 1);
						
		            }}); 
				
		        t.start();
				
				
			}
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		@SuppressWarnings("rawtypes")
		Map maprsp = new HashMap();
		
		maprsp.put("status", returnCode);
		maprsp.put("cost", (System.currentTimeMillis() - startTime));
		maprsp.put("result", result);
		
		PrintWriter out = null;
		
		try{
			resp.setCharacterEncoding(CHARACTER_ENCODING);
			out = resp.getWriter();
			out.print(JacksonUtils.getJsonString(maprsp));
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(out != null){
				out.flush();
				out.close();
			}
		}
		
		return;
	}


}
