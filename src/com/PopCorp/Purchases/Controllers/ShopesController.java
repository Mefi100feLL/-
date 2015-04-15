package com.PopCorp.Purchases.Controllers;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Adapters.ShopesAdapter;
import com.PopCorp.Purchases.Data.Shop;
import com.PopCorp.Purchases.DataBase.DB;
import com.PopCorp.Purchases.Fragments.SalesFragment;
import com.PopCorp.Purchases.Fragments.ShopesFragment;
import com.PopCorp.Purchases.Loaders.ShopesLoader;

public class ShopesController implements LoaderCallbacks<Cursor>{

	public static final int ID_FOR_CREATE_SHOPES_LOADER_FROM_DB = 3;
	public static final int ID_FOR_CREATE_SHOPES_LOADER_FROM_NET = 4;

	private ShopesFragment fragment;
	private ActionBarActivity context;
	private DB db;
	private SharedPreferences sPref;

	private ShopesAdapter adapter;
	private ArrayList<Shop> shopes = new ArrayList<Shop>();

	public ShopesController(ShopesFragment fragment, ActionBarActivity context){
		this.fragment = fragment;
		this.context = context;
		sPref = PreferenceManager.getDefaultSharedPreferences(context);

		db = new DB(context);
		openDB();

		adapter = new ShopesAdapter(shopes, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> result = null;
		if (id == ID_FOR_CREATE_SHOPES_LOADER_FROM_DB){
			result = new ShopesLoader(context, db, sPref.getString("city", "1"));
		}
		return result;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor!=null){
			if (cursor.moveToFirst()){
				addShopFromCursor(cursor);
				while (cursor.moveToNext()){
					addShopFromCursor(cursor);
				}
			}
			cursor.close();
		}
		if (shopes.size()>0){
			fragment.showProgress(false);
		} else{
			fragment.showProgress(true);
		}
		adapter.notifyDataSetChanged();
	}
	
	private void addShopFromCursor(Cursor cursor) {
		Shop newShop = new Shop(cursor);
		if (!shopes.contains(newShop)){
			shopes.add(newShop);
		}
	}

	public void updateShopes(ArrayList<Shop> arrayWithNewShopesFromNet) {
		for (int i=0; i<arrayWithNewShopesFromNet.size(); i++){
			if (!shopes.contains(arrayWithNewShopesFromNet.get(i))){
				arrayWithNewShopesFromNet.get(i).putInDB(db, sPref.getString("city", "1"));
				shopes.add(arrayWithNewShopesFromNet.get(i));
			}
		}
		if (shopes.size()>0){
			fragment.showProgress(false);
		}
		adapter.notifyDataSetChanged();
	}
	
	public void openShop(int position){
		Fragment fragment = new SalesFragment();
		Bundle args = new Bundle();
		args.putString(SalesFragment.CURRENT_SHOP_ID_TO_SALES_FRAGMENT, shopes.get(position).getId());
		args.putString(SalesFragment.CURRENT_SHOP_NAME_TO_SALES_FRAGMENT, shopes.get(position).getName());
		fragment.setArguments(args);
		FragmentManager fragmentManager = context.getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.content_frame, fragment, SalesFragment.TAG).commit();
	}

	/////////////////////////////////////////////////////////////////////////
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
	public ShopesAdapter getAdapter() {
		return adapter;
	}

}
