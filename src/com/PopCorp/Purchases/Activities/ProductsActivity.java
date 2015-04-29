package com.PopCorp.Purchases.Activities;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Controllers.MenuController;
import com.PopCorp.Purchases.Controllers.ProductsController;
import com.PopCorp.Purchases.Data.Product;
import com.PopCorp.Purchases.Fragments.ListFragment;
import com.software.shell.fab.ActionButton;

public class ProductsActivity extends AppCompatActivity{

	public static final String INTENT_TO_PRODUCTS_LISTITEMS = "array";
	
	private RecyclerView listView;
	private ActionButton floatingButton;
	private Toolbar toolBar;
	private ProgressBar progress;
	private TextView textViewEmpty;
	
	private ProductsController controller;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_products);
		toolBar = (Toolbar) findViewById(R.id.activity_products_toolbar);
		progress = (ProgressBar) findViewById(R.id.activity_products_progressbar);
		textViewEmpty = (TextView) findViewById(R.id.activity_products_textview_empty);
	    setSupportActionBar(toolBar);
	    getSupportActionBar().setHomeButtonEnabled(true);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		listView = (RecyclerView) findViewById(R.id.activity_products_listview);
		floatingButton = (ActionButton) findViewById(R.id.activity_products_floating_action_button);
		floatingButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				applyAndGoBack();
			}
		});
		
		ArrayList<Product> array = getIntent().getParcelableArrayListExtra(INTENT_TO_PRODUCTS_LISTITEMS);
		controller = new ProductsController(this, array);
		
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
		listView.setLayoutManager(mLayoutManager);
		listView.setAdapter(controller.getAdapter());
		
		RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
		listView.setItemAnimator(itemAnimator);
		listView.setAdapter(controller.getAdapter());
		
		getSupportLoaderManager().initLoader(MenuController.ID_FOR_CREATE_LOADER_FROM_DB, new Bundle(), controller);

		Loader<Cursor> loaderFromDB = getSupportLoaderManager().getLoader(MenuController.ID_FOR_CREATE_LOADER_FROM_DB);
		loaderFromDB.forceLoad();
	}
	
	private void applyAndGoBack(){
		Intent intent = new Intent();
		intent.putParcelableArrayListExtra(ListFragment.INTENT_TO_LIST_RETURNED_LISTITEMS, controller.apply());
		setResult(RESULT_OK, intent);
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_for_products, menu);
		showActionButton();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case android.R.id.home:{
			hideActionButton();
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
	
	@Override
	protected void onResume(){
		super.onResume();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	        return true;
	    }
	    return super.onKeyUp(keyCode, event);
	}
	
	public void showListView(int size){
		progress.setVisibility(View.GONE);
		if (size==0){
			listView.setVisibility(View.GONE);
			textViewEmpty.setVisibility(View.VISIBLE);
		} else{
			listView.setVisibility(View.VISIBLE);
			textViewEmpty.setVisibility(View.GONE);
		}
	}
	
	public void showActionButton() {
		floatingButton.setShowAnimation(ActionButton.Animations.SCALE_UP);
		floatingButton.show();
	}

	public void hideActionButton() {
		floatingButton.setHideAnimation(ActionButton.Animations.SCALE_DOWN);
		floatingButton.hide();
	}
}
