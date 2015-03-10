package com.PopCorp.Purchases.Loaders;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;

import com.PopCorp.Purchases.Data.Sale;
import com.PopCorp.Purchases.Data.Shop;
import com.PopCorp.Purchases.Utilites.InternetConnection;

public class SalesInternetLoader extends AsyncTaskLoader<ArrayList<Sale>> {

	private Shop shop;
	private String cityId;
	private Context context;
	private ArrayList<ArrayList<String>> prodCats;
	private ArrayList<ArrayList<String>> promCats;

	public SalesInternetLoader(Context ctx, Bundle args, Shop shop) {
		super(ctx);
		context = ctx;
		this.shop = shop;
		cityId = PreferenceManager.getDefaultSharedPreferences(context).getString("city", "1");
	}

	@Override
	public ArrayList<Sale> loadInBackground() {
		ArrayList<Sale> result = new ArrayList<Sale>();
		int countOfPages = getPageCount();

		InternetConnection connection = null;
		StringBuilder allpage = null;
		ArrayList<String> linksSale = new ArrayList<String>();

		for (int page = 1; page < countOfPages + 1; page++) {
			try {
				connection = new InternetConnection("http://mestoskidki.ru/view_shop.php?city=" + cityId + "&shop=" + shop.getId() + "&page=" + String.valueOf(page));
				allpage = connection.getPageInStringBuilder();
			} catch(IOException e){
				//show error: no internet
				return null;
			} finally{
				if (connection!=null){
					connection.disconnect();
				}
			}
			Matcher matcherForLinkSale = Pattern.compile("&id=[.[^\']]+\'").matcher(allpage.toString());
			while (matcherForLinkSale.find()) {// записываем ссылки
				linksSale.add(matcherForLinkSale.group().substring(4, matcherForLinkSale.group().length()-1));
			}
		}

		prodCats = getCats("1");
		promCats = getCats("2");

		for (int pageSale = 0; pageSale < linksSale.size(); pageSale++) {// с каждой страницы акции сохраняем данные
			try {
				connection = new InternetConnection("http://mestoskidki.ru/view_sale.php?city=" + cityId + "&id=" + String.valueOf(linksSale.get(pageSale)));
				allpage = connection.getPageInStringBuilder();
			} catch(IOException e){
				continue;
			} finally{
				if (connection!=null){ 
					connection.disconnect();
				}
			}
			
			String title = getTitle(allpage.toString());
			if (title==null){
				continue;
			}
			
			String subTitle = getSubTitle(allpage.toString());
			if (subTitle==null){
				continue;
			}
			
			String coast = getCoast(allpage.toString());
			if (coast==null){
				continue;
			}

			String count = getCount(allpage.toString());
			if (count==null){
				continue;
			}
			
			String coastFor = getCoastFor(allpage.toString());
			if (coastFor==null){
				continue;
			}
			
			String imageUrl = getImageUrl(allpage.toString());
			if (imageUrl==null){
				continue;
			}
			
			String imageId = getImageId(allpage.toString());
			if (imageId==null){
				continue;
			}
			
			String category = getCategory(allpage.toString());
			if (category==null){
				continue;
			}

			String[] period = getPeriod(allpage.toString());
			if (period==null){
				continue;
			}
			
			Sale sale = new Sale(-1, linksSale.get(pageSale), title, subTitle, coast, count, coastFor, imageUrl, imageId, shop.getId(), category, period[0], period[1]);
			result.add(sale);
		}
		return result;
	}

	private int getPageCount(){
		InternetConnection connection = null;
		StringBuilder allpage = null;
		try {
			connection = new InternetConnection("http://mestoskidki.ru/view_shop.php?city=" + cityId + "&shop=" + shop.getId());
			allpage = connection.getPageInStringBuilder();
		} catch(IOException e){
			return 1;
		} finally{
			if (connection!=null){
				connection.disconnect();
			}
		}

		Matcher matcher = Pattern.compile("([0-9]+)><b>&#062;&#062;").matcher(allpage.toString());
		if (matcher.find()) {// если нашли кол-во страниц
			Matcher matcher1 = Pattern.compile("[.[^>]]+").matcher(matcher.group());
			if (matcher1.find()) {// если нашли кол-во страниц
				return Integer.valueOf(matcher1.group());// нашли количество страниц
			}
		}
		return 1;
	}

	private String getCategory(String page){
		boolean prod = true;
		String category = null;
		Matcher matcher = Pattern.compile("view_cat[.]php[?]city=[0-9]+&cat[0-9=]+").matcher(page);
		if (matcher.find()) {
			String[] m = matcher.group().split("=");
			int size = m.length;
			String d = m[size-2].substring(m[size-2].length()-1);
			if (d.equals("2")){
				prod = false;
			}
			category = m[m.length-1];
		}
		if (category!=null){//проверка принадлежности акции
			if (prod){
				if (category.length()>2){
					for (int prodCat=0; prodCat<prodCats.size(); prodCat++){
						if ((prodCats.get(prodCat).contains(category)) || (category.equals(String.valueOf(prodCat+1)))){
							category = String.valueOf(prodCat+1);
							return category;
						}
					}
				}
			}else{
				if (category.length()>2){
					for (int promCat=0; promCat<promCats.size(); promCat++){
						if ((promCats.get(promCat).contains(category)) || (category.equals(String.valueOf(promCat+1)))){
							category = String.valueOf(promCat+1);
							return category;
						}
					}
				}
			}
		}
		return null;
	}


	private ArrayList<ArrayList<String>> getCats(String categoryId){
		InternetConnection connection = null;
		StringBuilder allpage = null;
		ArrayList<ArrayList<String>> categories = new ArrayList<ArrayList<String>>();
		try {
			connection = new InternetConnection("http://mestoskidki.ru/cat_sale.php?city=" + cityId + "&catid=" + categoryId);
			allpage = connection.getPageInStringBuilder();
		} catch (MalformedURLException e) {
			return categories;
		} catch (IOException e) {
			return categories;
		} finally{
			if (connection!=null){
				connection.disconnect();
			}
		}

		if (categoryId.equals("1")){
			categoryId = "";
		}
		Matcher matcher = Pattern.compile("view_cat[.]php[?]city=[0-9]+&cat" + categoryId + "=[0-9]+").matcher(allpage.toString());
		int i=-1;
		while (matcher.find()) {
			String[] m = matcher.group().split("=");
			String categ = m[m.length-1];
			if (categ.length()<3){
				i++;
				if (Integer.valueOf(categ)<(i+1)){
					break;
				}
				categories.add(new ArrayList<String>());
			}else{
				categories.get(i).add(categ);
			}
		}
		return categories;
	}

	private String[] getPeriod(String page){
		String[] result = new String[2];
		Matcher matcher = Pattern.compile("Период акции:<br><br><font color='red'>[.[^<]]+").matcher(page);
		if (matcher.find()) {// записываем период
			String period = matcher.group().substring(39, matcher.group().length());
			if (period.length()==10){
				result[0] = period;
				result[1] = period;
			} else{
				result[0] = period.substring(0, 10);
				result[1] = period.substring(13);
			}
			return result;
		}

		Matcher matcher1 = Pattern.compile("Период акции:<br><br>[.[^<]]+").matcher(page);
		if (matcher1.find()) {// записываем период, если красным шрифтом
			String period = matcher1.group().substring(21, matcher1.group().length());
			if (period.length()==10){
				result[0] = period;
				result[1] = period;
			} else{
				result[0] = period.substring(0, 10);
				result[1] = period.substring(13);
			}
			return result;
		}
		return null;
	}

	private String getCoast(String page){
		Matcher matcher = Pattern.compile("Цена: [.[^<]]+").matcher(page);
		if (matcher.find()) {// записываем цену
			return matcher.group().substring(6);
		}
		return null;
	}
	
	private String getCount(String page){
		Matcher matcher = Pattern.compile("Вес: [.[^<]]+").matcher(page);
		if (matcher.find()) {// вес
			return matcher.group().substring(5);
		}
		return "";
	}
	
	private String getCoastFor(String page){
		Matcher matcher = Pattern.compile("Цена за [.[^:]]+: [.[^<]]+").matcher(page);
		if (matcher.find()) {// записываем цену
			Matcher matcher2 = Pattern.compile(": [.[^<]]+").matcher(matcher.group());
			if (matcher2.find()) {// записываем цену
				return matcher2.group().substring(2);
			}
		}
		return "";
	}

	private String getTitle(String page){
		String title = null;
		Matcher matcher = Pattern.compile("<p class='larger'><strong>[.[^<]]+").matcher(page);
		if (matcher.find()) {// записываем имя
			title = matcher.group().substring(26).trim();
		}
		return title;
	}
	
	private String getSubTitle(String page){
		String subTitle = "";
		Matcher matcher = Pattern.compile("<p class='larger'><strong>[.]+</strong><br>[.[^<]]+").matcher(page);
		if (matcher.find()) {// записываем имя
			Matcher matcher2 = Pattern.compile("<br>[.[^<]]+").matcher(matcher.group());
			if (matcher2.find()) {// записываем имя
				subTitle=matcher2.group().substring(4).trim();
			}
		}
		return subTitle;
	}
	
	private String getImageId(String page){
		String imageId = null;
		Matcher matcher = Pattern.compile("src='http://mestoskidki.ru/skidki/[.[^[.jpg]]]+.jpg").matcher(page);
		if (matcher.find()) {// записываем имя
			String finded = matcher.group();
			imageId = finded.substring(finded.length()-10, finded.length()-4);
		}
		return imageId;
	}

	private String getImageUrl(String page){
		String imageUrl = null;
		Matcher matcher = Pattern.compile("src='http://mestoskidki.ru/skidki/[.[^[.jpg]]]+.jpg").matcher(page);
		if (matcher.find()) {// записываем имя
			imageUrl = matcher.group().substring(5);
		}
		return imageUrl;
	}
}
