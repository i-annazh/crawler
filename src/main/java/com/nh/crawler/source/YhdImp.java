package com.nh.crawler.source;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nh.crawler.conf.ConstantI;
import com.nh.crawler.until.JacksonUtils;
import com.nh.crawler.until.network.HtmlFetcher;

public class YhdImp extends AbstractFilterSave{
	
	private String url = "http://www.yhd.com/";
	private String name = "一号店";
	
	@Override
	public List<Map<String, String>> extractNavigationBar() {
		// TODO Auto-generated method stub
		
		String naviUrl = url;
		
		HtmlFetcher htmlFetcher = new HtmlFetcher();
		String html = null;
		
		try {
			html = htmlFetcher.getHtml(naviUrl, "utf-8", ConstantI.TIMEOUT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		Document doc = Jsoup.parse(html);
		Element target = doc.getElementsByClass("mod_hd_allsort").get(0);
		
		Elements listEle = target.select("a");
		
		
		List<Map<String, String>> reslutList = Lists.newArrayList();
		for(Element a:listEle){
			String title = a.attr("title");
			String url = a.attr("href");
			
			if(!url.contains("list")){
				continue;
			}
			
			Map<String, String> tempMap = Maps.newHashMap();
			tempMap.put("category", title);
			tempMap.put("url", "http:" +url);
			
			reslutList.add(tempMap);
			
			System.out.println(title);
			System.out.println(url);
		}
		
		saveNavigation(reslutList, name);

		return reslutList;
	}

	@Override
	public List<Map<String, String>> extractListPage(String url, int page) {
		// TODO Auto-generated method stub
		// 一号单翻页 采用的jsonp 请求 非网页加载 所以 如果需要翻页 需要重写写一套代码 分析jsonp
		String pageurl = url;
		
		System.out.println(pageurl);
		
		HtmlFetcher htmlFetcher = new HtmlFetcher();
		String html = null;
		Map<String, String> cookies = Maps.newHashMap();
		cookies.put("provinceId", "15");
		cookies.put("cityId", "159");
		try {
			//html = htmlFetcher.getHtml(pageurl, "utf-8", ConstantI.TIMEOUT);
			Response r = Jsoup.connect(pageurl).cookies(cookies).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(3000).execute();
			html = r.body();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		Document doc = Jsoup.parse(html);
		
		Element target = doc.getElementById("itemSearchList");
		
		//System.out.println(target.toString());
		
		Elements listEle = target.getElementsByClass("num");
		Elements listPic = target.getElementsByClass("img");

		List<Map<String, String>> reslutList = Lists.newArrayList();
		int i = 0;
		for(Element a:listEle){
			
			Element picEle = listPic.get(i++);
			
			String id = a.attr("productid");
			Element itemELe = target.getElementById("pdlink2_" +id);
			
			String title = itemELe.attr("title");
			String href = itemELe.attr("href");
			if(!href.contains("item.yhd.com")){
				continue;
			}
			
			String price = a.attr("yhdprice");
			
			String pic = picEle.select("img").get(0).attr("src");
			if(StringUtils.isBlank(pic)){
				//original
				pic = picEle.select("img").get(0).attr("original");
			}
			pic = "http:"+pic;
			System.out.println(title);
			System.out.println(href);
			System.out.println(price);
			
			Map<String, String> tempMap = Maps.newHashMap();

			tempMap.put("itemName", title);
			tempMap.put("itemUrl", href);
			tempMap.put("itemSku", id);
			tempMap.put("itemPrice", price);
			tempMap.put("itemFrom", url);
			tempMap.put("itemPic", pic);
			
			reslutList.add(tempMap);
		}

		System.out.println(reslutList.size());
		setScore(reslutList);
		saveItem(reslutList, name);
		return reslutList;
	}

	@Override
	public Map<String, String> extractItemPage(String url) {
		// TODO Auto-generated method stub
		String itemid = getItemKey(url);
		System.out.println(url);
		Map<String, String> cookies = Maps.newHashMap();
		cookies.put("provinceId", "15");
		cookies.put("cityId", "159");
		try {
			String rateurl = "http://e.yhd.com/front-pe/getRateByPM.do?pmInfoId="+itemid;
			String numurl = "http://e.yhd.com/front-pe/queryNumsByPm.do?pmInfoId="+itemid;
			
			String html = Jsoup.connect(rateurl).cookies(cookies).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute().body();
			Map<String, Object> map = JacksonUtils.getObjectmapper().readValue(html, Map.class);
			
			System.out.println(itemid);
			double rate = Double.parseDouble(map.get("proScore").toString())/100d;
			html = Jsoup.connect(numurl).cookies(cookies).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute().body();
			map = JacksonUtils.getObjectmapper().readValue(html, Map.class);
			
			int count = Integer.parseInt(map.get("experienceNum").toString());
			
			int goodnum = (int)(rate*count);
			int midnum = 0;
			int badnum = count - goodnum;
			
			Map<String, String> resultMap = Maps.newHashMap();
			resultMap.put("rate", String.valueOf(rate));
			resultMap.put("allnum", String.valueOf(count));
			resultMap.put("goodnum", String.valueOf(goodnum));
			resultMap.put("midnum",  String.valueOf(midnum));
			resultMap.put("badnum",  String.valueOf(badnum));
			
			return resultMap;
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;

	}
	
	
	@Override
	public String getItemKey(String url) {
		// TODO Auto-generated method stub
		String temp = url;
		if(url.indexOf("?") > 0){
			url = url.substring(0, url.indexOf("?"));
		}

		String regEx="[^0-9]";   
		Pattern p = Pattern.compile(regEx);   
		Matcher m = p.matcher(temp); 
		
		String itemid = m.replaceAll("").trim();
		
		return itemid;
	}
	
	@Override
	public String getSourceNname() {
		// TODO Auto-generated method stub
		return name;
	}
	
	public static void main(String[] args) {
		YhdImp y = new YhdImp();
		
//		y.extractNavigationBar();
		String listurl = "http://list.yhd.com/c21392-0-84179";
		listurl = "http://list.yhd.com/c23586-0-81436/";
		y.extractListPage(listurl, 1);
//		System.out.println(y.getItemKey("http://item.yhd.com/item/33336522"));
		
//		HtmlFetcher htmlFetcher = new HtmlFetcher();
//		String html = null;
//		
//		try {
//			html = htmlFetcher.getHtml("http://item.yhd.com/item/41152010?tracker_u=101281789&union_ref=5_2&cp=1", "utf-8", ConstantI.TIMEOUT);
//			Response r = Jsoup.connect("http://item.yhd.com/item/41152010?tracker_u=101281789&union_ref=5_2&cp=1").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute();
//			html = r.body();
//			
//			System.out.println(html);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return ;
//		}
		
		//Map<String, String> resultMap = y.extractItemPage("http://item.yhd.com/item/10606920?tracker_u=101281789&union_ref=5_2&cp=1");
		
		//System.out.println(JacksonUtils.getJsonString(resultMap));
	}

	

}
