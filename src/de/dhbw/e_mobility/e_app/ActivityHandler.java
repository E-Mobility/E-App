package de.dhbw.e_mobility.e_app;

import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothCommands;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDeviceProvider;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothInfoState;

public class ActivityHandler {

	// TODO aufr�umen

	// Result Intent
	public static final String BLUETOOTH_DEVICE_ADDRESS = "device_address";
	public static final String BLUETOOTH_DEVICE_NAME = "device_name";
	public static final String BLUETOOTH_DEVICE_UUID = "device_uuid";
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_LONG_TOAST = 5;
	public static final int MESSAGE_SHORT_TOAST = 6;
	public static final int UPDATE_BLUETOOTHINFO = 7;
	public static final int BLUETOOTH_INFO_STATE = 8;

	// erkennen und verwenden.. TODO
	public static final String MESSAGE_TEXT = "handle_text";
	public static final String MESSAGE_NUMBER = "handle_number";
	public static final int ASK_FOR_BLUETOOTH = 10;
	public static final int START_DISCOVERING_DEVICES = 11;
	public static final int UPDATE_BT_INFO = 30;
	// TODO Handler von den einzelnen KLassen speichern und dann �ber
	public static int HANDLLER_DEVICE_PROVIDER = 1;

	// public void add(Activity theActivity) {
	// add(theActivity, null);
	// }
	public static int HANDLLER_SETTINGS = 2;
	public static int HANDLLER_DISCOVERY = 3;
	private static ActivityHandler instance;
	private Handler handlerDeviceProvider = null;
	private Handler handlerSettings = null;
	private Handler handlerDiscovery = null;
	private HashMap<String, BluetoothCommands> bluetooth_commands = null;
	private HashMap<String, SpeedoValues> speedo_values = null;
	// private Handler activeHandler;
	private Vector<Activity> activities;
	private Context mainContext;
	private boolean loogedIn;

	private ActivityHandler() {
		// activeHandler = null;
		activities = new Vector<Activity>();
		loogedIn = false;
		saveBluetoothCommands();
        saveSpeedoValues();
	}

	public static synchronized ActivityHandler getInstance() {
		if (instance == null) {
			instance = new ActivityHandler();
		}
		return instance;
	}

	// Retruns the wanted handler
	private Handler getHandler(int handlerID) {
		if (HANDLLER_DEVICE_PROVIDER == handlerID) {
			return handlerDeviceProvider;
		} else if (HANDLLER_SETTINGS == handlerID) {
			return handlerSettings;
		} else if (HANDLLER_DISCOVERY == handlerID) {
			return handlerDiscovery;
		}
		return null;
	}

	// Unsets the Handler
	public void unsetHandler(int handlerID) {
		if (HANDLLER_DEVICE_PROVIDER == handlerID) {
			handlerDeviceProvider = null;
		} else if (HANDLLER_SETTINGS == handlerID) {
			handlerSettings = null;
		} else if (HANDLLER_DISCOVERY == handlerID) {
			handlerDiscovery = null;
		}
	}

	// Sets the Handler
	public void setHandler(int handlerID, Handler theHandler) {
		if (HANDLLER_DEVICE_PROVIDER == handlerID) {
			handlerDeviceProvider = theHandler;
		} else if (HANDLLER_SETTINGS == handlerID) {
			handlerSettings = theHandler;
		} else if (HANDLLER_DISCOVERY == handlerID) {
			handlerDiscovery = theHandler;
		}
	}

	public void add(Activity theActivity) {// , Handler theHandler) {
		// activeHandler = theHandler;
		activities.add(theActivity);
		manageLogin();
	}

	public void del(Activity theActivity) {
		activities.remove(theActivity);
		manageLogout();

	}

	private SharedPreferences getSharedPref() {
		if (mainContext != null) {
			return mainContext.getSharedPreferences(
					mainContext.getPackageName() + "_preferences",
					Context.MODE_PRIVATE);
		}
		return null;
	}

	private void manageLogout() {
		if (activities.size() == 0 && loogedIn) {
			Log.d("AppHANDLER", "LOGOUT");
			BluetoothDeviceProvider deviceProvider = BluetoothDeviceProvider
					.getInstance();
			if (deviceProvider != null) {
				deviceProvider.logout();
			}
			loogedIn = false;
		}
	}

	private void manageLogin() {
		if (activities.size() > 0 && !loogedIn) {
			Log.d("AppHANDLER", "LOGIN");
			BluetoothDeviceProvider deviceProvider = BluetoothDeviceProvider
					.getInstance();
			if (deviceProvider != null && isAutologin()) {
				deviceProvider.login();
			}
			loogedIn = true;
		}
	}

	public Context getMainContext() {
		return mainContext;
	}

	public void setMainContext(Context theContext) {
		mainContext = theContext;
	}

	public boolean fireToHandler(int handlerID, BluetoothInfoState infoState) {
		Bundle bundle = new Bundle();
		bundle.putInt(MESSAGE_NUMBER, infoState.ordinal());
		return fireToHandler(handlerID, BLUETOOTH_INFO_STATE, bundle);
	}

	// public boolean fireToHandler(Handler theHandler, int what) {
	public boolean fireToHandler(int handlerID, int what) {
		Handler handler = getHandler(handlerID);
		if (handler == null) {
			return false;
		}
		handler.obtainMessage(what).sendToTarget();
		return true;
	}

	// public boolean fireToHandler(Handler theHandler, int what, int resId,
	// String txt) {
	public boolean fireToHandler(int handlerID, int what, int resId, String txt) {
		return fireToHandler(handlerID, what, getStr(resId) + txt);
	}

	// public boolean fireToHandler(Handler theHandler, int what, int resId) {
	public boolean fireToHandler(int handlerID, int what, int resId) {
		return fireToHandler(handlerID, what, getStr(resId));
	}

	// public boolean fireToHandler(Handler theHandler, int what, String txt) {
	public boolean fireToHandler(int handlerID, int what, String txt) {
		Bundle bundle = new Bundle();
		bundle.putString(MESSAGE_TEXT, txt);
		return fireToHandler(handlerID, what, bundle);
	}

	// public boolean fireToHandler(Handler theHandler, int what, Bundle bundle)
	// {
	public boolean fireToHandler(int handlerID, int what, Bundle bundle) {
		Handler handler = getHandler(handlerID);
		if (handler == null) {
			return false;
		}
		Message msg = new Message();
		msg.what = what;
		msg.setData(bundle);
		handler.sendMessage(msg);
		return true;
	}

	// public void fireToast(Handler theHandler, int resId) {
	public void fireToast(int resId) {
		fireToast(resId, "");
	}

	// public void fireToast(Handler theHandler, int resId, String txt) {
	public void fireToast(int resId, String txt) {
		fireToast(getStr(resId) + txt);
	}

	// public void fireToast(Handler theHandler, String txt) {
	public void fireToast(String txt) {
		// fireToHandler(theHandler, MESSAGE_LONG_TOAST, txt);
		Toast.makeText(mainContext, txt, Toast.LENGTH_LONG).show();
		// Toast.makeText(mainContext, txt, Toast.LENGTH_LONG).show(); // TODO
		// TODO
		// TODO
		// TODO
		// TODO
		// TODO
		// TODO toast with short message
	}

	public String getStr(int resId) {
		return mainContext.getString(resId);
	}

	// TODO
	// # Logout immer wenn jemand Anwendung verl�sst (onPause oder so)
	// # Login immer bei (onResume)
	// # # (Bei Loginversuch vorher pr�fen ob verbunden, wenn nicht verbinden)
	// # # # Beim verbinden pr�fen ob ger�t verf�gbar
	// # # # beim verbinden pr�fen ob ger�t gepaird (wenn nicht, dann mache)
	// # # # # beim pairgen pr�fen ob bluetooth an (wenn nicth, dann mach)

	private void saveBluetoothCommands() {
		// Prepare the commands
		bluetooth_commands = new HashMap<String, BluetoothCommands>();
		for (BluetoothCommands command : BluetoothCommands.values()) {
			bluetooth_commands.put(command.getCommand(), command);
		}
	}

    private void saveSpeedoValues() {
        // Prepare the commands
        speedo_values = new HashMap<String, SpeedoValues>();
        for (SpeedoValues command : SpeedoValues.values()) {
            speedo_values.put(command.getCommand(), command);
        }
    }

	public HashMap<String, BluetoothCommands> getBluetoothCommands() {
		return bluetooth_commands;
	}
	public HashMap<String, SpeedoValues> getSpeedoValues() {
		return speedo_values;
	}

	// Returns true if auto login is active
	private boolean isAutologin() {
		SharedPreferences tmpPref = getSharedPref();
		if (tmpPref != null) {
			return tmpPref.getBoolean(SettingsElements.AUTOLOG.getKey(), false);
		}
		return false;
	}

	// Checks the saved speed settings
	public float getSpeedFactor() {
        if (!isKmh()) {
            return (float) 0.625;
        }
		return 1;
	}

    public boolean isKmh() {
        SharedPreferences tmpPref = getSharedPref();
        if (tmpPref != null) {
            if (!tmpPref.getString(SettingsElements.SPEED.getKey(), "").equals(
                    "mph")) {
                return true;
            }
        }
        return false;
    }

	// Returns the saved password
	public String getPassword() {
		SharedPreferences tmpPref = getSharedPref();
		if (tmpPref != null) {
			return tmpPref.getString(SettingsElements.PASSWORD.getKey(), null);
		}
		return null;
	}

	// Saves the device address
	public void saveDeviceAddress(String address) {
		SharedPreferences tmpPref = getSharedPref();
		if (tmpPref != null) {
			tmpPref.edit().putString(SettingsElements.DEVICE.getKey(), address)
					.apply();
		}
	}

	// Returns the device address
	public String getDeviceAddress() {
		SharedPreferences tmpPref = getSharedPref();
		if (tmpPref != null) {
			return tmpPref.getString(SettingsElements.DEVICE.getKey(), null);
		}
		return null;
	}
}
