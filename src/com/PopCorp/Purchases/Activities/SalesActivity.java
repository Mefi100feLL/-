package com.PopCorp.Purchases.Activities;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Controllers.SalesController;
import com.PopCorp.Purchases.Data.Sale;
import com.PopCorp.Purchases.Data.Shop;
import com.PopCorp.Purchases.Loaders.SalesInternetLoader;
import com.PopCorp.Purchases.Loaders.ShopesInternetLoader;

public class SalesActivity extends FragmentActivity implements LoaderCallbacks<ArrayList<Sale>>{

	private GridView gridView;

	private SalesController controller;
	private SharedPreferences sPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sales);

		sPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		gridView = (GridView) findViewById(R.id.activity_sales_gridview);
		
		controller = new SalesController(this);
		
		gridView.setAdapter(controller.getAdapter());
		
		getSupportLoaderManager().initLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB, new Bundle(), controller);

		getSupportLoaderManager().initLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_NET, new Bundle(), this);
		
		getSupportLoaderManager().initLoader(SalesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_DB, new Bundle(), controller);
		Loader<Cursor> shopesLoaderFromDB = getSupportLoaderManager().getLoader(SalesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_DB);
		shopesLoaderFromDB.forceLoad();

		getSupportLoaderManager().initLoader(SalesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_NET, new Bundle(), new CallBackForShopesFromNet());
		Loader<ArrayList<Shop>> shopesLoaderFromNET = getSupportLoaderManager().getLoader(SalesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_NET);
		shopesLoaderFromNET.forceLoad();
	}
	

	public void loadSalesForShop(){
		getSupportLoaderManager().restartLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB, new Bundle(), controller);
		Loader<Cursor> loaderFromDB = getSupportLoaderManager().getLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB);
		loaderFromDB.forceLoad();
		
		getSupportLoaderManager().restartLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_NET, new Bundle(), this);
		Loader<String> loaderFromNet = getSupportLoaderManager().getLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_NET);
		loaderFromNet.forceLoad();
	}


	public void loadShopes() {
		getSupportLoaderManager().restartLoader(SalesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_DB, new Bundle(), controller);
		Loader<Cursor> shopesLoaderFromDB = getSupportLoaderManager().getLoader(SalesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_DB);
		shopesLoaderFromDB.forceLoad();
		
		getSupportLoaderManager().restartLoader(SalesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_NET, new Bundle(), new CallBackForShopesFromNet());
		Loader<ArrayList<Shop>> shopesLoaderFromNET = getSupportLoaderManager().getLoader(SalesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_NET);
		shopesLoaderFromNET.forceLoad();
	}

	@Override
	public Loader<ArrayList<Sale>> onCreateLoader(int id, Bundle args) {
		Loader<ArrayList<Sale>> result = null;
		if (id == SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_NET){
			result = new SalesInternetLoader(this, args, controller.getCurrentShop());
		}
		return result;
	}


	@Override
	public void onLoadFinished(Loader<ArrayList<Sale>> loader, ArrayList<Sale> data) {
		controller.updateSales(data);
	}


	@Override
	public void onLoaderReset(Loader<ArrayList<Sale>> loader) {

	}


	@Override
	protected void onResume(){
		super.onResume();
		controller.openDB();
	}
	

	@Override
	protected void onStop(){
		super.onStop();
		controller.closeDB();
	}
	
	
	public class CallBackForShopesFromNet implements LoaderCallbacks<ArrayList<Shop>>{
		@Override
		public Loader<ArrayList<Shop>> onCreateLoader(int id, Bundle args) {
			Loader<ArrayList<Shop>> result = null;
			if (id == SalesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_NET){
				result = new ShopesInternetLoader(SalesActivity.this, args, sPref.getString("city", "1"));
			}
			return result;
		}

		@Override
		public void onLoadFinished(Loader<ArrayList<Shop>> loader, ArrayList<Shop> shopes) {
			controller.updateShopes(shopes);
		}

		@Override
		public void onLoaderReset(Loader<ArrayList<Shop>> loader) {
			
		}
	}
}
