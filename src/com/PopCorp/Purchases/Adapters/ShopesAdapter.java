package com.PopCorp.Purchases.Adapters;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Controllers.ShopesController;
import com.PopCorp.Purchases.Data.Shop;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ShopesAdapter extends RecyclerView.Adapter<ShopesAdapter.ViewHolder> {

	private ArrayList<Shop> shopes;
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
	private ShopesController controller;

	public ShopesAdapter(ArrayList<Shop> array, ShopesController controller) {
		this.shopes=array;
		this.controller = controller;
		
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
	
	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		public View view;
		public ImageView image;
		private ClickListener clickListener;
		
		public ViewHolder(View view) {
			super(view);
			this.view = view;
			image = (ImageView) view.findViewById(R.id.shop_imageview);
			view.setOnClickListener(this);
		}
		
		public interface ClickListener {
	        public void onClick(View v, int position);
	    }
		
		public void setClickListener(ClickListener clickListener) {
	        this.clickListener = clickListener;
	    }
		
		@Override
		public void onClick(View v) {
			clickListener.onClick(v, getAdapterPosition());
		}
	}

	@Override
	public int getItemCount() {
		return shopes.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Shop shop = shopes.get(position);

		imageLoader.displayImage(shop.getImageUrl(), holder.image, options);
		holder.setClickListener(new ViewHolder.ClickListener() {
	        @Override
	        public void onClick(View view, int position) {
	            controller.openShop(position);
	        }
	    });
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop, parent, false);
		
		ViewHolder viewHolder = new ViewHolder(v);
		return viewHolder;
	}
}
