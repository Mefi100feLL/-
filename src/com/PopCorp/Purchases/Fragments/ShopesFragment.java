package com.PopCorp.Purchases.Fragments;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Controllers.ShopesController;
import com.PopCorp.Purchases.Data.Shop;
import com.PopCorp.Purchases.Loaders.ShopesInternetLoader;

public class ShopesFragment extends Fragment {
	
public static final String TAG = ShopesFragment.class.getSimpleName();
	
	private RecyclerView gridView;
	private ProgressBar progress;

	private ShopesController controller;
	private SharedPreferences sPref;

	private AppCompatActivity context;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_shopes, container, false);
		context = (AppCompatActivity) getActivity();
		
		sPref = PreferenceManager.getDefaultSharedPreferences(context);
		
		gridView = (RecyclerView) rootView.findViewById(R.id.fragment_shopes_gridview);
		progress = (ProgressBar) rootView.findViewById(R.id.fragment_shopes_progress);
		
		controller = new ShopesController(this, context);
		
		StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
		layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
		
		gridView.setLayoutManager(layoutManager);
		gridView.setAdapter(controller.getAdapter());
		RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
		gridView.setItemAnimator(itemAnimator);
		
		getLoaderManager().initLoader(ShopesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_DB, new Bundle(), controller);
		Loader<Cursor> shopesLoaderFromDB = getLoaderManager().getLoader(ShopesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_DB);
		shopesLoaderFromDB.forceLoad();

		getLoaderManager().initLoader(ShopesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_NET, new Bundle(), new CallBackForShopesFromNet());
		Loader<ArrayList<Shop>> shopesLoaderFromNET = getLoaderManager().getLoader(ShopesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_NET);
		shopesLoaderFromNET.forceLoad();
		
		return rootView;
	}
	
	public void showProgress(boolean show){
		progress.setVisibility(show ? View.VISIBLE : View.GONE);
		gridView.setVisibility(show ? View.GONE : View.VISIBLE);
	}
	
	public class CallBackForShopesFromNet implements LoaderCallbacks<ArrayList<Shop>>{
		@Override
		public Loader<ArrayList<Shop>> onCreateLoader(int id, Bundle args) {
			Loader<ArrayList<Shop>> result = null;
			if (id == ShopesController.ID_FOR_CREATE_SHOPES_LOADER_FROM_NET){
				result = new ShopesInternetLoader(context, args, sPref.getString("city", "1"));
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
