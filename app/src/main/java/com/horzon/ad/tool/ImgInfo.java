package com.horzon.ad.tool;

import android.net.Uri;

public class ImgInfo {

	private int iconResId;

	private Uri iconResUri ;


	private String package_id ;

	public ImgInfo(int iconResId,String package_id) {
		super();
		this.iconResId = iconResId;
		this.package_id = package_id ;
	}


	public ImgInfo(Uri iconResUri, String package_id) {
		this.iconResUri = iconResUri;
		this.package_id = package_id ;

	}

	@Override
	public boolean equals(Object o) {

		if(o instanceof ImgInfo){
			ImgInfo info = (ImgInfo) o;
			return (info.package_id.equals(package_id));

		}

		return false;
	}

	public String getPackage_id() {
		return package_id;
	}

	public void setPackage_id(String package_id) {
		this.package_id = package_id;
	}


	public int getIconResId() {
		return iconResId;
	}
	public void setIconResId(int iconResId) {
		this.iconResId = iconResId;
	}


	public Uri getIconResUri() {
		return iconResUri;
	}

	public void setIconResUri(Uri iconResUri) {
		this.iconResUri = iconResUri;
	}

}
