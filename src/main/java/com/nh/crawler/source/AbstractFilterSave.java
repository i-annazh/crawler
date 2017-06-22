package com.nh.crawler.source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.nh.crawler.db.Db;
import com.nh.crawler.until.JacksonUtils;

public abstract class AbstractFilterSave implements ExtractElementI{

	public void saveNavigation(List<Map<String, String>> list, String source){
		
		for(Map<String, String> map:list){
			
			if(!map.containsKey("category")){
				continue;
			}
			
			String category = map.get("category");
			String url = map.get("url");
			
			if(Db.isExistDbNav(source, url)){
				continue;
			}
			
			int time = (int)(System.currentTimeMillis()/1000);
			
			Db.insertIntoDbNav(source, category, url, 0, time, time);
		}
		
		return;
	}
	
	//更新navigate
	public void updateNavigation(String category){
		
		//根据类目获取导航栏 的
		String urlDes = "";
		List<Map<String, String>> list = extractNavigationBar();
		
		for(Map<String, String> map:list){
			String tempcate = map.get("category");
			
			if(tempcate.equals(category)){
				urlDes = map.get("url");
				break;
			}
		}
		
		if(StringUtils.isBlank(urlDes)){
			return;
		}
		
		System.out.println("find ");
		
		//查询数据库中的url
		String oldurl = Db.getCategoryUrl(getSourceNname(), category);
		
		if(oldurl.equals(urlDes)){
			System.out.println("nop change");
			return;
		}
		
		//发动跟新
		Db.updateDbNavUrl(oldurl, urlDes);
		Db.updateGoodDetailUrl(oldurl, urlDes);
		
	}
	
	public void saveItem(List<Map<String, String>> list, String source){
		
		
		for(Map<String, String> map:list){
			
			
			String score = map.get("itemScore");
			if(StringUtils.isBlank(score)){
				// 当详情页获取出现问题 比如网络缘故 我们默认为得分0
				score = "0";
			}
			
			String itemName = map.get("itemName");
			String itemUrl = map.get("itemUrl");
			//String itemSku = map.get("itemSku");
			String itemPrice = map.get("itemPrice");
			String itemFrom = map.get("itemFrom");	
			String img_url = map.get("itemPic");
			String itemKey = getItemKey(itemUrl);
			
			if(StringUtils.isBlank(itemKey)){
				itemKey = "0";
			}
			
			String merchantName = "";
			if(!StringUtils.isBlank(map.get("itemMerchant"))){
				merchantName = map.get("itemMerchant");
			}
			
			String id = "";
			if(map.containsKey("itemId"))
				id = map.get("itemId");
			
			if(id.equals("0") || id.equals("")){
				// page属于扩展字段
				Map<String, Object> extendMap = new HashMap<String, Object>();
				
				if(map.containsKey("page")){
					extendMap.put("page", map.get("page"));
				}
				
				//把pagemap转成String存储到itemExtend字段中
				String itemExtend = JacksonUtils.getJsonString(extendMap);
				
				int time = (int)(System.currentTimeMillis()/1000);
				
				//判断url是否存在
				long itemKeyValue = Db.isExistDbItemUrl("", itemKey);
				if(itemKeyValue > 0L){
					id = String.valueOf(itemKeyValue);
				}else{
					long tempid = 0L;
					try {
						
						tempid = Db.insertIntoDbItem(itemName, Float.parseFloat(itemPrice), merchantName, itemUrl, itemExtend, 0, time, time, itemFrom, Integer.parseInt(score), itemKey, img_url);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						System.out.println(e);
						continue;
					}
					id = String.valueOf(tempid);
				}
			}
			
			if(id.equals("")){
				continue;
			}
			
			if(StringUtils.isBlank(map.get("itemAll"))){
				continue;
			}
			
			Db.insertIntoDbTrend(Integer.parseInt(id),
					Integer.parseInt(map.get("itemAll")),
					Float.parseFloat(map.get("itemRate")),
					Integer.parseInt(map.get("itemGood")),
					Integer.parseInt(map.get("itemMid")),
					Integer.parseInt(map.get("itemBad")),
					Float.parseFloat(map.get("itemPrice")), 
					(int)(System.currentTimeMillis()/1000));
			
		}
		
		
		return;
	}
	
	public void setScore(List<Map<String, String>> list){
		
		for(Map<String, String> map:list){
			
			String itemUrl = map.get("itemUrl");
			
			int id = Db.getUrlId(itemUrl);
			int time = 0;
			
			if(id > 0){
				time = Db.getCreateTime(id);
			}else{
				time = (int)(System.currentTimeMillis()/1000);
			}
			
			Map<String, String> resultMap = null;
			try {
				resultMap = extractItemPage(itemUrl);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				continue;
			}
			if(null == resultMap){
				// 走入这个分支 得分就没有
				continue;
			}
			try {
				Thread.sleep((1 + new java.util.Random().nextInt(3))*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
			String rate = resultMap.get("rate");
			String allnum = resultMap.get("allnum");
			String goodnum = resultMap.get("goodnum");
			String midnum = resultMap.get("midnum");
			String badnum = resultMap.get("badnum");
			int score = getScore(Double.parseDouble(rate), Integer.parseInt(allnum),
					Integer.parseInt(goodnum),
					Integer.parseInt(midnum),
					Integer.parseInt(badnum),
					time);
			
			map.put("itemScore", String.valueOf(score));
			map.put("itemRate", rate);
			map.put("itemAll", allnum);
			map.put("itemGood", goodnum);			
			map.put("itemMid", midnum);
			map.put("itemBad", badnum);
			map.put("itemId", String.valueOf(id));
			
		}
		
		return;
	}
	//Db.updateScoreSphinx(id, score);
	//Db.updateScoreDb(id, score);
	public int getScore(double rate, int commentCount, int goodnum, int midnum, int badnumm, int time){
		int score = 0;
		if(rate > 0.95f){
			score = score + 1;
		}
		
		if(commentCount > 5000){
			score = score + 1;
		}
		
		if(commentCount > 20000){
			score = score + 1;
		}
		
		if( ((int)(System.currentTimeMillis()/1000) - time ) < 60*60*24*30){
			score = score + 1;
		}
		
		if(badnumm > 20 && score >0){
			score = score -1;
		}
		
		return score;
	}

}
