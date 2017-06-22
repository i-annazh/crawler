package com.nh.crawler.source;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nh.crawler.conf.ConstantI;
import com.nh.crawler.db.Db;
import com.nh.crawler.until.JacksonUtils;
import com.nh.crawler.until.network.HtmlFetcher;

public class AmazonImp extends AbstractFilterSave{
	
	private String url = "https://www.amazon.cn";
	private String name = "亚马逊";
	private Set<String> setUrlMap = Sets.newHashSet();
	
	public AmazonImp(){}

	@Override
	public List<Map<String, String>> extractNavigationBar() {
		// TODO Auto-generated method stub
		HtmlFetcher htmlFetcher = new HtmlFetcher();
		String html = null;
		List<Map<String, String>> reslutList = Lists.newArrayList();
		try {
			//html = htmlFetcher.getHtml(url, "utf-8", ConstantI.TIMEOUT);
			html = Jsoup.connect("http://www.amazon.cn")
					.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute().body();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		//System.out.println(html);
		//}}); });
		Pattern pp = Pattern.compile("run\\(function\\(data\\) \\{ data(.*?)\\}\\}\\); \\}\\);");
		Matcher mm = pp.matcher(html);
		String temp = "";
		while(mm.find()){
			temp = mm.group(1)+"}})";
	

		}

		temp = temp.substring(1, temp.lastIndexOf(")"));

		//System.out.println(temp);
		
		try {
			//用Map存储匹配到的数据
			Map<String, Object> map = JacksonUtils.getObjectmapper().readValue(temp, Map.class);
			//先获得key，最外层map
			for (String key : map.keySet()) {
				if(!key.contains("Deepbrowse")){
					continue;
				}
				System.out.println("key= "+ key);
				//在根据key获得第二层map
				Map<String, Object> subMap  = (Map<String, Object>)map.get(key);
				Map<String, Object> tepMap = (Map<String, Object>)subMap.get("template");
				//在根据key为template获得第三层map
				Map<String, Object> dataMap = (Map<String, Object>)tepMap.get("data");
				//获得dataMap的text名字属性值
				String name1 = dataMap.get("text1").toString();
				//获得dataMap下的items属性值。
				List<Map<String, Object>> itemList = (List<Map<String, Object>>)dataMap.get("items");
				
				for(Map<String, Object> map1:itemList){
					if(!map1.containsKey("text")){
						continue;
					}
					//获得items的一级text属性值
					String name2 = map1.get("text").toString();
					//获得items的一级url值，一般取下面更精确的url
					//String url2 = map1.get("url").toString();
					
					//获取items数组里的每一项数据
					List<Map<String, Object>> item3List = (List<Map<String, Object>>)map1.get("items");
					for(Map<String, Object> map2:item3List){
						String url=null;
						String name3 = map2.get("text").toString();
						String url3 = map2.get("url").toString();
						if(null == url3){
							continue;
						}
						System.out.println(name1 + " "+ name2 + " "+ name3 + "https://www.amazon.com" + url3);
						//把所有数据对象存储在reslutList中
						Map<String, String> tempMap = Maps.newHashMap();
						tempMap.put("category", name1 + " "+ name2 + " "+ name3);
						
						url=java.net.URLDecoder.decode(url3, "utf-8");
						url="https://www.amazon.cn" +url;
						tempMap.put("url",  url);
						reslutList.add(tempMap);
					}
					break;
				}
	
			}
			
			
			
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		saveNavigation(reslutList, name);
		
		return reslutList;
	}

	
	//列表页分析抓取
	@Override
	public List<Map<String, String>> extractListPage(String url, int page) {
		// TODO Auto-generated method stub
		String pageurl = url + "&page=#";
		
		pageurl = pageurl.replace("#", String.valueOf(page));
		pageurl= url;
		HtmlFetcher htmlFetcher = new HtmlFetcher();
		String html = null;
		try {
			//https
			//html = htmlFetcher.getHtml(pageurl, "utf-8", ConstantI.TIMEOUT);
			//http 伪装头部
			Response r = Jsoup.connect(pageurl).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute();
			html = r.body();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			
			if(e.toString().contains("404")){
				Db.updateDbNavFlag(url);
			}
			return null;
		}
		//id="search-results" class="a-section" 
		Document doc = Jsoup.parse(html);
		//id=resultsCol search-results mainResults
		Element listEle = doc.getElementById("search-results");
		if(null == listEle){
			System.out.println("it is over");
			Db.updateDbNavFlag(url);
			return null;
		}
		//System.out.println(listEle);
		Elements itemList = listEle.getElementsByClass("s-item-container");
		//System.out.println(itemList);
		List<Map<String, String>> itemLists = Lists.newArrayList();
		for(Element item:itemList){
			//System.out.println(item);
			String title = item.getElementsByClass("a-size-base").get(0).attr("data-attribute");
			String detailurl = item.getElementsByClass("s-access-detail-page").get(0).attr("href");
			//detailurl=java.net.URLDecoder.decode(item.get("detailurl").toString(), "utf-8");
			String price = item.getElementsByClass("a-size-base").get(1).text();
			String pic = item.getElementsByClass("s-access-image").get(0).attr("src");
			String newprice=price.replace("￥", "");
			System.out.println(newprice);
			try {
				detailurl = java.net.URLDecoder.decode(detailurl, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			System.out.println(title + " " +detailurl);
			
			Map<String, String> tempItemMap = Maps.newHashMap();
			tempItemMap.put("itemName", title);
			tempItemMap.put("itemUrl", detailurl);
			
			newprice = newprice.replace(",", "");
			tempItemMap.put("itemPrice", newprice);
			tempItemMap.put("itemFrom", pageurl);
			tempItemMap.put("itemPic", pic);
			//添加page
			tempItemMap.put("page", String.valueOf(page));
			itemLists.add(tempItemMap);
			//测试
			//break;
		}
		setScore(itemLists);
		saveItem(itemLists, name);
		return itemLists;
	}

	@Override
	public Map<String, String> extractItemPage(String url) {
		// TODO Auto-generated method stub
		try {
			//###########/dp/
			String tempUrl = url;
			
			String key = getItemKey(tempUrl);
			
			String commenturl = "http://www.amazon.cn/gp/customer-reviews/widgets/average-customer-review/popover/ref=dpx_acr_pop_?contextId=dpx&asin=#";
			commenturl = commenturl.replace("#", key);
			
			System.out.println(commenturl);
			
			String html = null;
			
			try {
				Response r = Jsoup.connect(commenturl).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute();
				html = r.body();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				return null;
			}
			
			Document doc = Jsoup.parse(html);
			
			Elements trEles = doc.getElementsByClass("a-histogram-row");
			int[] a = { 0, 0, 0, 0, 0 };  
			int i =0;
			for(Element e:trEles){
				String temp = e.getElementsByClass("a-size-small").get(1).text();
				a[i] = Integer.parseInt(temp);
				System.out.println(a[i]);
				i++;
			}
			
			int count = a[0]+a[1]+a[2]+a[3]+a[4];
			int goodnum = a[0]+a[1];
			
			DecimalFormat  df = new DecimalFormat("######0.00");
			
			double  rate = 0d;
			if(0 == count){
				rate = 0d;
			}else{
				rate = Double.parseDouble(df.format((double)goodnum / (double)count));;
			}
			
			
			int midnum = a[2];
			int badnum = a[3]+a[4];
			
			Map<String, String> resultMap = Maps.newHashMap();

			
			resultMap.put("rate", String.valueOf(rate));
			resultMap.put("allnum", String.valueOf(count));
			resultMap.put("goodnum", String.valueOf(goodnum));
			resultMap.put("midnum",  String.valueOf(midnum));
			resultMap.put("badnum",  String.valueOf(badnum));
			
			System.out.println(a.toString());
			
			return resultMap;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String getItemKey(String url) {
		// TODO Auto-generated method stub
		String tempUrl = url;
		int index = tempUrl.indexOf("/dp/");
		tempUrl = tempUrl.substring(index + "/dp/".length(), tempUrl.length());
		String key = tempUrl.substring(0 , tempUrl.indexOf("/"));
		return key;
	}
	
	public static void main(String[] args) {
		AmazonImp amazonImp = new AmazonImp();
		//amazonImp.extractNavigationBar();
		amazonImp.extractListPage("https://www.amazon.cn/b/ref=sa_menu_applia_l3_b874272051/460-1029895-3869868?ie=UTF8&node=874272051", 1);
		//amazonImp.extractListPage("https://www.amazon.com/b/ref=sa_menu_applia_l3_b874274051/461-8654114-8672361?ie=UTF8&node=874274051", 1);
		//Map<String, String> resultMap = amazonImp.extractItemPage("http://www.amazon.cn/Skechers-%E6%96%AF%E5%87%AF%E5%A5%87-ON-THE-GO-%E7%94%B7-ON-THE-GO-GLIDE-%E8%BD%BB%E8%B4%A8%E7%BB%91%E5%B8%A6%E4%BC%91%E9%97%B2%E9%9E%8B-53775-NVGY-%E8%93%9D%E8%89%B2-39-5/dp/B06X3X9P51/ref=sr_1_1?m=A1AJ19PSB66TGU&s=shoes&ie=UTF8&qid=1493294629&sr=1-1&nodeID=1885494071");
		
		//System.out.println(JacksonUtils.getJsonString(resultMap));
	}

	@Override
	public String getSourceNname() {
		// TODO Auto-generated method stub
		return name;
	}

	

}
