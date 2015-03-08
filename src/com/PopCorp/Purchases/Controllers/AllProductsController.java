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
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Activities.AllProductsActivity;
import com.PopCorp.Purchases.Adapters.AllProductsAdapter;
import com.PopCorp.Purchases.Data.Product;
import com.PopCorp.Purchases.DataBase.DB;
import com.PopCorp.Purchases.Loaders.ProductsLoader;

public class AllProductsController implements LoaderCallbacks<Cursor>{
	
	public static final int ID_FOR_CREATE_LOADER_FROM_DB = 1;
	
	private SharedPreferences sPref;
	
	private AllProductsActivity context;
	private DB db;
	private AllProductsAdapter adapter;
	private ArrayList<Product> items;
	private Product editedProduct;
	
	public AllProductsController(AllProductsActivity context){
		this.context = context;
		sPref = PreferenceManager.getDefaultSharedPreferences(context);

		db = new DB(context);
		openDB();
		
		items = new ArrayList<Product>();
		adapter = new AllProductsAdapter(context, items);
	}
	
	
	public void showPopupMenu(View view, final int position) {
		PopupMenu popupMenu = new PopupMenu(context, view);
		popupMenu.inflate(R.menu.popup_menu_for_product);
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.action_edit_product :{
					context.putProductInFields(items.get(position));
					editedProduct = items.get(position);
					return true;
				}
				case R.id.action_remove_product:{
					removeProduct(position);
					return true;
				}
				default:
					return false;
				}
			}
		});

		popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
			@Override
			public void onDismiss(PopupMenu menu) {

			}
		});
		popupMenu.show();
	}


	private void removeProduct(int position) {
		items.get(position).remove(db);
		items.remove(position);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> result = null;
		if (id == ID_FOR_CREATE_LOADER_FROM_DB){
			result = new ProductsLoader(context, db);
		}
		return result;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor==null){
			//no items
		}
		if (cursor.moveToFirst()){
			addProductFromCursor(cursor);
			while (cursor.moveToNext()){
				addProductFromCursor(cursor);
			}
		}
		cursor.close();
		refreshAdapter();
	}

	private void addProductFromCursor(Cursor cursor) {
		items.add(new Product(cursor));
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
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

	public ArrayList<Product> getItems() {
		return items;
	}

	public AllProductsAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(AllProductsAdapter adapter) {
		this.adapter = adapter;
	}


	public void addNewProduct(String name, String count, String edizm, String coast, String category, String shop, String comment) {
		if (editedProduct!=null){
			editedProduct.update(db, name, count, edizm, coast, category, shop, comment);
			editedProduct = null;
			refreshAdapter();
			return;
		}
		Product newProduct = new Product(0, name, count, edizm, coast, category, shop, comment);
		long id = db.addRec(DB.TABLE_ALL_ITEMS, DB.COLUMNS_ALL_ITEMS, newProduct.getFields());
		newProduct.setId(id);
		items.add(newProduct);
		refreshAdapter();
	}


	private void refreshAdapter() {
		Collections.sort(items, new SortOnlyNames());
		adapter.notifyDataSetChanged();
	}
	
	
	private class SortOnlyCategories implements Comparator<Product>
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

		public int compare(Product p1, Product p2)
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
	
	private class SortOnlyNames implements Comparator<Product>
	{
		public int compare(Product p1, Product p2)
		{
			return p1.getName().compareToIgnoreCase(p2.getName());
		}
	}

	public Product getEditedProduct() {
		return editedProduct;
	}

}
