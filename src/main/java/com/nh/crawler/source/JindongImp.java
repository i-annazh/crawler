package com.nh.crawler.source;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
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


public class JindongImp extends AbstractFilterSave{
	
	private String url = "https://www.jd.com";
	private String name = "京东";
	private Set<String> setUrlMap = Sets.newHashSet();
	
	public JindongImp(){}

	public List<Map<String, String>> extractNavigationBar() {
		// TODO Auto-generated method stub
		HtmlFetcher htmlFetcher = new HtmlFetcher();
		String html = null;
		//导航栏json格式，分析网页得出
		String jsonpUrl = "https://dc.3.cn/category/get?callback=getCategoryCallback";
		List<Map<String, String>> reslutList = Lists.newArrayList();
		
		try {
			html = htmlFetcher.getHtml(jsonpUrl, "gbk", ConstantI.TIMEOUT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		Pattern pp = Pattern.compile("getCategoryCallback([^;]*)");
		Matcher mm = pp.matcher(html);
		
		while(mm.find()){
			html = mm.group(1);
		}
		
		html = html.substring(1, html.lastIndexOf(")"));
		try {
			Map<String, Object> map = (Map<String, Object>)JacksonUtils.getObjectmapper().readValue(html, Map.class);
		
			List<Map<String, Object>> list = (List<Map<String, Object>>)map.get("data");

			
			for(Map<String, Object> basemap:list){
				//用"s"匹配。得到每个目录下的一级目录
				List<Map<String, Object>> listbase = (List<Map<String, Object>>)basemap.get("s");
				
				//每个子目录下用"n"匹配得到二级目录下的类目名
				String firstCategory = "";
				for(Map<String, Object> tempmap:listbase){
					firstCategory = firstCategory + tempmap.get("n").toString().split("\\|")[1] + " ";
				}
				firstCategory = firstCategory.trim();
				//得每个子目录下的类目名和url
				for(Map<String, Object> tempmap:listbase){
					
					System.out.println(firstCategory);
					List<Map<String, Object>> list1 = (List<Map<String, Object>>)tempmap.get("s");

					for(Map<String, Object> tempmap1:list1){
						//用"n"匹配可以得到url，用|匹配可以得到详细类目
						String secondArray[] = tempmap1.get("n").toString().split("\\|");
						String secondCategory = secondArray[1];
						String secondUrl =  filter(secondArray[0]);
						
						if(null == secondUrl){
							continue;
						}
						
						System.out.println(firstCategory +" "+ secondCategory + "https://" + secondUrl);
						Map<String, String> tempMap = Maps.newHashMap();
						tempMap.put("category", firstCategory +" "+ secondCategory);
						tempMap.put("url", "https://" + secondUrl);
						reslutList.add(tempMap);
						//得三级目录						
						List<Map<String, Object>> list2 = (List<Map<String, Object>>)tempmap1.get("s");
						
						for(Map<String, Object> tempmap2:list2){
							String thirdArray[] = tempmap2.get("n").toString().split("\\|");
							String thirdCategory = thirdArray[1];
							String thirdUrl =  filter(thirdArray[0]);
							
							if(null == thirdUrl){
								continue;
							}
							
							System.out.println(firstCategory +" "+ secondCategory + " " + thirdCategory  + "https://"+thirdUrl);
							Map<String, String> temp3Map = Maps.newHashMap();
							temp3Map.put("category", firstCategory +" "+ secondCategory + " " + thirdCategory);
							temp3Map.put("url", "https://"+ thirdUrl);
							reslutList.add(temp3Map);
						}
						
					}
					
					break;
				}
			}
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 

		//System.out.println(html);
		//存入数据库
		saveNavigation(reslutList, name);
		
		return reslutList;
	}
	//存入数据库之前去重
	private String filter(String url){
		
		String resultUrl = null;
		
		if(url.contains("jd") && !url.contains("list")){
			return null;
		}
		
		if(url.contains("-")){
			resultUrl = "list.jd.com/list.html?cat=" + url.replace("-", ",");
		}
		
		if(null == resultUrl){
			if(url.contains("&")){
				resultUrl = url.substring(0, url.indexOf("&"));
			}else{
				resultUrl =  url;
			}
		}
		
		
		
		if(setUrlMap.contains(resultUrl)){
			return null;
		}
		
		setUrlMap.add(resultUrl);
		
		return resultUrl;
	}

	public List<Map<String, String>> extractListPage(String url, int page) {
		// TODO Auto-generated method stub
		
		String pageurl = url + "&page=#";
		pageurl = pageurl.replace("#", String.valueOf(page));
		
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
			return null;
		}
		
		//gl-warp clearfix 
		Document doc = Jsoup.parse(html);
		
		Elements listEle = doc.getElementsByClass("gl-warp");
		
		if(null == listEle || 0 == listEle.size()){
			System.out.println("it is over");
			return null;
		}
		
		/* only one */
		Element listMain = listEle.get(0);
		
		Elements itemsEle = listMain.getElementsByClass("gl-item");
		
		if(null == itemsEle || 0 == itemsEle.size()){
			System.out.println("item not find");
			return null;
		}
		
		String skus = "";
		List<Map<String, String>> itemList = Lists.newArrayList();
		for(Element item:itemsEle){
			
			String itemurl ="https:"+ item.getElementsByClass("p-name").get(0).select("a").get(0).attr("href");
			String itemname = item.getElementsByClass("p-name").get(0).select("a").get(0).text();
			String itemsku = "J_" + item.getElementsByClass("p-operate").get(0).getElementsByClass("p-o-btn").get(0).attr("data-sku");
			
			String temp = item.getElementsByClass("p-img").get(0).select("img").get(0).attr("src");
			
			if(StringUtils.isBlank(temp)){
				temp = item.getElementsByClass("p-img").get(0).select("img").get(0).attr("data-lazy-img");
			}
			String pic = "https:" + temp;
			System.out.println(itemname + "    " + itemurl + " pic = " + pic);
			skus = skus+itemsku+",";
			Map<String, String> tempItemMap = Maps.newHashMap();
			tempItemMap.put("itemName", itemname);
			tempItemMap.put("itemUrl", itemurl);
			tempItemMap.put("itemSku", itemsku);
			tempItemMap.put("itemFrom", url);
			tempItemMap.put("page", String.valueOf(page));
			tempItemMap.put("itemPic", pic);
			
			itemList.add(tempItemMap);
		}
		
		skus = skus.substring(0, skus.length() -1);
		
		Map<String, String> priceMap = getPrice(skus);
		
		for(Map<String, String> tempItemMap:itemList){
			tempItemMap.put("itemPrice", priceMap.get(tempItemMap.get("itemSku")));
		}
		
		setScore(itemList);
		
		saveItem(itemList, name);
		
		return itemList;
	}
	
	public Map<String, String> getPrice(String skulist){
		HtmlFetcher htmlFetcher = new HtmlFetcher();
		String html = null;
		
		try {
			//html = htmlFetcher.getHtml("https://p.3.cn/prices/mgets?callback=jQuery1581537&type=1&skuIds=" + java.net.URLEncoder.encode(skulist, "utf-8"), "utf-8", ConstantI.TIMEOUT);
			html = Jsoup.connect("http://p.3.cn/prices/mgets?callback=jQuery1581537&type=1&skuIds=" + java.net.URLEncoder.encode(skulist, "utf-8"))
					.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute().body();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("http://p.3.cn/prices/mgets?callback=jQuery1581537&type=1&skuIds=" + skulist);
			return null;
		}
		
		Pattern pp = Pattern.compile("jQuery1581537([^;]*)");
		Matcher mm = pp.matcher(html);
		
		while(mm.find()){
			html = mm.group(1);
		}
		
		html = html.substring(1, html.lastIndexOf(")"));
		List<Map<String, String>> list = null;
		
		try {
			list = (List<Map<String, String>>)JacksonUtils.getObjectmapper().readValue(html, List.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		Map<String, String> returnMap = Maps.newHashMap();
		for(Map<String, String> priceMap:list){
			returnMap.put(priceMap.get("id"), priceMap.get("p"));
		}
		
		
		return returnMap;
	}

	public Map<String, String> extractItemPage(String url) {
		
		String itemid = getItemKey(url);

		/* 根据url爬取详情页 */
		String html = null;
		
		String commenturl = "http://club.jd.com/comment/productCommentSummaries.action?referenceIds=#";
		
		try {
			html = Jsoup.connect(commenturl.replace("#", itemid)).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute().body();
			Map<String, Object> commentMap = JacksonUtils.getObjectmapper().readValue(html, Map.class);
			List<Map<String, Object>> resultList = (List<Map<String, Object>>)commentMap.get("CommentsCount");
			commentMap = resultList.get(0);
			
			int count = (int)commentMap.get("CommentCount");
			double rate = (double)commentMap.get("GoodRate");
			int goodnum = (int)commentMap.get("GoodCount");
			int midnum = (int)commentMap.get("GeneralCount");
			int badnum = (int)commentMap.get("PoorCount");
			
			Map<String, String> resultMap = Maps.newHashMap();

			
			resultMap.put("rate", String.valueOf(rate));
			resultMap.put("allnum", String.valueOf(count));
			resultMap.put("goodnum", String.valueOf(goodnum));
			resultMap.put("midnum",  String.valueOf(midnum));
			resultMap.put("badnum",  String.valueOf(badnum));
			
			return resultMap;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(commenturl.replace("#", itemid));
			return null;
		}
	}
	
	@Override
	public String getItemKey(String url) {
		// TODO Auto-generated method stub
		
		String regEx="[^0-9]";   
		Pattern p = Pattern.compile(regEx);   
		Matcher m = p.matcher(url); 
		
		String itemid = m.replaceAll("").trim();
		
		return itemid;
	}
	
	@Override
	public String getSourceNname() {
		// TODO Auto-generated method stub
		return name;
	}
	
	public static void main(String[] args) {
		JindongImp jindong = new JindongImp();
		//jindong.extractNavigationBar();
		//jindong.extractListPage("https://list.jd.com/list.html?cat=1316,1384,11930", 1);
		//String skuList = "J_1068548,J_3379813,J_427726,J_10807208540,J_546093,J_10039754711,J_710698,J_2145572,J_907695,J_11126641498,J_1123825,J_246522,J_579711,J_11288751040,J_263724,J_907754,J_907744,J_1470484406,J_286197,J_10746166010,J_1967074599,J_1351488024,J_907746,J_1260676,J_10161738448,J_1972922931,J_1121132,J_1157775000,J_10807283541,J_1967074598,J_1975571676,J_1978110993,J_10623420898,J_1964751076,J_1978110992,J_379875,J_10024247328,J_10039754710,J_10865151150,J_1287729652,J_1981207970,J_11286136208,J_1964989829,J_1706646369,J_1953941240,J_1981848152,J_11126641497,J_10207575101,J_1967342680,J_11005537071,J_1967123754,J_11503022012,J_1965641184,J_10695842526,J_1968313148,J_1969033169,J_10327459004,J_1968229513,J_10918471995,J_1633932151";
		//Map<String, String> map = jindong.getPrice(skuList);
		
		//System.out.println(JacksonUtils.getJsonString(map));
		jindong.extractListPage("https://list.jd.com/list.html?cat=737,752,902", 1);
		//jindong.extractItemPage("https://item.jd.com/4206468.html");
		
	}

	
}
