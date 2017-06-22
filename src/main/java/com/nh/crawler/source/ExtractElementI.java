package com.nh.crawler.source;

import java.util.List;
import java.util.Map;

public interface ExtractElementI {
	
	/* 抽取导航栏 形成 到具体类目的 url 
	 * 返回元素有 具体类目  url 来自某个网站例如
	 * 手机/手机通信/对讲机   https://list.jd.com/list.html?cat=9987,653,659 京东
	 * */
	List<Map<String, String>> extractNavigationBar();
	
	
	/*
	 * 抽取list 页面元素
	 * 例如我们获取到 https://list.jd.com/list.html?cat=9987,653,659
	 * 获取每个list下面 商品名字 价格 店铺名称 详情页的url
	 * 例如价格25 ，名字苹果6s手机套   店铺名字摩斯维数码专营店  url=https://item.jd.com/1146448811.html
	 * 预留extend字段没有写空 json格式
	 * */
	List<Map<String, String>> extractListPage(String url, int page);
	
	
	/*
	 * 抽取详情页的内容 具体看情况而定 详情页有商品的具体信息 介绍等等
	 * */
	//	String rate = resultMap.get("rate");
	//	String allnum = resultMap.get("allnum");
	//	String goodnum = resultMap.get("goodnum");
	//	String midnum = resultMap.get("midnum");
	//	String badnum = resultMap.get("badnum");
	Map<String, String> extractItemPage(String url);
	
	String getItemKey(String url);
	
	
	String getSourceNname();

}
