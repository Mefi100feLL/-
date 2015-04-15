package com.PopCorp.Purchases.Fragments;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Controllers.SalesController;
import com.PopCorp.Purchases.Data.Sale;
import com.PopCorp.Purchases.Loaders.SalesInternetLoader;

public class SalesFragment extends Fragment {
	
	public static final String TAG = SalesFragment.class.getSimpleName();
	public static final String CURRENT_SHOP_ID_TO_SALES_FRAGMENT = "currentShopID";
	public static final String CURRENT_SHOP_NAME_TO_SALES_FRAGMENT = "currentShopName";
	
	private GridView gridView;
	private ProgressBar progress;
	private Toolbar toolBar;

	private SalesController controller;

	private ActionBarActivity context;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_sales, container, false);
		context = (ActionBarActivity) getActivity();
				
		gridView = (GridView) rootView.findViewById(R.id.fragment_sales_gridview);
		progress = (ProgressBar) rootView.findViewById(R.id.fragment_sales_progress);
		
		String currentShopId = getArguments().getString(CURRENT_SHOP_ID_TO_SALES_FRAGMENT);
		String currentShopName = getArguments().getString(CURRENT_SHOP_NAME_TO_SALES_FRAGMENT);
		
		toolBar = (Toolbar) getActivity().findViewById(R.id.activity_main_toolbar);
		toolBar.setTitle(currentShopName);
		
		controller = new SalesController(this, context, currentShopId);
		
		gridView.setAdapter(controller.getAdapter());
		
		getLoaderManager().initLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB, new Bundle(), controller);
		Loader<Cursor> loaderFromDB = getLoaderManager().getLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB);
		loaderFromDB.forceLoad();
		
		getLoaderManager().initLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_NET, new Bundle(), new SalesLoaderCallbacks());
		Loader<String> loaderFromNet = getLoaderManager().getLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_NET);
		loaderFromNet.forceLoad();
		return rootView;
	}
	
	public void showProgress(boolean show){
		progress.setVisibility(show ? View.VISIBLE : View.GONE);
		gridView.setVisibility(show ? View.GONE : View.VISIBLE);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		controller.openDB();
	}
	
	@Override
	public void onStop(){
		super.onStop();
		controller.closeDB();
	}
	
	public class SalesLoaderCallbacks implements LoaderCallbacks<ArrayList<Sale>>{
		@Override
		public Loader<ArrayList<Sale>> onCreateLoader(int id, Bundle args) {
			Loader<ArrayList<Sale>> result = null;
			if (id == SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_NET){
				result = new SalesInternetLoader(context, args, controller.getCurrentShop());
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
	}

	public void onBackPressed() {
		Fragment fragment = new ShopesFragment();
		String tag = ShopesFragment.TAG;
		toolBar.setTitle(R.string.menu_sales);

		FragmentManager fragmentManager = context.getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, tag).commit();
	}
}
