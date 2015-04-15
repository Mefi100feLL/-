package com.PopCorp.Purchases.Controllers;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import com.PopCorp.Purchases.Adapters.SalesAdapter;
import com.PopCorp.Purchases.Data.Sale;
import com.PopCorp.Purchases.Data.Shop;
import com.PopCorp.Purchases.DataBase.DB;
import com.PopCorp.Purchases.Fragments.SalesFragment;
import com.PopCorp.Purchases.Loaders.SalesLoader;

public class SalesController implements LoaderCallbacks<Cursor>{

	public static final int ID_FOR_CREATE_SALES_LOADER_FROM_DB = 1;
	public static final int ID_FOR_CREATE_SALES_LOADER_FROM_NET = 2;

	public static final int ID_FOR_CREATE_SHOPES_LOADER_FROM_DB = 3;
	public static final int ID_FOR_CREATE_SHOPES_LOADER_FROM_NET = 4;

	private SalesFragment fragment;
	private Context context;
	private DB db;
	private SharedPreferences sPref;

	private SalesAdapter adapter;
	private ArrayList<Sale> sales = new ArrayList<Sale>();

	private String currentShop;

	public SalesController(SalesFragment fragment, Context context, String currentShop){
		this.fragment = fragment;
		this.context = context;
		this.currentShop = currentShop;
		sPref = PreferenceManager.getDefaultSharedPreferences(context);

		db = new DB(context);
		openDB();

		adapter = new SalesAdapter(context, sales);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> result = null;
		if (id == ID_FOR_CREATE_SALES_LOADER_FROM_DB){
			result = new SalesLoader(context, db, currentShop);
		}
		return result;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
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
		if (sales.size()>0){
			fragment.showProgress(false);
		} else{
			fragment.showProgress(true);
		}
		refreshAll();
	}
	
	private void addSaleFromCusror(Cursor cursor){
		Sale newSale = new Sale(cursor);
		if (isSaleActual(newSale)){
			if (!sales.contains(newSale)){
				sales.add(newSale);
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
		if (sales.size()>0){
			fragment.showProgress(false);
		}
		refreshAll();
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
	public String getCurrentShop() {
		return currentShop;
	}

	public SalesAdapter getAdapter() {
		return adapter;
	}
}
