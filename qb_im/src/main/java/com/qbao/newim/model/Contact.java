package com.qbao.newim.model;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.LinkedHashMap;

public class Contact implements Comparable<Contact>{
	public long contactPhoto;
	public String hashValue;
	public String contactName;
	public int contactId;
	public String contactNum;
	public String pinyin;
	public long user_id;
    public boolean is_add;
	public LinkedHashMap<Integer, String> pinyin_index;

	public String getInitial(){
		String pinYin = pinyin;
		if (TextUtils.isEmpty(pinYin)) {
			return "~";
		}
		pinYin = pinYin.trim();
		if (TextUtils.isEmpty(pinYin)) {
			return "~";
		}
		String initial;
		char initialChar = pinYin.charAt(0);
		if (Character.isLetter(initialChar)) {
			initial = Character.toString(initialChar).toUpperCase();
		} else {
			initial = "~";
		}
		return initial;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hashValue == null) ? 0 : hashValue.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contact other = (Contact) obj;
		if (hashValue == null) {
			if (other.hashValue != null)
				return false;
		} else if (!hashValue.equals(other.hashValue))
			return false;
		return true;
	}

	@Override
	public int compareTo(@NonNull Contact o) {
		return this.getInitial().compareTo(o.getInitial());
	}
}
