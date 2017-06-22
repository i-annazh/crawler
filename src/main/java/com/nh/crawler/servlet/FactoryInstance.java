package com.nh.crawler.servlet;

import com.nh.crawler.source.AbstractFilterSave;
import com.nh.crawler.source.AmazonImp;
import com.nh.crawler.source.ExtractElementI;
import com.nh.crawler.source.JindongImp;
import com.nh.crawler.source.TianmaoImp;
import com.nh.crawler.source.YhdImp;

public class FactoryInstance {
	
	public static AbstractFilterSave getInstatnce(String source){
		
		if(source.equals("京东")){
			return new JindongImp();
		}else if(source.equals("一号店")){
			return new YhdImp();
		}else if(source.equals("亚马逊")){
			return new AmazonImp();
		}else if(source.equals("天猫")){
			return new TianmaoImp();
		}
		
		return null;
	}

}
