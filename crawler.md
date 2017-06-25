# crawler系统源代码说明
##系统功能介绍
爬虫系统主要由以下几个功能实现：
###1.分析网页
爬虫程序crawler的爬虫策略需要建立在分析各大电商网页的基础上进行。通过分析各大电商的数据结构，我们得出下列结论：每个电商网站基本上都分为首页（包含商品类目数据）、一级类目和商品详情页这三个界面层次。首页商品类目数据包含一级类目的url，一级类目数据中包含商品详细信息的url。

###2.页面爬取功能设计
爬虫系统的主要功能是毋庸置疑是爬取。爬取功能的实现借助于两个包：jetty框架和jsoup网页分析包；jetty框架提供爬虫程序的httpserver功能，jsoup分析包提供分析网页界面提取所需数据的功能。
经过上节的分析，商品类目信息可以在首页得到，通过爬取类目信息可以得到一级类目的url，一级类目包含数据展示页,通过对一级类目的爬取可以得到商品详情页的url，通过对商品详情页可以得到评价信息字段。接下来会以亚马逊商品数据提取为例详细分析每个页面字段的提取规则。
 
###3.爬取导航栏数据
导航栏数据从首页中提取，就是电商入口页。

目标：取导航栏数据 格式：String url=””; String category=””;

原始数据：Amazon首页html   格式：html

例如： 
	 ` window.when("data").run(function(data) { data({"Deep…Panel":
	{"promoID":"nav-sa-deepbrowse-household-appliances",
	"template":
	{
	"name":"itemListDeepBrowse",
	"data":{
	"count":1,
	"text1":"家用电器",
	"items":[
	{
	"text":"电视/音响",
	"url":"/b/ref=sa_menu_applia_l2_b874259051?... ",
	"items":[
	{
	"text":"平板电视",
	"url":"/b/ref=sa_menu_applia_l3_b874269051?...","columnBreak":"1"
	},
	{"text":"迷你音响",
	"url":"/b/ref=sa_menu_applia_l3_b874267051?..."
	},
	{"text":"投音机",
	"url":"/b/ref=sa_menu_applia_l3_b1397684071?..."
	},
	…
	}}); }); ` 

数据分析：

>由数据格式来看，该数据是json格式。通过js加载数据，整个数据放在data中，data存放了一个字符串和一个名为template的json数据。Template存储着以key为name和data数据，data又以json数据格式存储。data数据中key为"text1"，表示第一层类目结构，"items"表示主类目下的第二层类目划分，里面依然存储了key为items数json数据。这个items以json数据格式存储了第三层类目名和具体的商品url信息。

提取方式：

	首页html页面中取 数据类型：json数据
提取规则：

-	先用匹配规则"run\\(function\\(data\\) \\{ data(.*?)\\}\\}\\); \\}\\);"去匹配整个javascript，定义匹配数据规则
-	对匹配到的数据进行获取并追加"}})"并保存
-	截取掉保存数据的右括号”)”并保存在变量中
-	用Map存储匹配到并处理好后的数据
-	先获得key，最外层map，key的筛选规则：key.contains("Deepbrowse")
-	在根据key获得第二层map，此时要用Map存储key和template
-	在根据key为template获得第三层map，保存在变量dataMap中
-	获得dataMap的text名字属性值
-	获得dataMap下的items属性值，存储在变量itemList中
-	遍历itemList，获得items的key为text属性值，获取items数组里的每一项数据
-	把所有数据对象存储在reslutList中

目标数据：

	导航栏类目category字段和列表页url字段获取
格式： 

		String category= name1 + " "+ name2 + " "+ name3;
		String url="https://www.amazon.com" + url3;  



###4.爬取列表页数据
目标：

	取列表页数据  格式：html

原始数据：

	详情页html页面   格式：html
例子：

 ` <div id=”s-result-info-bar” class=”a-row-a-…”>
	<div id=” s-result-info-content” class=”a-row”>
	…
</div>
<div> ` 

提取方式：

	页面中取
  
提取规则：

-	对爬取得页面获取id="search-results"所有内容保存在listEle对象中
-	对listEle的内容用class="s-item-container"做筛选，保存在itemList对象中
-	遍历itemList
-	得到商品名title：取每个item的class="a-size-base"的第一条匹配数据的"data-attribute"属性作为商品名
-	得到详情页url：取每个item的class="s-access-detail-page"的第一条匹配数据的"href"属性作为商品详情页url
-	得到商品价格：取每个item的class=" a-size-base "的第二条匹配数据的text作为商品的价格，并对价格去掉人民币符号"￥"
-	得到商品图片的url：取每个item的class=" s-access-image "的第一条匹配数据的src属性作为商品图片url
-	把得到的字段保存在List对象中

目标数据：

	列表页数据
   
格式：

     String itemName=…;
	 String itemUrl=…;
 	 String itemFrom=…;
	 String itemPrice=…;
	 String itemPic=…;  



###5.爬取详情页数据
目标：

	商品详情页数据 格式：html

原始数据：

	商品详情页html  格式：html

目标数据url来源：

` http://www.amazon.cn/gp/customer-reviews/widgets/average-customer-review/popover/ref=dpx_acr_pop_?contextId=dpx&asin=商品id` 

例子：

` <table id="histogramTable" class="a-normal a-align-middle a-spacing-base">
		…
</table>` 

提取方式：

	页面中取

提取规则：

-	对获取的html页面用class="a-histogram-row"筛选，并把筛选后的html保存在trEles中
-	声明数组a[]存储1-5颗星的数量
-	遍历trEles，用class="a-size-small"进行筛选，取匹配的第二个字符串的text依次保存在a[i]中
-	定义评价数规则如下
-	总评数count：count=a[1]+a[2]+a[3]+a[4]+a[5]
-	好评数good_num:good_num=a[1]+a[2]
-	中评数mid_num:mid_num=a[3]
-	差评数bad_num:bad_num=a[4]+a[5]
-	好评率good_rate:good_rate=good_num/count;
-	把数据保存到resultMap中

目标数据：

	商品详情页的评价数据信息

格式：
		
		Int count = …;
   		Int good_num = …;
   		Int mid_num = …;
  		Int bad_num = …;
  		Int good_rate = …;



##系统代码介绍
以京东为例
###爬取导航栏数据实现代码

	`public List<Map<String, String>> extractNavigationBar() {
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
	} ` 
###爬取列表页数据实现代码
 	`public List<Map<String, String>> extractListPage(String url, int page) {
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
	}`
###爬取详情页数据实现代码
	`public Map<String, String> extractItemPage(String url) {
		
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
	}`

##反爬机制分析和应对策略
###1.jsoup模拟浏览器请求

如果我们通过jsoup发http请求给某个页面，一般都会失败，因为电商服务器那边做了反爬处理，如果判断访问的http没有带浏览器头部信息，则过滤该请求。我们做的应对策略是调用jsoup包封装的http模拟浏览器向电商服务器发http请求并接收处理返回结果，这时模拟浏览器行为需要伪装浏览器头信息，如下代码所示：

 //http 伪装头部
	`Response r = Jsoup.connect(pageurl).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36").timeout(ConstantI.TIMEOUT).execute();
	html = r.body(); ` 

###2.亚马逊反爬策略
每个电商的反爬机制不一样，所有针对不同的电商网站我们要做不同的反爬应对策略。亚马逊的反爬机制是每隔几天就用随机数更新类目url中某个数字串，导致我们之前爬去的类目url再访问时已经不存在。所以针对亚马逊，我们制定的反爬策略是每次爬取数据之前都更新类目，这样就解决再次爬取类目url时页面不存在的问题。

##url去重处理
导航栏页面提取的一级类目url基本上不存在重复，但爬取一级类目后得到的商品详情页的url却存在相同的产品，这些产品我们不必重复爬取。

url去重（基于特征去重算法）：对于每个电商的url，其实都拥有一个自身的唯一序列，以京东https://item.jd.com/12281889185.html为例，我们可以采集出12281889185这个字段加上京东这个源作为去重的key，这样我们每次只要以这个条件作为查询判断依据就能得出这个url是新的还是之前存在的，达到去重目的。如下url去重流程图：



##系统爬取结构设计
###爬取接口设计
根据每个电商模板的爬取需求定义公共接口ExtractElement，接口中定义了三个抽象方法：爬取类目方法extractNavigationBar()，爬取一级类目数据方法extractListPage()和爬取商品详情页数据方法extractItemPage()。


###爬取抽象类设计
总体crawler数据结构如下图所示：定义了一个抽象类AbstractFilterSave，实现了ExtractElement接口，并在抽象方法中实现了将爬取数据存入数据库的公共方法，此方法将被每个电商爬取模板调用。每个电商爬取模板类都继承自该抽象类，整体的类设计达到了很好的重用与耦合。



###爬取实例继承
 ` public class JindongImp extends AbstractFilterSave{...} ` 

##爬虫系统通信设计
###服务器实现爬取
系统采用crawler程序作为爬虫服务器，crawler程序使用jetty框架提供爬虫程序的httpserver功能。当用户对某个url进行爬取时，客户端程序manage向爬虫程序发送爬取命令，crawler爬虫程序接收请求并调用本身的爬取列表页和详情页方法爬取数据；为了防止连接超时，在爬取过程中会返回状态码给页面程序。爬取结束后crawler再统一把爬取的数据存入到数据库中。Crawler内部采用多线程结构，针对每个爬取请求都会创建一个新的线程服务。数据的爬取则是依靠jsoup框架向电商网站发送请求来完成。
