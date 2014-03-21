package de.dhbw.e_mobility.e_app;

import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ActivityHandler {

	private Handler activeHandler;
	private Vector<Activity> activities;
	private Context appContext;
	private Context mainContext;
	private static ActivityHandler instance;
	private boolean loogedIn;

	public static synchronized ActivityHandler getInstance() {
		if (instance == null) {
			instance = new ActivityHandler();
		}
		return instance;
	}

	private ActivityHandler() {
		activeHandler = null;
		activities = new Vector<Activity>();
		appContext = null;
		loogedIn = false;
	}

	public void add(Activity theActivity) {
		add(theActivity, null);
	}

	public void add(Activity theActivity, Handler theHandler) {
		activeHandler = theHandler;
		activities.add(theActivity);
		appContext = theActivity.getApplicationContext();
		manageLogin();
	}

	public void del(Activity activity) {
		activities.remove(activity);
		manageLogout();

	}

	public void setMainContext(Context theContext) {
		mainContext = theContext;
	}

	private void manageLogout() {
		if (activities.size() == 0 && loogedIn) {
			Log.d("AppHANDLER", "LOGOUT");
			// TODO
			loogedIn = false;
		}
	}

	private void manageLogin() {
		if (activities.size() > 0 && !loogedIn) {
			Log.d("AppHANDLER", "LOGIN");
			// TODO
			loogedIn = true;
		}
	}

	public Context getMainContext() {
		return mainContext;
	}

	public Context getAppContext() {
		return appContext;
	}

	public boolean fireToHandler(int what) {
		if (activeHandler == null) {
			return false;
		}
		activeHandler.obtainMessage(what).sendToTarget();
		return true;
	}

	public boolean fireToHandler(int what, String txt) {
		if (activeHandler == null) {
			return false;
		}
		Bundle bundle = new Bundle();
		bundle.putString(SettingsActivity.MESSAGE_TEXT, txt);
		Message msg = new Message();
		msg.what = what;
		msg.setData(bundle);
		activeHandler.sendMessage(msg);
		return true;
	}

	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_LONG_TOAST = 5;
	public static final int MESSAGE_SHORT_TOAST = 6;
	public static final int UPDATE_BLUETOOTHINFO = 7;

	public void fireToast(int resId) {
		fireToast(resId, "");
	}

	public void fireToast(int resId, String txt) {
		fireToast(getStr(resId) + txt);
	}

	public void fireToast(String txt) {
		fireToHandler(MESSAGE_LONG_TOAST, txt);
	}

	public String getStr(int resId) {
		return mainContext.getString(resId);
	}

	// TODO
	// # Logout immer wenn jemand Anwendung verlässt (onPause oder so)
	// # Login immer bei (onResume)
	// # # (Bei Loginversuch vorher prüfen ob verbunden, wenn nicht verbinden)
	// # # # Beim verbinden prüfen ob gerät verfügbar
	// # # # beim verbinden prüfen ob gerät gepaird (wenn nicht, dann mache)
	// # # # # beim pairgen prüfen ob bluetooth an (wenn nicth, dann mach)
}
