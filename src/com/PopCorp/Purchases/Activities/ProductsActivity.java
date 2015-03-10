package com.PopCorp.Purchases.Activities;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Controllers.MenuController;
import com.PopCorp.Purchases.Controllers.ProductsController;
import com.PopCorp.Purchases.Data.Product;
import com.PopCorp.Purchases.Fragments.ListFragment;
import com.shamanland.fab.FloatingActionButton;

public class ProductsActivity extends ActionBarActivity{

	public static final String INTENT_TO_PRODUCTS_LISTITEMS = "array";
	
	private ListView listView;
	private FloatingActionButton floatingButton;
	private Toolbar toolBar;
	
	private ProductsController controller;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_products);
		
		toolBar = (Toolbar) findViewById(R.id.activity_products_toolbar);
	    setSupportActionBar(toolBar);
	    getSupportActionBar().setHomeButtonEnabled(true);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		listView = (ListView) findViewById(R.id.activity_products_listview);
		floatingButton = (FloatingActionButton) findViewById(R.id.activity_products_floating_action_button);
		floatingButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				getBack();
			}
		});
		
		ArrayList<Product> array = getIntent().getParcelableArrayListExtra(INTENT_TO_PRODUCTS_LISTITEMS);
		controller = new ProductsController(this, array);
		
		listView.setAdapter(controller.getAdapter());
		
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				controller.changeItemSelection(view, position);
			}
		});
		
		getSupportLoaderManager().initLoader(MenuController.ID_FOR_CREATE_LOADER_FROM_DB, new Bundle(), controller);

		Loader<Cursor> loaderFromDB = getSupportLoaderManager().getLoader(MenuController.ID_FOR_CREATE_LOADER_FROM_DB);
		loaderFromDB.forceLoad();
	}
	
	private void getBack(){
		Intent intent = new Intent();
		intent.putParcelableArrayListExtra(ListFragment.INTENT_TO_LIST_RETURNED_LISTITEMS, controller.apply());
		setResult(RESULT_OK, intent);
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_for_products, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case android.R.id.home:{
			finish();
			break;
		}
		default:{
			item.setChecked(true);
			controller.sort(item.getItemId());
		}
		}
		
		return super.onOptionsItemSelected(item);
	}
}
