package com.PopCorp.Purchases.Controllers;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.ArrayAdapter;

import com.PopCorp.Purchases.Activities.SalesActivity;
import com.PopCorp.Purchases.Adapters.SalesAdapter;
import com.PopCorp.Purchases.Data.Sale;
import com.PopCorp.Purchases.Data.Shop;
import com.PopCorp.Purchases.DataBase.DB;
import com.PopCorp.Purchases.Loaders.SalesLoader;
import com.PopCorp.Purchases.Loaders.ShopesLoader;

public class SalesController implements LoaderCallbacks<Cursor>{

	public static final int ID_FOR_CREATE_SALES_LOADER_FROM_DB = 1;
	public static final int ID_FOR_CREATE_SALES_LOADER_FROM_NET = 2;
	
	public static final int ID_FOR_CREATE_SHOPES_LOADER_FROM_DB = 3;
	public static final int ID_FOR_CREATE_SHOPES_LOADER_FROM_NET = 4;

	private SalesActivity context;
	private DB db;
	private SharedPreferences sPref;

	private SalesAdapter adapter;
	private ArrayList<Sale> sales;
	private ArrayList<Shop> shopes;

	private Shop currentShop;

	public SalesController(SalesActivity context){
		this.context = context;
		sPref = PreferenceManager.getDefaultSharedPreferences(context);

		db = new DB(context);
		openDB();

		sales = new ArrayList<Sale>();
		shopes = new ArrayList<Shop>();
		adapter = new SalesAdapter(context, sales);
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> result = null;
		if (id == ID_FOR_CREATE_SALES_LOADER_FROM_DB){
			result = new SalesLoader(context, db, currentShop);
		} 
		if (id == ID_FOR_CREATE_SHOPES_LOADER_FROM_DB){
			result = new ShopesLoader(context, db, sPref.getString("city", "1"));
		}
		return result;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader instanceof SalesLoader){
			sales.clear();
			if (cursor==null){
				//no sales
				return;
			}
			if (cursor.moveToFirst()){
				addSaleFromCusror(cursor);
				while (cursor.moveToNext()){
					addSaleFromCusror(cursor);
				}
			}
			cursor.close();
			refreshAll();
		}
		if (loader instanceof ShopesLoader){
			if (cursor!=null){
				if (cursor.moveToFirst()){
					addShopFromCursor(cursor);
					while (cursor.moveToNext()){
						addShopFromCursor(cursor);
					}
				}
				cursor.close();
			}

			if (shopes.size()==0){
				// no shops, show progress while loading shops
			} else {
				currentShop = shopes.get(0);
				context.loadSalesForShop();
			}
		}
	}
	

	public void updateShopes(ArrayList<Shop> arrayWithNewShopesFromNet) {
		for (int i=0; i<arrayWithNewShopesFromNet.size(); i++){
			if (!shopes.contains(arrayWithNewShopesFromNet.get(i))){
				arrayWithNewShopesFromNet.get(i).putInDB(db, sPref.getString("city", "1"));
				shopes.add(arrayWithNewShopesFromNet.get(i));
			}
		}
		// update spinner with shops
		if (shopes.size()==0){
			// no shopes and no internet
		} else {
			if (currentShop==null){
				currentShop = shopes.get(0);
				context.loadSalesForShop();
			}
		}
	}
	

	public void updateSales(ArrayList<Sale> data) {
		for (Sale sale : data){
			if (!sales.contains(sale)){
				sale.putInDB(db);
				sales.add(sale);
			}
		}
		refreshAll();
	}
	
	
	private void addShopFromCursor(Cursor cursor) {
		Shop newShop = new Shop(cursor);
		if (!shopes.contains(newShop)){
			shopes.add(newShop);
		}
	}
	
	
	private void addSaleFromCusror(Cursor cursor){
		Sale newSale = new Sale(cursor);
		if (isSaleActual(newSale)){
			if (!sales.contains(newSale)){
				sales.add(newSale);
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////
	private void refreshAll() {
		adapter.notifyDataSetChanged();
	}
	
	private boolean isSaleActual(Sale newSale) {
		Calendar dateOfSaleFinish = Calendar.getInstance();
		dateOfSaleFinish.setTime(newSale.getPeriodFinish());
		dateOfSaleFinish.add(Calendar.DAY_OF_MONTH, 1);
		if (dateOfSaleFinish.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()){
			newSale.remove(db);
			return false;
		} else {
			return true;
		}
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
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	/////////////////////////////////////////////// SETTERS AND GETTERS //////////////////////////////////////////////
	public Shop getCurrentShop() {
		return currentShop;
	}
	
	public SalesAdapter getAdapter() {
		return adapter;
	}
}
