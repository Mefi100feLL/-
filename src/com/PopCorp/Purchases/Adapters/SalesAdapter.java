package com.PopCorp.Purchases.Adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Data.Sale;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SalesAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<Sale> array;
	private DisplayImageOptions options;
	private ImageLoader imageLoader;

	public SalesAdapter(Context context, ArrayList<Sale> array) {
		this.context = context;
		this.array=array;
		
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
		
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		int NumColumns = ((GridView)parent).getNumColumns();
		if (convertView == null) {
			imageView = new ImageView(context);
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setPadding(5, 5, 5, 5);
			imageView.setLayoutParams(new AbsListView.LayoutParams(parent.getWidth()/NumColumns,parent.getWidth()/NumColumns));
		}else{
			imageView = (ImageView) convertView;
		}

		Sale sale = array.get(position);

		imageLoader.displayImage(sale.getImageUrl(), imageView, options);
		return imageView;
	}

	public int getCount() {
		return array.size();
	}

	public Sale getItem(int position) {
		return array.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
}