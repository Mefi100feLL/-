package com.PopCorp.Purchases.Adapters;

import java.util.ArrayList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Controllers.MenuController;
import com.PopCorp.Purchases.Data.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder>{

	private ArrayList<List> lists;
	private MenuController controller;
	
	public MenuAdapter(Context context, ArrayList<List> lists, MenuController controller){
		super();
		this.lists = lists;
		this.controller = controller;
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		public View view;
		public TextView name;
		public TextView items;
		public TextView count;
		public ImageView alarm;
		public ImageView overflow;
		public View divider;
		public List list;
		private ClickListener clickListener;
		
		public ViewHolder(View view) {
			super(view);
			this.view = view;
			name = (TextView) view.findViewById(R.id.content_card_textview_name);
			items = (TextView) view.findViewById(R.id.content_card_textview_items);
			count = (TextView) view.findViewById(R.id.content_card_textview_total_count);
			alarm = (ImageView) view.findViewById(R.id.content_card_alarm_image);
			overflow = (ImageView) view.findViewById(R.id.content_card_overflow_image);
			divider = view.findViewById(R.id.content_card_divider);
			((ViewGroup) view).getChildAt(0).setOnClickListener(this);
		}
		
		public interface ClickListener {
	        public void onClick(View v, int position);
	    }
		
		public void setClickListener(ClickListener clickListener) {
	        this.clickListener = clickListener;
	    }

		
		@Override
		public void onClick(View v) {
			clickListener.onClick(v, getPosition());
		}
	}

	@Override
	public int getItemCount() {
		return lists.size();
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		List list = lists.get(position);
		
		holder.name.setText(list.getName());
		holder.count.setText(String.valueOf(list.getItems().size()));
		if (list.getItems().size()==0){
			holder.items.setVisibility(View.GONE);
			holder.divider.setVisibility(View.GONE);
		} else{
			holder.items.setVisibility(View.VISIBLE);
			holder.divider.setVisibility(View.VISIBLE);
			holder.items.setText(list.getSpannableStringBuilder());
		}
		if (list.getAlarm().equals("")){
			
		} else{
			
		}
		holder.overflow.setTag(list);
		holder.overflow.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				int position = lists.indexOf(v.getTag());
				controller.showPopupMenu(v, position);
			}
		});
		holder.setClickListener(new ViewHolder.ClickListener() {
	        @Override
	        public void onClick(View view, int position) {
	            controller.openList(position);
	        }
	    });
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
		
		ViewHolder viewHolder = new ViewHolder(v);
		return viewHolder;
	}
}
