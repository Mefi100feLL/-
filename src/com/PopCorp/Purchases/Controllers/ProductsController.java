package com.PopCorp.Purchases.Controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.CheckBox;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Adapters.ProductsAdapter;
import com.PopCorp.Purchases.Data.ListItem;
import com.PopCorp.Purchases.Data.Product;
import com.PopCorp.Purchases.DataBase.DB;
import com.PopCorp.Purchases.Loaders.ProductsLoader;

public class ProductsController implements LoaderCallbacks<Cursor>{
	
	public static final int ID_FOR_CREATE_LOADER_FROM_DB = 1;
	
	private Context activity;
	private SharedPreferences sPref;
	private DB db;
	private ProductsAdapter adapter;
	private ArrayList<ListItem> items;
	private ArrayList<String> categories;
	private ArrayList<Integer> colors;
	
	public ProductsController(Context context, ArrayList<ListItem> itemsInList){
		this.activity = context;
		sPref = PreferenceManager.getDefaultSharedPreferences(context);
		this.items = itemsInList;
		db = new DB(activity);
		openDB();
		for (ListItem item : items){
			item.setSelected(true);
		}
		categories = getCategories();
		colors = getColors();
		adapter = new ProductsAdapter(activity, items, categories, colors);
	}
	
	public ArrayList<ListItem> apply(){
		ArrayList<ListItem> result = new ArrayList<ListItem>();
		for (ListItem item : items){
			if (item.isSelected()){
				result.add(item);
			}
		}
		return result;
	}
	
	public void changeItemSelection(View view, int position) {
		if (items.get(position).isSelected()){
			//view.findViewById(R.id.item_product_count_layout).setVisibility(View.GONE);
			((CheckBox) view.findViewById(R.id.item_product_in_products_checkbox)).setChecked(false);
			items.get(position).setSelected(false);
		} else {
			//view.findViewById(R.id.item_product_count_layout).setVisibility(View.VISIBLE);
			((CheckBox) view.findViewById(R.id.item_product_in_products_checkbox)).setChecked(true);
			items.get(position).setSelected(true);
		}
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> result = null;
		if (id == ID_FOR_CREATE_LOADER_FROM_DB){
			result = new ProductsLoader(activity, db);
		}
		return result;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor==null){
			//no items
		}
		if (cursor.moveToFirst()){
			addListItemAllFromCursor(cursor);
			while (cursor.moveToNext()){
				addListItemAllFromCursor(cursor);
			}
		}
		cursor.close();
		Collections.sort(items, new SortOnlyNames());
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}
	
	private void addListItemAllFromCursor(Cursor cursor){
		String name = cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_NAME));
		for (ListItem item : items){
			if (item.getName().equals(name)){
				item.setSelected(true);
				return;
			}
		}
		ListItem newProduct = new ListItem(-1, "",
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_NAME)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_COUNT)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_EDIZM)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_COAST)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_CATEGORY)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_SHOP)),
				cursor.getString(cursor.getColumnIndex(DB.KEY_ALL_ITEMS_COMMENT)),
				"false",
				"false");
		items.add(newProduct);
	}
	
	
	public void closeDB(){
		if (db!=null){
			if (!db.isClosed()){
				db.close();
			}
		}
	}
	
	public void openDB(){
		if (db!=null){
			if (db.isClosed()){
				db.open();
			}
		}
	}

	public ArrayList<ListItem> getItems() {
		return items;
	}

	public ProductsAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(ProductsAdapter adapter) {
		this.adapter = adapter;
	}

	public void sort(int itemId) {
		switch (itemId){
		case R.id.action_sort_by_abc:{
			Collections.sort(items, new SortOnlyNames());
			break;
		}
		case R.id.action_sort_by_category:{
			Collections.sort(items, new SortOnlyCategories(activity));
			break;
		}
		case R.id.action_sort_by_favorite:{
			break;
		}
		}
	}
	
	private class SortOnlyCategories implements Comparator<ListItem>
	{
		private ArrayList<String> categories;

		public SortOnlyCategories(Context context){
			categories = new ArrayList<String>();
			Set<String> s = sPref.getStringSet("categories", new LinkedHashSet<String>());
			for (String i:new ArrayList<String>(s)){
				for (int y=0;y<i.length();y++){
					if (i.charAt(y)=='!'){
						categories.add(i.substring(y+1,i.length()));
					}
				}
			}
		}

		public int compare(ListItem p1, ListItem p2)
		{
			if ((p1.getCategory().equals(p2.getCategory()))){
				return p1.getName().compareToIgnoreCase(p2.getName());
			}else{
				if (categories.contains(p1.getCategory()) && categories.contains(p2.getCategory())){
					if (categories.indexOf(p1.getCategory()) > categories.indexOf(p2.getCategory())){
						return 1;
					}else{
						return -1;
					}
				}else if (categories.contains(p1.getCategory())){
					return 1;
				}else if (categories.contains(p2.getCategory())){
					return -1;
				}else{
					return p1.getCategory().compareToIgnoreCase(p2.getCategory());
				}
			}
		}
	}
	
	private class SortOnlyNames implements Comparator<ListItem>
	{
		public int compare(ListItem p1, ListItem p2)
		{
			return p1.getName().compareToIgnoreCase(p2.getName());
		}
	}
	
	public ArrayList<String> getCategories() {
		ArrayList<String> result = new ArrayList<String>();
		Cursor cursor = db.getAllData(DB.TABLE_CATEGORIES);
		if (cursor!=null){
			if (cursor.moveToFirst()){
				result.add(cursor.getString(cursor.getColumnIndex(DB.KEY_CATEGS_NAME)));
				while (cursor.moveToNext()){
					result.add(cursor.getString(cursor.getColumnIndex(DB.KEY_CATEGS_NAME)));
				}
			}
			cursor.close();
		}
		result.add(activity.getString(R.string.string_no_category));
		return result;
	}

	public ArrayList<Integer> getColors() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		Cursor cursor = db.getAllData(DB.TABLE_CATEGORIES);
		if (cursor!=null){
			if (cursor.moveToFirst()){
				result.add(cursor.getInt(cursor.getColumnIndex(DB.KEY_CATEGS_COLOR)));
				while (cursor.moveToNext()){
					result.add(cursor.getInt(cursor.getColumnIndex(DB.KEY_CATEGS_COLOR)));
				}
			}
			cursor.close();
		}
		result.add(activity.getResources().getColor(android.R.color.transparent));
		return result;
	}
}
