package com.PopCorp.Purchases.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Controllers.MenuController;
import com.software.shell.fab.ActionButton;

public class MenuFragment extends Fragment{

	public static final String TAG = MenuFragment.class.getSimpleName();

	private Context context;

	private RecyclerView listView;
	private MenuController controller;
	private FrameLayout layoutForSnackBar;

	private ActionButton floatingButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_menu, container, false);

		context = getActivity();
		setHasOptionsMenu(true);

		listView = (RecyclerView) rootView.findViewById(R.id.fragment_menu_listview);
		floatingButton = (ActionButton) rootView.findViewById(R.id.fragment_menu_floating_action_button);
		layoutForSnackBar = (FrameLayout) rootView.findViewById(R.id.fragment_menu_layout_for_snackbar);

		StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
		layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
		
		controller = new MenuController((AppCompatActivity) context, layoutForSnackBar, layoutManager, this);
		//controller.firstStart();

		floatingButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				controller.showDialogForNewList();
			}
		});

		listView.setLayoutManager(layoutManager);
		listView.setAdapter(controller.getAdapter());
		RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
		listView.setItemAnimator(itemAnimator);

		getLoaderManager().initLoader(MenuController.ID_FOR_CREATE_LOADER_FROM_DB, new Bundle(), controller);
		
		Bundle args = getArguments();
		if (args!=null){
			String title = args.getString(ListFragment.INTENT_TO_LIST_TITLE);
			String datelist = args.getString(ListFragment.INTENT_TO_LIST_DATELIST);
			if (title!=null && datelist!=null){
				controller.openList(title, datelist);
			}
		}
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.menu_for_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_load_list) {
			controller.loadFromSMS();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume(){
		super.onResume();
		controller.openDB();
		getLoaderManager().restartLoader(MenuController.ID_FOR_CREATE_LOADER_FROM_DB, new Bundle(), controller);

		Loader<Cursor> loaderFromDB = getLoaderManager().getLoader(MenuController.ID_FOR_CREATE_LOADER_FROM_DB);
		loaderFromDB.forceLoad();
	}

	@Override
	public void onStop(){
		super.onStop();
		controller.closeDB();
	}

	public void addListFromJSON(String json) {
		controller.addNewListFromJSON(json);
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	        return true;
	    }
	    return false;
	}
}
