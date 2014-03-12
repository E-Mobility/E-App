package de.dhbw.e_mobility.e_app;

import android.app.Application;
import android.content.Context;

public class Helper extends Application {

	private static Context mContext;

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
	}
	
	public static void setContext(Context theContext) {
		mContext = theContext;
	}

	public static Context getContext() {
		return mContext;
	}
	
	public static String getStr(int resId) {
		return mContext.getString(resId);
	}
}
