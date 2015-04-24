package com.PopCorp.Purchases.Fragments;

import java.util.ArrayList;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Controllers.SalesController;
import com.PopCorp.Purchases.Data.Sale;
import com.PopCorp.Purchases.Loaders.SalesInternetLoader;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SalesFragment extends Fragment {
	
	public static final String TAG = SalesFragment.class.getSimpleName();
	public static final String CURRENT_SHOP_ID_TO_SALES_FRAGMENT = "currentShopID";
	public static final String CURRENT_SHOP_NAME_TO_SALES_FRAGMENT = "currentShopName";
	
	private GridView gridView;
	private ProgressBar progress;
	private Toolbar toolBar;

	private SalesController controller;

	private AppCompatActivity context;
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
	
	private String currentShopId;
	private String currentShopName;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_sales, container, false);
		context = (AppCompatActivity) getActivity();
		
		imageLoader = ImageLoader.getInstance();
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_launcher)
		.showImageForEmptyUri(R.drawable.ic_launcher)
		.showImageOnFail(R.drawable.ic_launcher)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
				
		gridView = (GridView) rootView.findViewById(R.id.fragment_sales_gridview);
		progress = (ProgressBar) rootView.findViewById(R.id.fragment_sales_progress);
		
		currentShopId = getArguments().getString(CURRENT_SHOP_ID_TO_SALES_FRAGMENT);
		currentShopName = getArguments().getString(CURRENT_SHOP_NAME_TO_SALES_FRAGMENT);
		
		toolBar = (Toolbar) getActivity().findViewById(R.id.activity_main_toolbar);
		toolBar.setTitle(currentShopName);
		
		controller = new SalesController(this, context, currentShopId, currentShopName);
		
		gridView.setAdapter(controller.getAdapter());
		
		gridView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Sale clickedSale = controller.getSale(position);
				MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
				builder.customView(R.layout.dialog_sale, true);
				builder.positiveText(R.string.dialog_in_list);
				builder.negativeText(R.string.dialog_cancel);
				builder.callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        controller.addListsAndShowDialogForSelect(clickedSale);
                    }

					@Override
                    public void onNegative(MaterialDialog dialog) {
                    	
                    }
                });
				Dialog dialog = builder.build();
				
				ImageView imageView = (ImageView) dialog.findViewById(R.id.dialog_sale_image);
				imageLoader.displayImage(clickedSale.getImageUrl(), imageView, options);
				
				LinearLayout layoutFields = (LinearLayout) dialog.findViewById(R.id.dialog_sale_layout_with_fields);
				TextView name = (TextView) dialog.findViewById(R.id.dialog_sale_name);
				TextView subTitle = (TextView) dialog.findViewById(R.id.dialog_sale_subtitle);
				TextView coast = (TextView) dialog.findViewById(R.id.dialog_sale_coast);
				TextView period = (TextView) dialog.findViewById(R.id.dialog_sale_period);
				TextView shop = (TextView) dialog.findViewById(R.id.dialog_sale_shop);
				TextView count = (TextView) dialog.findViewById(R.id.dialog_sale_count);
				
				name.setText(clickedSale.getTitle());
				
				if (clickedSale.getCount().isEmpty()){
					count.setVisibility(View.GONE);
				} else{
					count.setText(clickedSale.getCount() + " ");
				}
				
				coast.setText(getString(R.string.string_coast_za) + " " + clickedSale.getCoast());
				
				shop.setText(" " + getString(R.string.string_in_shop) + " " + currentShopName);
				
				if (clickedSale.getPeriodBeginInString().equals(clickedSale.getPeriodFinishInString())){
					period.setText(clickedSale.getPeriodBeginInString());
				} else{
					period.setText(clickedSale.getPeriodBeginInString() + " - " + clickedSale.getPeriodFinishInString());
				}
				
				if (clickedSale.getSubTitle().isEmpty()){
					subTitle.setVisibility(View.GONE);
				} else{
					subTitle.setText(clickedSale.getSubTitle());
				}
				
				dialog.setCanceledOnTouchOutside(false);
				dialog.show();
				layoutFields.setVisibility(View.VISIBLE);
			}
		});
		
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

		FragmentManager fragmentManager = context.getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, tag).commit();
	}
}
