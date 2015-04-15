package com.PopCorp.Purchases.Adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Activities.MainActivity;
import com.PopCorp.Purchases.Comparators.ListComparator;
import com.PopCorp.Purchases.Controllers.ListController;
import com.PopCorp.Purchases.Data.List;
import com.PopCorp.Purchases.Data.ListItem;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> implements Filterable{

	private MainActivity context;
	private ArrayList<ListItem> items;
	private ArrayList<ListItem> publishItems;
	private ListController controller;
	private ListItem updatedItem;
	private int oldPosition = -1;

	public ArrayList<ListItem> getPublishItems() {
		return publishItems;
	}

	private String currency;
	private RecyclerView listView;
	private ActionMode actionMode;
	public ActionMode getActionMode() {
		return actionMode;
	}

	private ArrayList<ListItem> selectedItems;

	public ListAdapter(MainActivity context, ArrayList<ListItem> items, ListController controller, String currency, RecyclerView listView){
		super();
		this.context = context;
		this.items = items;
		publishItems = new ArrayList<ListItem>();
		publishItems.addAll(items);
		this.controller = controller;
		this.currency = currency;
		this.listView = listView;
		selectedItems = new ArrayList<ListItem>();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
		public View view;
		public TextView name;
		public TextView count;
		public TextView edizm;
		public TextView coast;
		public TextView shop;
		public TextView comment;
		public TextView totalOne;
		public TextView totalTwo;
		public ImageView important;
		private ClickListener clickListener;

		public ViewHolder(View view) {
			super(view);
			this.view = view;
			name = (TextView) view.findViewById(R.id.item_listitem_name);
			count = (TextView) view.findViewById(R.id.item_listitem_count);
			edizm = (TextView) view.findViewById(R.id.item_listitem_edizm);
			coast = (TextView) view.findViewById(R.id.item_listitem_coast);
			shop = (TextView) view.findViewById(R.id.item_listitem_shop);
			comment = (TextView) view.findViewById(R.id.item_listitem_comment);
			totalOne = (TextView) view.findViewById(R.id.item_listitem_total_one);
			totalTwo = (TextView) view.findViewById(R.id.item_listitem_total_two);
			important = (ImageView) view.findViewById(R.id.item_listitem_image_important);
			view.setOnClickListener(this);
			view.setOnLongClickListener(this);
		}

		public interface ClickListener {
			public void onClick(View v, int position, boolean isLongClick);
		}

		public void setClickListener(ClickListener clickListener) {
			this.clickListener = clickListener;
		}

		@Override
		public boolean onLongClick(View v) {
			clickListener.onClick(v, getAdapterPosition(), true);
			return true;
		}

		@Override
		public void onClick(View v) {
			clickListener.onClick(v, getAdapterPosition(), false);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public ListItem getItem(int position){
		return publishItems.get(position);
	}

	@Override
	public int getItemCount() {
		return publishItems.size();
	}

	ActionMode.Callback callback = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode currentActionMode, Menu menu) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode currentActionMode) {
			actionMode = null;
			selectedItems.clear();
			for (int i=0; i<publishItems.size(); i++){
				notifyItemChanged(i);
			}
		}

		@Override
		public boolean onCreateActionMode(ActionMode currentActionMode, Menu menu) {
			actionMode = currentActionMode;
			currentActionMode.getMenuInflater().inflate(R.menu.popup_menu_for_item, menu);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode currentActionMode, MenuItem item) {
			switch (item.getItemId()){
			case R.id.action_edit_item : {
				controller.startEditingItem(selectedItems.get(0));
				break;
			}
			case R.id.action_remove_item : {
				controller.removeItems(selectedItems);
				break;
			}
			case R.id.action_send_as_sms : {
				controller.sendItems(List.TYPE_OF_SENDING_LIST_TO_SMS, selectedItems);
				break;
			}
			case R.id.action_send_as_email : {
				controller.sendItems(List.TYPE_OF_SENDING_LIST_TO_EMAIL, selectedItems);
				break;
			}
			case R.id.action_send_as_text : {
				controller.sendItems(List.TYPE_OF_SENDING_LIST_AS_TEXT, selectedItems);
				break;
			}
			default : {
				return true;
			}
			}
			actionMode.finish();
			return false;
		}
	};

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		ListItem item = publishItems.get(position);

		holder.name.setText(item.getName());
		holder.count.setText(item.getCountInString());
		holder.edizm.setText(item.getEdizm());
		holder.coast.setText(item.getCoastInString() + " " + currency);

		String total = item.getCount().multiply(item.getCoast()).toString();
		if (item.getComment().isEmpty()){
			holder.shop.setVisibility(View.GONE);

			if (item.getShop().isEmpty()){
				holder.comment.setVisibility(View.GONE);
				holder.totalTwo.setVisibility(View.GONE);
				holder.totalOne.setVisibility(View.VISIBLE);
				holder.totalOne.setText(total + " " + currency);
			} else{
				holder.comment.setVisibility(View.VISIBLE);
				holder.totalTwo.setVisibility(View.VISIBLE);
				holder.totalOne.setVisibility(View.GONE);
				holder.comment.setText(context.getString(R.string.string_in) + " " + item.getShop());
				holder.totalTwo.setText(total + " " + currency);
			}
		} else{
			holder.comment.setVisibility(View.VISIBLE);
			holder.comment.setText(item.getComment());
			holder.totalTwo.setVisibility(View.VISIBLE);
			holder.totalOne.setVisibility(View.GONE);
			holder.totalTwo.setText(total + " " + currency);

			if (item.getShop().isEmpty()){
				holder.shop.setVisibility(View.GONE);
			} else{
				holder.shop.setVisibility(View.VISIBLE);
				holder.shop.setText(context.getString(R.string.string_in) + " " + item.getShop());
			}
		}

		if (item.isImportant()){
			holder.important.setVisibility(View.VISIBLE);
		} else {
			holder.important.setVisibility(View.GONE);
		}

		holder.setClickListener(new ViewHolder.ClickListener() {
			@Override
			public void onClick(View view, int position, boolean isLongClick) {
				if (isLongClick) {
					if (actionMode!=null){
						changeItemInActionMode(position);
					} else{
						listView.startActionMode(callback);
						selectedItems.add(publishItems.get(position));
						actionMode.setTitle(String.valueOf(selectedItems.size()));
						notifyItemChanged(position);
					}
				} else {
					if (actionMode!=null){
						changeItemInActionMode(position);
					} else{
						controller.changeItemBuyed(publishItems.get(position));
					}
				}
			}

			private void changeItemInActionMode(int position) {
				if (selectedItems.contains(publishItems.get(position))){
					selectedItems.remove(publishItems.get(position));
				} else{
					selectedItems.add(publishItems.get(position));
				}
				notifyItemChanged(position);
				actionMode.setTitle(String.valueOf(selectedItems.size()));
				if (selectedItems.size()==1){
					actionMode.getMenu().findItem(R.id.action_edit_item).setVisible(true);
				} else{
					actionMode.getMenu().findItem(R.id.action_edit_item).setVisible(false);
				}
				if (selectedItems.size()==0){
					actionMode.finish();
					actionMode = null;
				}
			}
		});
		if (item.isBuyed()){
			holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG| Paint.FAKE_BOLD_TEXT_FLAG);
			((ViewGroup) holder.view).getChildAt(0).setAlpha(0.3f);
		} else {
			holder.name.setPaintFlags(Paint.LINEAR_TEXT_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
			((ViewGroup) holder.view).getChildAt(0).setAlpha(1f);
		}

		if (selectedItems.contains(item)){
			holder.view.setActivated(true);
		} else{
			holder.view.setActivated(false);
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listitem, parent, false);

		ViewHolder viewHolder = new ViewHolder(v);
		return viewHolder;
	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				ArrayList<ListItem> newItems = (ArrayList<ListItem>) results.values;
				ListIterator<ListItem> iterator = (ListIterator<ListItem>) publishItems.listIterator(0);
				while (iterator.hasNext()){
					ListItem item = iterator.next();
					if (!newItems.contains(item)){
						int position = publishItems.indexOf(item);
						iterator.remove();
						notifyItemRemoved(position);
					}
				}
				ArrayList<ListItem> tmpItems = new ArrayList<ListItem>(newItems);
				tmpItems.removeAll(publishItems);
				publishItems.addAll(tmpItems);

				Collections.sort(publishItems, new ListComparator());
				for (ListItem item : tmpItems){
					int position = publishItems.indexOf(item);
					if (position!=-1){
						notifyItemInserted(position);
					}
				}
				updateEditedItem();
			}

			private void updateEditedItem() {
				if (updatedItem!=null){
					int newPosition = publishItems.indexOf(updatedItem);
					if (oldPosition!=-1 && newPosition!=-1){
						if (oldPosition != newPosition){
							notifyItemMoved(oldPosition, newPosition);
						}
					}
					updatedItem = null;
					oldPosition = -1;
				}
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				ArrayList<ListItem> FilteredArrayNames = new ArrayList<ListItem>();

				if (constraint.equals("") || constraint.equals(context.getString(R.string.string_all_shops))){
					results.count = items.size();
					results.values = items;
					return results;
				}

				for (int i = 0; i < items.size(); i++) {
					ListItem item = items.get(i);
					if (item.getShop().equals(constraint)){
						FilteredArrayNames.add(item);
					}
				}

				results.count = FilteredArrayNames.size();
				results.values = FilteredArrayNames;
				return results;
			}
		};

		return filter;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public void setUpdatedItem(int oldPosition, ListItem editedItem) {
		this.oldPosition = oldPosition;
		this.updatedItem = editedItem;
	}
}
