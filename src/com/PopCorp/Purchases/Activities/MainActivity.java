package com.PopCorp.Purchases.Activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Adapters.NavigationListAdapter;
import com.PopCorp.Purchases.Data.List;
import com.PopCorp.Purchases.Fragments.ListFragment;
import com.PopCorp.Purchases.Fragments.MenuFragment;

public class MainActivity extends ActionBarActivity{

	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;
	private Toolbar toolBar;
	private ListView drawerList;
	private String[] navigationItems;
	private LinearLayout navigationLayout;
	private DrawerClickListener drawerClickListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toolBar = (Toolbar) findViewById(R.id.activity_main_toolbar);
	    setSupportActionBar(toolBar);
	    
	    drawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
	    
	    drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.app_name, R.string.app_name);
	    drawerLayout.setDrawerListener(drawerToggle);
	    
	    navigationItems = getResources().getStringArray(R.array.navigation_menu_items);
	    
	    navigationLayout = (LinearLayout) findViewById(R.id.activity_main_navigation_layout);
	    drawerList = (ListView) findViewById(R.id.activity_main_navigation_list);
	    drawerList.setAdapter(new NavigationListAdapter(this));
	    drawerClickListener = new DrawerClickListener();
	    drawerList.setOnItemClickListener(drawerClickListener);

	    Intent intent = getIntent();
	    if (intent.getStringExtra(ListFragment.INTENT_TO_LIST_TITLE)!=null){
	    	drawerClickListener.selectItem(1);
	    }
	    
		drawerClickListener.selectItem(1);
		
		Uri data = getIntent().getData();
		if (data!=null) {
			loadFromFile(data);
		} else {

		}
	}
	
	private void loadFromFile(Uri data) {
		getIntent().setData(null);
		try {
			final String scheme = data.getScheme();
			if (ContentResolver.SCHEME_CONTENT.equals(scheme) || ContentResolver.SCHEME_FILE.equals(scheme)) {
				ContentResolver cr = getContentResolver();
				InputStream is = cr.openInputStream(data);
				if (is == null){
					return;
				}

				StringBuffer buf = new StringBuffer();			
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String str;
				if (is!=null) {
					while ((str = reader.readLine()) != null) {	
						buf.append(str + "\n" );
					}				
				}		
				is.close();
				FragmentManager fragmentManager = getSupportFragmentManager();
			    Fragment findedFragment = fragmentManager.findFragmentByTag(MenuFragment.TAG);
			    if (findedFragment!=null){
			    	((MenuFragment) findedFragment).addListFromJSON(buf.toString());
			    }
			}
		} catch (Exception e) {
			finish();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if (drawerToggle.onOptionsItemSelected(item)) {
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
	    drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
	    if (drawerLayout.isDrawerOpen(Gravity.START | Gravity.LEFT)){
	        drawerLayout.closeDrawers();
	        return;
	    }
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    Fragment findedFragment = fragmentManager.findFragmentByTag(ListFragment.TAG);
	    if (findedFragment!=null){
	    	((ListFragment) findedFragment).onBackPressed();
	    	return;
	    }
	    super.onBackPressed();
	}
	
	private class DrawerClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
		
		public void selectItem(int position){
			((NavigationListAdapter) drawerList.getAdapter()).setSelected(position);
			Fragment fragment = null;
			String tag = null;
			switch (position) {
			case 0 : {
				fragment = new MenuFragment();
				tag = MenuFragment.TAG;
				break;
			}
			case 1 : {
				Intent intent = getIntent();
				fragment = new MenuFragment(intent.getStringExtra(ListFragment.INTENT_TO_LIST_TITLE), intent.getStringExtra(ListFragment.INTENT_TO_LIST_DATELIST));
				tag = MenuFragment.TAG;
				break;
			}
			case 2 : {
				fragment = new MenuFragment();
				tag = MenuFragment.TAG;
				break;
			}
			case 3 : {
				fragment = new MenuFragment();
				tag = MenuFragment.TAG;
				break;
			}
			}
			
		    FragmentManager fragmentManager = getSupportFragmentManager();
		    Fragment findedFragment = fragmentManager.findFragmentByTag(tag);
		    if (findedFragment==null){
		    	fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, tag).commit();

			    drawerList.setItemChecked(position, true);
			    toolBar.setTitle(navigationItems[position]);
		    }
		    
		    drawerLayout.closeDrawer(navigationLayout);
		}
	}
	
	@Override
	protected void onDestroy(){
		File directory = Environment.getExternalStorageDirectory();
		if (List.isExternalStorageWritable()){
			directory = new File(directory.getAbsolutePath() + "/Purchases");
			if (directory.exists()){
				File[] files = directory.listFiles();
				for (int i=0; i<files.length; i++){
					files[i].delete();
				}
			}
		}
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	        if (toolBar.isOverflowMenuShowing()) {
	        	toolBar.hideOverflowMenu();
	        } else {
	        	toolBar.showOverflowMenu();
	        }
	        return true;
	    }
	    return super.onKeyUp(keyCode, event);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}
}
