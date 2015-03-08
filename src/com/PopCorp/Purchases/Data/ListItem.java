package com.PopCorp.Purchases.Data;

import java.math.BigDecimal;

import android.os.Parcel;
import android.os.Parcelable;

import com.PopCorp.Purchases.DataBase.DB;

public class ListItem implements Cloneable, Parcelable{

	private long id;
	private String datelist;
	private String name;
	private BigDecimal count;
	private String edizm;
	private BigDecimal coast;
	private String category;
	private String shop;
	private String comment;
	private boolean buyed;
	private boolean important;
	
	private boolean selected;

	public ListItem(long id, String datelist, String name, String count, String edizm, String coast, String category, String shop, String comment, String buyed, String important){
		setId(id);
		setDatelist(datelist);
		setName(name);
		setCount(count);
		setEdizm(edizm);
		setCoast(coast);
		setCategory(category);
		setShop(shop);
		setComment(comment);
		setBuyed(buyed);
		setImportant(important);
	}
	
	public void update(DB db, String name, String count, String edizm, String coast, String category, String shop, String comment, String important){
		setName(name);
		setCount(count);
		setEdizm(edizm);
		setCoast(coast);
		setCategory(category);
		setShop(shop);
		setComment(comment);
		setImportant(important);
		db.update(DB.TABLE_ITEMS, DB.COLUMNS_ITEMS_WITHOUT_DATELIST, DB.KEY_ID + "=" + getId(), getFields());
		//to update in products table
	}

	public void changeBuyed(DB db){
		setBuyed(!isBuyed());
		db.update(DB.TABLE_ITEMS, DB.KEY_ID + "=" + getId() , DB.KEY_ITEMS_BUYED, isBuyedInString());
	}

	public void changeImportant(DB db){
		setImportant(!isImportant());
		db.update(DB.TABLE_ITEMS, DB.KEY_ID + "=" + getId(), DB.KEY_ITEMS_IMPORTANT, isImportantInString());
	}
	
	public void remove(DB db){
		db.deleteRows(DB.TABLE_ITEMS, DB.KEY_ID + "=" + getId());
	}
	
	public String[] getFields(){
		return new String[] {
				getName(),
				getCountInString(),
				getEdizm(),
				getCoastInString(),
				getCategory(),
				getShop(),
				getComment(),
				isBuyedInString(),
				isImportantInString()
		};
	}
	
	public String[] getFieldsWithDatelist(){
		return new String[] {
				getDatelist(),
				getName(),
				getCountInString(),
				getEdizm(),
				getCoastInString(),
				getCategory(),
				getShop(),
				getComment(),
				isBuyedInString(),
				isImportantInString()
		};
	}

	
	
	public ListItem clone(){
		return new ListItem(new Long(id),
				new String(datelist),
				new String(name),
				new String(String.valueOf(count)),
				new String(edizm),
				new String(String.valueOf(coast)),
				new String(category),
				new String(shop),
				new String(comment),
				new String(String.valueOf(buyed)),
				new String(String.valueOf(important)));
	}
	
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(datelist);
		dest.writeString(name);
		dest.writeString(count.toString());
		dest.writeString(edizm);
		dest.writeString(coast.toString());
		dest.writeString(category);
		dest.writeString(shop);
		dest.writeString(comment);
		if (buyed){
			dest.writeByte((byte) 1);
		} else {
			dest.writeByte((byte) 0);
		}
		if (important){
			dest.writeByte((byte) 1);
		} else {
			dest.writeByte((byte) 0);
		}
	}

	public static final Parcelable.Creator<ListItem> CREATOR = new Parcelable.Creator<ListItem>() {
		public ListItem createFromParcel(Parcel in) {
			return new ListItem(in);
		}

		public ListItem[] newArray(int size) {
			return new ListItem[size];
		}
	};

	private ListItem(Parcel parcel) {
		id = parcel.readLong();
		datelist = parcel.readString();
		name = parcel.readString();
		count = new BigDecimal(parcel.readString());
		edizm = parcel.readString();
		coast = new BigDecimal(parcel.readString());
		category = parcel.readString();
		shop = parcel.readString();
		comment = parcel.readString();
		if (parcel.readByte()==1){
			buyed=true;
		}else{
			buyed=false;
		}
		if (parcel.readByte()==1){
			important=true;
		}else{
			important=false;
		}
	}
	
	

	////////////////////////////////////////////////// getters and setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDatelist() {
		return datelist;
	}

	public void setDatelist(String datelist) {
		if (datelist!=null){
			this.datelist = datelist;
		}else{
			this.datelist = "";
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name!=null){
			this.name = name;
		}else{
			this.name = "";
		}
	}

	public BigDecimal getCount() {
		return count;
	}
	
	public String getCountInString() {
		return count.toString();
	}

	public void setCount(BigDecimal count) {
		if (count!=null){
			this.count = count;
		}else{
			this.count = new BigDecimal("0");
		}
	}

	public void setCount(String count) {
		try{
			this.count = new BigDecimal(count);
		} catch(Exception e){
			this.count = new BigDecimal("0");
		}
	}

	public String getEdizm() {
		return edizm;
	}

	public void setEdizm(String edizm) {
		if (edizm!=null){
			this.edizm = edizm;
		}else{
			this.edizm = "";
		}
	}

	public BigDecimal getCoast() {
		return coast;
	}
	
	public String getCoastInString() {
		return coast.toString();
	}

	public void setCoast(BigDecimal coast) {
		if (coast!=null){
			this.coast = coast;
		}else{
			this.coast = new BigDecimal("0");
		}
	}

	public void setCoast(String coast) {
		try{
			setCoast(new BigDecimal(coast));
		} catch(Exception e){
			setCoast(new BigDecimal("0"));
		}
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		if (category!=null){
			this.category = category;
		}else{
			this.category = "";
		}
	}

	public String getShop() {
		return shop;
	}

	public void setShop(String shop) {
		if (shop!=null){
			this.shop = shop;
		}else{
			this.shop = "";
		}
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		if (comment!=null){
			this.comment = comment;
		}else{
			this.comment = "";
		}
	}

	public boolean isBuyed() {
		return buyed;
	}
	
	public String isBuyedInString() {
		return String.valueOf(buyed);
	}

	public void setBuyed(boolean buyed) {
		this.buyed = buyed;
	}

	public void setBuyed(String buyed) {
		try{
			setBuyed(Boolean.valueOf(buyed));
		} catch(Exception e){
			setBuyed(false);
		}
	}

	public boolean isImportant() {
		return important;
	}
	
	public String isImportantInString() {
		return String.valueOf(important);
	}

	public void setImportant(boolean important) {
		this.important = important;
	}

	public void setImportant(String important) {
		try{
			setImportant(Boolean.valueOf(important));
		} catch(Exception e){
			setImportant(false);
		}
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
