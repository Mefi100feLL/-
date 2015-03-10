package com.PopCorp.Purchases.Activities;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Controllers.AllProductsController;
import com.PopCorp.Purchases.Controllers.MenuController;
import com.PopCorp.Purchases.Data.Product;

public class AllProductsActivity extends FragmentActivity{
	
	private ListView listView;
	private LinearLayout layoutWithFields;
	private EditText editTextForName;
	private EditText editTextForCount;
	private EditText editTextForEdizm;
	private EditText editTextForCoast;
	private EditText editTextForCategory;
	private EditText editTextForShop;
	private EditText editTextForComment;
	
	private AllProductsController controller;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_products);
		
		listView = (ListView) findViewById(R.id.activity_all_products_listview);
		listView = (ListView) findViewById(R.id.activity_all_products_listview);
		layoutWithFields = (LinearLayout) findViewById(R.id.activity_all_products_layout_with_fields);
		editTextForName = (EditText) findViewById(R.id.activity_all_products_edittext_for_name);
		editTextForCount = (EditText) findViewById(R.id.activity_all_products_edittext_for_count);
		editTextForEdizm = (EditText) findViewById(R.id.activity_all_products_edittext_for_edizm);
		editTextForCoast = (EditText) findViewById(R.id.activity_all_products_edittext_for_coast);
		editTextForCategory = (EditText) findViewById(R.id.activity_all_products_edittext_for_category);
		editTextForShop = (EditText) findViewById(R.id.activity_all_products_edittext_for_shop);
		editTextForComment = (EditText) findViewById(R.id.activity_all_products_edittext_for_comment);
		
		controller = new AllProductsController(this);
		
		listView.setAdapter(controller.getAdapter());
		
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				controller.showPopupMenu(view, position);
			}
		});
		
		getSupportLoaderManager().initLoader(MenuController.ID_FOR_CREATE_LOADER_FROM_DB, new Bundle(), controller);

		Loader<Cursor> loaderFromDB = getSupportLoaderManager().getLoader(MenuController.ID_FOR_CREATE_LOADER_FROM_DB);
		loaderFromDB.forceLoad();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_for_all_products, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_add_new_product) {
			if (layoutWithFields.getVisibility() == View.GONE){
				layoutWithFields.setVisibility(View.VISIBLE);
				//item.setIcon(R.drawable.ic_action_accept);
			} else {
				if (!editTextForName.getText().toString().isEmpty()){
					for (Product product : controller.getItems()){
						if (product.getName().equals(editTextForName.getText().toString())){
							if (controller.getEditedProduct()==null){
								return true;
							} else {
								break;
							}
						}
					}
					controller.addNewProduct(
							editTextForName.getText().toString(),
							editTextForCount.getText().toString(),
							editTextForEdizm.getText().toString(),
							editTextForCoast.getText().toString(),
							editTextForCategory.getText().toString(),
							editTextForShop.getText().toString(),
							editTextForComment.getText().toString());
				}
				layoutWithFields.setVisibility(View.GONE);
				clearFields();
				//item.setIcon(R.drawable.ic_action_new);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void putProductInFields(Product item){
		if (layoutWithFields.getVisibility() == View.GONE){
			layoutWithFields.setVisibility(View.VISIBLE);
		}
		editTextForName.setText(item.getName());
		editTextForCount.setText(item.getCount().toString());
		editTextForEdizm.setText(item.getEdizm());
		editTextForCoast.setText(item.getCoast().toString());
		editTextForCategory.setText(item.getCategory());
		editTextForShop.setText(item.getShop());
		editTextForComment.setText(item.getComment());
	}

	private void clearFields(){
		editTextForName.setText("");
		editTextForCount.setText("");
		editTextForEdizm.setText("");
		editTextForCoast.setText("");
		editTextForCategory.setText("");
		editTextForShop.setText("");
		editTextForComment.setText("");
	}
}
