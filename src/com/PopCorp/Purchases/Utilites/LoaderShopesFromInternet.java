package com.PopCorp.Purchases.Utilites;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.PopCorp.Purchases.Data.Shop;

public class LoaderShopesFromInternet {
	
	public static String getFirstPage(String city){
		InternetConnection connection = null;
		StringBuilder page = null;
		try{
			connection = new InternetConnection("http://mestoskidki.ru/view_rating.php?city=" + city);
			page = connection.getPageInStringBuilder();
		} catch(IOException e) {
			return null;
		} finally {
			if (connection!=null){
				connection.disconnect();
			}
		}
		return page.toString();
	}
	
	public static ArrayList<Shop> getLinksForShops(String page){//
		Matcher matcherBegin = Pattern.compile("a href=[.[^&]]+&shop=[0-9]+\' class='left_links2'>[.[^<]]+").matcher(page);
		ArrayList<Shop> shops = new ArrayList<Shop>();
		while (matcherBegin.find()) {
			String tmpString = matcherBegin.group();
			String keyShop = "";
			Matcher matcherBegin1 = Pattern.compile("shop=[0-9]+").matcher(tmpString);
			if (matcherBegin1.find()) {
				keyShop = matcherBegin1.group().substring(5);
			}
			Matcher matcherBegin2 = Pattern.compile(">[.[^(]]+").matcher(tmpString);
			if (matcherBegin2.find()) {
				String name = matcherBegin2.group().substring(1, matcherBegin2.group().length()-1);
				shops.add(new Shop(keyShop, name));
			}
		}
		return shops;
	}

}
