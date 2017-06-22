package com.nh.crawler.until.network;
//下载网页。支持https
public class SampleFetcher {
	public static void main(String[] args) throws Exception {
		HtmlFetcher htmlFetcher = new HtmlFetcher();
        String html = htmlFetcher.getHtml("http://www.washingtontimes.com/news/2012/aug/29/american-scene-cdc-says-west-nile-cases-rise-40-in/?page=1&utm_medium=RSS&utm_source=RSS_Feed", "utf-8",0);
        System.out.println("Html : "+html);
	}

}
