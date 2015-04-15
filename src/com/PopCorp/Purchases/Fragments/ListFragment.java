package com.PopCorp.Purchases.Fragments;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.SD;
import com.PopCorp.Purchases.Activities.ProductsActivity;
import com.PopCorp.Purchases.Adapters.CategoriesAdapter;
import com.PopCorp.Purchases.Controllers.ListController;
import com.PopCorp.Purchases.Controllers.MenuController;
import com.PopCorp.Purchases.Data.ListItem;
import com.PopCorp.Purchases.Data.Product;
import com.shamanland.fab.FloatingActionButton;
import com.shamanland.fab.ShowHideOnScroll;

public class ListFragment extends Fragment{

	public static final String TAG = ListFragment.class.getSimpleName();

	public static final String INTENT_TO_LIST_TITLE = "title";
	public static final String INTENT_TO_LIST_DATELIST = "datelist";

	public static final String INTENT_TO_LIST_RETURNED_LISTITEMS = "array";

	public static final int REQUEST_CODE_FOR_INTENT_TO_PRODUCTS = 1;
	private static final int REQUEST_CODE_FOR_INTENT_SPEECH = 2;

	private RecyclerView listView;
	private LinearLayout layoutWithFields;
	private AutoCompleteTextView editTextForName;
	private EditText editTextForCount;
	private Spinner spinnerForEdizm;
	private EditText editTextForCoast;
	private Spinner spinnerForCategory;
	private Spinner spinnerForShop;
	private EditText editTextForComment;
	private CheckBox checkBoxForImportant;
	private ImageView buttonForVoice;
	private ImageView buttonCountPlus;
	private ImageView buttonCountMinus;
	private FrameLayout layoutForSnackBar;

	private ArrayList<String> edizmsForSpinner;
	private ArrayAdapter<String> adapterForSpinnerEdizm;

	private CategoriesAdapter adapterForSpinnerCategory;

	private ArrayList<String> shopesForSpinner;
	private ArrayAdapter<String> adapterForSpinnerShop;

	private TextView textviewForTotal;
	private TextView textviewForTotalBuyedCount;
	private TextView textviewForTotalCount;

	private ListController controller;
	private FloatingActionButton floatingButton;
	private Menu menu;


	public FloatingActionButton getFloatingButton() {
		return floatingButton;
	}

	private SharedPreferences sPref;
	private SharedPreferences.Editor editor;;
	private ActionBarActivity context;

	private Toolbar toolBar;

	private ShowHideOnScroll showHideOnScroll;

	private ArrayList<String> categories;
	private ArrayList<Integer> colors;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_list, container, false);
		context = (ActionBarActivity) getActivity();
		sPref = PreferenceManager.getDefaultSharedPreferences(context);
		editor = sPref.edit();

		buttonForVoice = (ImageView) rootView.findViewById(R.id.fragment_list_button_for_voice);
		listView = (RecyclerView) rootView.findViewById(R.id.fragment_list_listview);
		layoutWithFields = (LinearLayout) rootView.findViewById(R.id.fragment_list_fields_layout);
		editTextForName = (AutoCompleteTextView) rootView.findViewById(R.id.fragment_list_edittext_for_name);
		editTextForCount = (EditText) rootView.findViewById(R.id.fragment_list_edittext_for_count);
		spinnerForEdizm = (Spinner) rootView.findViewById(R.id.fragment_list_spinner_for_edizm);
		editTextForCoast = (EditText) rootView.findViewById(R.id.fragment_list_edittext_for_coast);
		spinnerForCategory = (Spinner) rootView.findViewById(R.id.fragment_list_spinner_for_category);
		spinnerForShop = (Spinner) rootView.findViewById(R.id.fragment_list_spinner_for_shop);
		editTextForComment = (EditText) rootView.findViewById(R.id.fragment_list_edittext_for_comment);
		checkBoxForImportant = (CheckBox) rootView.findViewById(R.id.fragment_list_checkbox_for_important);
		buttonCountPlus = (ImageView) rootView.findViewById(R.id.fragment_list_count_plus);
		buttonCountMinus = (ImageView) rootView.findViewById(R.id.fragment_list_count_minus);

		textviewForTotal = (TextView) rootView.findViewById(R.id.fragment_list_textview_total);
		textviewForTotalBuyedCount = (TextView) rootView.findViewById(R.id.fragment_list_textview_total_buyed_count);
		textviewForTotalCount = (TextView) rootView.findViewById(R.id.fragment_list_textview_total_count);
		floatingButton = (FloatingActionButton) rootView.findViewById(R.id.fragment_list_floating_action_button);

		layoutForSnackBar = (FrameLayout) rootView.findViewById(R.id.fragment_list_layout_for_snackbar);

		floatingButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (layoutWithFields.getVisibility() == View.GONE){
					clearFields();
					layoutWithFields.setVisibility(View.VISIBLE);
				} else {
					if (!editTextForName.getText().toString().isEmpty()){
						for (ListItem listItem : controller.getCurrentList().getItems()){
							if (listItem.getName().equals(editTextForName.getText().toString())){
								if (controller.getEditedItem()==null){
									return;
								} else {
									break;//if editing, then name may equals
								}
							}
						}
						String shop = (String) spinnerForShop.getSelectedItem();
						if (spinnerForShop.getSelectedItemPosition() == adapterForSpinnerShop.getCount()-1){
							shop = "";
						}
						controller.addNewListItem(
								editTextForName.getText().toString(),
								editTextForCount.getText().toString(),
								(String) spinnerForEdizm.getSelectedItem(),
								editTextForCoast.getText().toString(),
								(String) spinnerForCategory.getSelectedItem(),
								shop,
								editTextForComment.getText().toString(),
								String.valueOf(checkBoxForImportant.isChecked()));
					} else {
						// show toast about no name
						return;
					}
					layoutWithFields.setVisibility(View.GONE);
					clearFields();
					floatingButton.setImageResource(R.drawable.ic_add);
				}
			}
		});

		setHasOptionsMenu(true);

		buttonForVoice.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				displaySpeechRecognizer();
			}
		});

		buttonCountPlus.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					BigDecimal count = new BigDecimal(editTextForCount.getText().toString());
					count = count.add(new BigDecimal("1"));
					editTextForCount.setText(count.toString());
				} catch(Exception e){

				}
			}
		});

		buttonCountMinus.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				try{
					BigDecimal count = new BigDecimal(editTextForCount.getText().toString());
					if (count.doubleValue()>=1){
						count = count.subtract(new BigDecimal("1"));
						editTextForCount.setText(count.toString());
					}
				} catch (Exception e){

				}
			}
		});

		toolBar = (Toolbar) getActivity().findViewById(R.id.activity_main_toolbar);

		toolBar.setTitle(getArguments().getString(INTENT_TO_LIST_TITLE));
		controller = new ListController(this, getArguments().getString(INTENT_TO_LIST_TITLE), getArguments().getString(INTENT_TO_LIST_DATELIST), listView, layoutForSnackBar);

		toolBar.setTitle(controller.getCurrentList().getName());

		LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
		listView.setLayoutManager(mLayoutManager);
		listView.setAdapter(controller.getAdapter());

		RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
		listView.setItemAnimator(itemAnimator);

		showHideOnScroll = new ShowHideOnScroll(floatingButton);
		listView.setOnTouchListener(showHideOnScroll);

		initializeEdizms();
		initializeSpinnerForCategory();
		initializeSpinnerForShop();

		getLoaderManager().initLoader(MenuController.ID_FOR_CREATE_LOADER_FROM_DB, new Bundle(), controller);

		Loader<Cursor> loaderFromDB = getLoaderManager().getLoader(MenuController.ID_FOR_CREATE_LOADER_FROM_DB);
		loaderFromDB.forceLoad();
		return rootView;
	}

	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.menu_for_list, menu);
		this.menu = menu;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_all_items) {
			goToProducts();
			return true;
		}
		if (id == R.id.action_change_list) {
			controller.showDialogForEditingList();
			return true;
		}
		if (id == R.id.action_remove_list) {
			controller.removeCurrentList();
			return true;
		}
		if (id == R.id.action_send_list) {
			controller.showDialogForSendingList();
			return true;
		}
		if (id == R.id.action_load_list) {
			controller.loadFromSMS();
			return true;
		}
		if (id == R.id.action_put_alarm) {
			controller.showDialogForAlarm();
			return true;
		}
		if (controller.onOptionsItemSelected(item)){
			item.setChecked(true);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void goToProducts() {
		controller.removeItemsFromTmpArray();
		Intent intent = new Intent(context, ProductsActivity.class);
		ArrayList<Product> selectedItems = controller.getCurrentList().getSelectedItems();
		intent.putParcelableArrayListExtra(ProductsActivity.INTENT_TO_PRODUCTS_LISTITEMS, selectedItems);
		startActivityForResult(intent, REQUEST_CODE_FOR_INTENT_TO_PRODUCTS);
	}

	public void putItemInFields(ListItem item){
		if (layoutWithFields.getVisibility() == View.GONE){
			layoutWithFields.setVisibility(View.VISIBLE);
		}
		editTextForName.setText(item.getName());
		editTextForCount.setText(item.getCountInString());
		spinnerForEdizm.setSelection(getPositionForEdizm(item.getEdizm()));
		editTextForCoast.setText(item.getCoastInString());
		putCategoryInSpinner(item.getCategory());
		spinnerForShop.setSelection(getPositionForShop(item.getShop()));
		editTextForComment.setText(item.getComment());
		checkBoxForImportant.setChecked(item.isImportant());
	}

	private void clearFields(){
		editTextForName.setText("");
		editTextForCount.setText("1.0");
		spinnerForEdizm.setSelection(getPositionForEdizm(sPref.getString(SD.PREFS_DEF_EDIZM, getResources().getString(R.string.default_unit_one))));
		editTextForCoast.setText("");
		spinnerForCategory.setSelection(adapterForSpinnerCategory.getCount()-1);
		spinnerForShop.setSelection(getPositionForShop(shopesForSpinner.get(shopesForSpinner.size()-1)));
		editTextForComment.setText("");
		checkBoxForImportant.setChecked(false);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK){
			if (requestCode == REQUEST_CODE_FOR_INTENT_SPEECH) {
				ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				String spokenText = results.get(0);
				editTextForName.setText(spokenText);
			}
			if (requestCode == REQUEST_CODE_FOR_INTENT_TO_PRODUCTS) {
				controller.openDB();
				ArrayList<Product> returnedArray = data.getParcelableArrayListExtra(INTENT_TO_LIST_RETURNED_LISTITEMS);
				controller.updateArray(returnedArray);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
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

	public void showTotals(String totalBuyed, String total, String size) {
		textviewForTotalBuyedCount.setText(totalBuyed + " " + controller.getCurrentList().getCurrency());
		textviewForTotal.setText(getString(R.string.content_total).replace("%0", size));
		textviewForTotalCount.setText(total + " " +controller.getCurrentList().getCurrency());
	}

	private void displaySpeechRecognizer() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		startActivityForResult(intent, REQUEST_CODE_FOR_INTENT_SPEECH);
	}

	public void setTitle(String newName) {
		toolBar.setTitle(newName);
	}

	public void backToLists() {
		controller.removeItemsFromTmpArray();
		setTitle(context.getString(R.string.string_lists));
		Fragment fragment = new MenuFragment();
		String tag = MenuFragment.TAG;

		FragmentManager fragmentManager = context.getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, tag).commit();
	}

	private void initializeEdizms(){
		edizmsForSpinner = new ArrayList<String>(sPref.getStringSet(SD.PREFS_EDIZMS, new LinkedHashSet<String>()));

		adapterForSpinnerEdizm = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, edizmsForSpinner);
		adapterForSpinnerEdizm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerForEdizm.setAdapter(adapterForSpinnerEdizm);
		spinnerForEdizm.setSelection(getPositionForEdizm(sPref.getString(SD.PREFS_DEF_EDIZM, getResources().getString(R.string.default_unit_one))));

		spinnerForEdizm.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				editTextForCoast.setHint(getResources().getString(R.string.string_coast_za_ed) + " " + spinnerForEdizm.getItemAtPosition(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	private int getPositionForEdizm(String edizm){
		if (edizm==null){
			return adapterForSpinnerEdizm.getCount()-1;
		}
		if (!edizmsForSpinner.contains(edizm)){
			if (edizm.equals("")){
				return adapterForSpinnerEdizm.getCount()-1;
			}
			edizmsForSpinner.add(edizm);
			addNewEdizmToPrefs(edizm);
		}
		return adapterForSpinnerEdizm.getPosition(edizm);
	}

	private void addNewEdizmToPrefs(final String newEdizm) {
		Set<String> edizmsFromPrefs = sPref.getStringSet(SD.PREFS_EDIZMS, new LinkedHashSet<String>());
		edizmsFromPrefs.add(newEdizm);
		editor.putStringSet(SD.PREFS_EDIZMS, edizmsFromPrefs);
		editor.commit();
	}


	private void initializeSpinnerForCategory() {
		categories = controller.getCategories();
		colors = controller.getColors();

		adapterForSpinnerCategory = new CategoriesAdapter(context, categories, colors);
		adapterForSpinnerCategory.setDropDownViewResource(R.layout.item_list_categories);
		spinnerForCategory.setAdapter(adapterForSpinnerCategory);
		spinnerForCategory.setSelection(adapterForSpinnerCategory.getCount()-1);//select no category default
	}

	private void putCategoryInSpinner(String category) {
		if (categories.contains(category)) {
			spinnerForCategory.setSelection(adapterForSpinnerCategory.getItemPosition(category));
		} else {
			spinnerForCategory.setSelection(adapterForSpinnerCategory.getCount()-1);
		}
	}



	private void initializeSpinnerForShop() {
		shopesForSpinner = new ArrayList<String>(sPref.getStringSet(SD.PREFS_SHOPES, new LinkedHashSet<String>()));
		shopesForSpinner.add(getResources().getString(R.string.string_no_shop));//add item for no shop

		adapterForSpinnerShop = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, shopesForSpinner);
		adapterForSpinnerShop.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerForShop.setAdapter(adapterForSpinnerShop);
		spinnerForShop.setSelection(getPositionForShop(shopesForSpinner.get(shopesForSpinner.size()-1)));
	}

	private int getPositionForShop(String shop){
		if (shop==null){
			return adapterForSpinnerShop.getCount()-1;
		}
		if (!shopesForSpinner.contains(shop)){
			if (shop.equals("")){
				return adapterForSpinnerShop.getCount()-1;
			}
			shopesForSpinner.add(shop);
			addNewShopToPrefs(shop);
		}
		return adapterForSpinnerShop.getPosition(shop);
	}

	private void addNewShopToPrefs(final String newShop) {
		Set<String> shopesFromPrefs = sPref.getStringSet(SD.PREFS_SHOPES, new LinkedHashSet<String>());
		shopesFromPrefs.add(newShop);
		editor.putStringSet(SD.PREFS_SHOPES, shopesFromPrefs);
		editor.commit();
	}

	public void onBackPressed(){
		if (layoutWithFields.getVisibility() == View.VISIBLE){
			layoutWithFields.setVisibility(View.GONE);
			clearFields();
			floatingButton.setImageResource(R.drawable.ic_add);
			return;
		}
		if (controller.closeActionMode()){
			return;
		}
		backToLists();
	}



	public void hideFilterMenuItem() {
		menu.findItem(R.id.action_filter).setVisible(false);
	}

	public void showFilterMenuItem(ArrayList<String> filterShops, String selectedShop) {
		int groupId = 12;
		MenuItem item = menu.findItem(R.id.action_filter);
		item.getSubMenu().clear();
		for (String shop : filterShops){
			MenuItem addedItem = item.getSubMenu().add(groupId, shop.hashCode(), Menu.NONE, shop);
			if (shop.equals(selectedShop)){
				addedItem.setChecked(true);
			}
		}
		item.getSubMenu().setGroupCheckable(groupId, true, true);
		item.getSubMenu().setGroupEnabled(groupId, true);
		item.setVisible(true);
	}

	public void showFloatingButton(){
		showHideOnScroll.onScrollDown();
	}



	public void setAutoCompleteAdapter() {
		editTextForName.setAdapter(controller.getAdapterWithProducts());
		editTextForName.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListItem item = controller.getSelectedItem(position);
				if (item!=null){
					putItemInFields(item);
					item = null;
				}
			}
		});
	}
}
