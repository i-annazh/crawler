package com.nh.crawler.source;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nh.crawler.conf.ConstantI;
import com.nh.crawler.until.JacksonUtils;
import com.nh.crawler.until.network.HtmlFetcher;

public class TianmaoImp extends AbstractFilterSave{
	private String name = "天猫";
	@Override
	public List<Map<String, String>> extractNavigationBar() {
		// TODO Auto-generated method stub
		
		String html = null;
		String pageurl = "http://www.tmall.com/";
		
		try {
			//https
			//html = htmlFetcher.getHtml(pageurl, "utf-8", ConstantI.TIMEOUT);
			//http 伪装头部
			Response r = Jsoup.connect(pageurl).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute();
			html = r.body();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		Document doc = Jsoup.parse(html);
		
//		Element target = doc.getElementById("J_defaultData");
//		System.out.println(target.toString()); 
		
		String s= "\"appId\":(\\d+)";
        Pattern  pattern=Pattern.compile(s);  
        Matcher  ma=pattern.matcher(html);  
   
        List<String> appList = Lists.newArrayList();
        Set<String> appSet = Sets.newHashSet();
        while(ma.find()){ 
        	String temp = ma.group(1);
        	if(appSet.contains(temp)){
        		continue;
        	}
        	
        	appList.add(temp);
        	appSet.add(temp); 
        }
        

        for(String tempCatId:appList){
        	
        	System.out.println(tempCatId);
            String caturl = "http://aldh5.tmall.com/recommend2.htm?notNeedBackup=true&appId=" + tempCatId;
            
            List<Map<String, String>> reslutList = Lists.newArrayList();
            try {
            	HtmlFetcher htmlFetcher = new HtmlFetcher();
    			html = htmlFetcher.getHtml(caturl, "gbk", ConstantI.TIMEOUT);
    			//http 伪装头部
    			//Response r = Jsoup.connect(caturl).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute();
    			//html = r.body();
    			
    			Map<String, Object> map = (Map<String, Object>)JacksonUtils.getObjectmapper().readValue(html, Map.class);
    			//获取key
    			String key = "";
    			for (Map.Entry<String, Object> m :map.entrySet())  {  
    				key = m.getKey();  
    				break;
    	        }  
    			
    			map = (Map<String, Object>)map.get(key);
    			List<Map<String, Object>> objList = (List<Map<String, Object>>)map.get("data");
    			for(Map<String, Object> tempMap:objList){
    				
    				Map<String, String> tempresultMap = Maps.newHashMap();
    				tempresultMap.put("category", tempMap.get("title").toString());
    				String tempAct = tempMap.get("action").toString();
    				
    				if(!tempAct.contains("list.tmall.com")){
    					continue;
    				}
    				
    				tempAct = getCat(tempAct);
    				if(StringUtils.isBlank(tempAct)){
    					continue;
    				}
    				tempresultMap.put("url", "https://list.tmall.com/search_product.htm?cat=" + tempAct);
    				reslutList.add(tempresultMap);
    			}
    			
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			continue;
    		}
            
            System.out.println(JacksonUtils.getJsonString(reslutList));
            saveNavigation(reslutList, "天猫");
            try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        }
        
        
	
		return null;
	}
	
	public String getCat(String temp){
		String s= "cat=(\\d+)";
        Pattern  pattern=Pattern.compile(s);  
        Matcher  ma=pattern.matcher(temp);  
  
        while(ma.find()){
        	return ma.group(1);
        }
		return "";
	}

	@Override
	public List<Map<String, String>> extractListPage(String url, int page) {
		// TODO Auto-generated method stub
		//&s=120
		String tempUrl = url.replace("https", "http");
		
		String html = "";
		try {
			Response r = Jsoup.connect(tempUrl).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute();
			html = r.body();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(tempUrl);
			return null;
		}
		
		//System.out.println(html);
		List<Map<String, String>> itemList = Lists.newArrayList();
		Document doc = Jsoup.parse(html);
		
		Element listEle = doc.getElementById("J_ItemList");
		
		Elements itemEles = listEle.getElementsByClass("product-iWrap");
		
		for(Element item:itemEles){
			//System.out.println(item.toString());
			
			try {
				String title  = item.getElementsByClass("productTitle").get(0).select("a").get(0).text();
				String itemUrl = "https:"+item.getElementsByClass("productTitle").get(0).select("a").get(0).attr("href");
				String price = item.getElementsByClass("productPrice").text().replace("¥", "");
				String pic1 = item.getElementsByClass("productImg").get(0).select("img").get(0).attr("src");
				if(StringUtils.isBlank(pic1)){
					//data-ks-lazyload
					pic1 = item.getElementsByClass("productImg").get(0).select("img").get(0).attr("data-ks-lazyload");
				}
				String pic = "https:" + pic1;
				System.out.println(title);
				System.out.println(itemUrl);
				System.out.println(price);
				
				Map<String, String> tempItemMap = Maps.newHashMap();
				tempItemMap.put("itemName", title);
				tempItemMap.put("itemUrl", itemUrl);
				tempItemMap.put("itemPrice", price);
				tempItemMap.put("itemFrom", url);
				tempItemMap.put("itemPic", pic);
				
				tempItemMap.put("page", String.valueOf(page));
				itemList.add(tempItemMap);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
		}
		
		setScore(itemList);
		
		saveItem(itemList, name);
		
		return itemList;
	}

	@Override
	public Map<String, String> extractItemPage(String url) {
		// TODO Auto-generated method stub
		
		Map<String, String> resultMap;
		try {

			String itemid = getItemKey(url);
			if (itemid == null) {
				return null;
			}
			
			System.out.println(itemid);
			String html ="";
			String tempUrl = "https://rate.tmall.com/listTagClouds.htm?itemId=#&isAll=true&isInner=true&callback=".replace("https", "http");
			tempUrl = tempUrl.replace("#", itemid);
			
			try {
				Response r = Jsoup.connect(tempUrl).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute();
				html = r.body();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
			html = html.replace("\"tags\":", "");
			System.out.println(html);
			Map<String, Object> map = null;
			int goodnum = 0;
			try {
				
				map = JacksonUtils.getObjectmapper().readValue(html, Map.class);
				goodnum = Integer.parseInt(map.get("rateSum").toString());
				
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
			tempUrl = "https://dsr-rate.tmall.com/list_dsr_info.htm?itemId=#".replace("https", "http");
			tempUrl = tempUrl.replace("#", itemid);
			
			try {
				Response r = Jsoup.connect(tempUrl).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute();
				html = r.body();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			int count = 0;
			try {
				html = html.substring(html.indexOf("(") + 1, html.length()-1);
				map = JacksonUtils.getObjectmapper().readValue(html, Map.class);
				count = Integer.parseInt(((Map<String, Object>)map.get("dsr")).get("rateTotal").toString());
				
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
			if(goodnum > count){
				goodnum = count;
			}
			
			DecimalFormat  df = new DecimalFormat("######0.00"); 
			double rate = 0d;
			if(0 != count)
				rate= Double.parseDouble(df.format((double)goodnum / (double)count));
			
			int midnum = 0;
			int badnum = count - goodnum;
			
			resultMap = Maps.newHashMap();

			
			resultMap.put("rate", String.valueOf(rate));
			resultMap.put("allnum", String.valueOf(count));
			resultMap.put("goodnum", String.valueOf(goodnum));
			resultMap.put("midnum",  String.valueOf(midnum));
			resultMap.put("badnum",  String.valueOf(badnum));
			
			System.out.println(JacksonUtils.getJsonString(resultMap));
			return resultMap;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return null;
		
	}
	
	@Override
	public String getItemKey(String url) {
		// TODO Auto-generated method stub
		
		String regEx="id=(\\d+)";   
		Pattern p = Pattern.compile(regEx);   
		Matcher m = p.matcher(url);
		
		String itemid = "";
		if (m.find()) {
			itemid =  m.group(1);
		}else{
			return null;
		}
		
		return itemid;
	}
	
	@Override
	public String getSourceNname() {
		// TODO Auto-generated method stub
		return name;
	}
	
	public static void main(String[] args) {
		TianmaoImp tianmaoImp = new TianmaoImp();
		//tianmaoImp.extractNavigationBar();
		//{"category":"夏季新品","url":"https://list.tmall.com/search_product.htm?cat=53636001"}
		tianmaoImp.extractListPage("https://list.tmall.com/search_product.htm?cat=54180014", 1);
		//tianmaoImp.extractItemPage("https://detail.tmall.com/item.htm?spm=a220m.1000858.1000725.1.doUKW9&id=547230212482&skuId=3312990124698&user_id=196993935&cat_id=53636001&is_b=1&rn=9fb17b50df8e091d7de1ae7e7165ee50");
	}

	

}
