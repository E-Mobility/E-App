package de.dhbw.e_mobility.e_app.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import de.dhbw.e_mobility.e_app.R;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothInfoState;
import de.dhbw.e_mobility.e_app.bluetooth.Command;
import de.dhbw.e_mobility.e_app.bluetooth.DeviceProvider;
import de.dhbw.e_mobility.e_app.settings.SettingsElements;
import de.dhbw.e_mobility.e_app.speedo.SpeedoValues;

public class ActivityHandler {

    // Singleton
    private static ActivityHandler instance;
    // Handler of classes
    private Handler handlerDeviceProvider = null;
    private Handler handlerSettings = null;
    private Handler handlerDiscovery = null;
    // Maps with all commands or values
    private HashMap<String, Command> bluetooth_commands = null;
    private HashMap<String, SpeedoValues> speedo_values = null;
    // Handling with activities
    private Vector<Activity> activities;
    private Context mainContext;
    // Others
    private boolean loggedIn;
    private long duration_start;
    private long duration;

    // Constructor
    private ActivityHandler() {
        // activeHandler = null;
        activities = new Vector<Activity>();
        loggedIn = false;
        saveBluetoothCommands();
        saveSpeedoValues();
    }

    // Singleton
    public static synchronized ActivityHandler getInstance() {
        if (instance == null) {
            instance = new ActivityHandler();
        }
        return instance;
    }

    // Returns the right handler
    private Handler getHandler(int handlerID) {
        if (IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue() == handlerID) {
            return handlerDeviceProvider;
        } else if (IntentKeys.HANDLLER_SETTINGS.getValue() == handlerID) {
            return handlerSettings;
        } else if (IntentKeys.HANDLLER_DISCOVERY.getValue() == handlerID) {
            return handlerDiscovery;
        }
        return null;
    }

    // Unregister handler
    public void unsetHandler(int handlerID) {
        if (IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue() == handlerID) {
            handlerDeviceProvider = null;
        } else if (IntentKeys.HANDLLER_SETTINGS.getValue() == handlerID) {
            handlerSettings = null;
        } else if (IntentKeys.HANDLLER_DISCOVERY.getValue() == handlerID) {
            handlerDiscovery = null;
        }
    }

    // Register handler
    public void setHandler(int handlerID, Handler theHandler) {
        if (IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue() == handlerID) {
            handlerDeviceProvider = theHandler;
        } else if (IntentKeys.HANDLLER_SETTINGS.getValue() == handlerID) {
            handlerSettings = theHandler;
        } else if (IntentKeys.HANDLLER_DISCOVERY.getValue() == handlerID) {
            handlerDiscovery = theHandler;
        }
    }

    // Add Activity from list
    public void add(Activity theActivity) {
        activities.add(theActivity);
        manageLogin();
    }

    // Remove Activity from list
    public void del(Activity theActivity) {
        activities.remove(theActivity);
        manageLogout();

    }

    // Returns the SharedPreferences
    private SharedPreferences getSharedPref() {
        if (mainContext != null) {
            return mainContext.getSharedPreferences(
                    mainContext.getPackageName() + "_preferences",
                    Context.MODE_PRIVATE);
        }
        return null;
    }

    // Manages the logout process
    private void manageLogout() {
        if (activities.size() == 0 && loggedIn) {
            Log.d("AppHANDLER", "LOGOUT");
            DeviceProvider deviceProvider = DeviceProvider
                    .getInstance();
            if (deviceProvider != null) {
                deviceProvider.logout();
            }
            loggedIn = false;
        }
    }

    // Manages the login process
    private void manageLogin() {
        if (activities.size() > 0 && !loggedIn) {
            Log.d("AppHANDLER", "LOGIN");
            DeviceProvider deviceProvider = DeviceProvider
                    .getInstance();
            if (deviceProvider != null && isAutologin()) {
                deviceProvider.login();
            }
            loggedIn = true;
        }
    }

    // Returns the main context
    public Context getMainContext() {
        return mainContext;
    }

    // Saves the main context
    public void setMainContext(Context theContext) {
        mainContext = theContext;
        resetDuration();
    }

    // Fires a BluetoothInfoState to one of the handlers
    public boolean fireToHandler(int handlerID, BluetoothInfoState infoState) {
        Bundle bundle = new Bundle();
        bundle.putInt(IntentKeys.MESSAGE_NUMBER.toString(), infoState.ordinal());
        return fireToHandler(handlerID, IntentKeys.BLUETOOTH_INFO_STATE.getValue(), bundle);
    }

    // Obtains a message to one of the handlers
    public boolean fireToHandler(int handlerID, int what) {
        Handler handler = getHandler(handlerID);
        if (handler == null) {
            return false;
        }
        handler.obtainMessage(what).sendToTarget();
        return true;
    }

    // Fires a message to one of the handlers
    public boolean fireToHandler(int handlerID, int what, String txt) {
        Bundle bundle = new Bundle();
        bundle.putString(IntentKeys.MESSAGE_TEXT.toString(), txt);
        return fireToHandler(handlerID, what, bundle);
    }

    // Fires a bundle to one of the handlers
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

    // Fires a toast with a resource text
    public void fireToast(int resId) {
        fireToast(resId, "");
    }

    // Fires a toast message plus a resource text
    public void fireToast(int resId, String txt) {
        fireToast(getStr(resId) + txt);
    }

    // Fires a toast message
    public void fireToast(String txt) {
        Toast.makeText(mainContext, txt, Toast.LENGTH_LONG).show();
    }

    // Returns a resource text as string
    public String getStr(int resId) {
        return mainContext.getString(resId);
    }

    // Saves all bluetooth commands in the map
    private void saveBluetoothCommands() {
        // Prepare the commands
        bluetooth_commands = new HashMap<String, Command>();
        for (Command command : Command.values()) {
            bluetooth_commands.put(command.getCommand(), command);
        }
    }

    // Saves all speedo values in the map
    private void saveSpeedoValues() {
        // Prepare the commands
        speedo_values = new HashMap<String, SpeedoValues>();
        for (SpeedoValues command : SpeedoValues.values()) {
            speedo_values.put(command.getCommand(), command);
        }
    }

    // Returns the bluetooth command map
    public HashMap<String, Command> getBluetoothCommands() {
        return bluetooth_commands;
    }

    // Returns the speedo value map
    public HashMap<String, SpeedoValues> getSpeedoValues() {
        return speedo_values;
    }

    // Returns true if auto login is active
    private boolean isAutologin() {
        SharedPreferences tmpPref = getSharedPref();
        return tmpPref != null && tmpPref.getBoolean(SettingsElements.AUTOLOG.getKey(), false);
    }

    // Checks the saved unit factor settings
    public float getUnitFactor() {
        if (!isKm()) {
            return (float) 0.621371192;
        }
        return 1;
    }

    // Checks if the current speed unit is km
    public boolean isKm() {
        SharedPreferences tmpPref = getSharedPref();
        if (tmpPref != null) {
            Resources res = mainContext.getResources();
            String[] distanceUnits = res.getStringArray(R.array.settings_distance_unit);
            if (distanceUnits.length > 1) {
                if (!tmpPref.getString(SettingsElements.DISTANCE.getKey(), "").equals(
                        distanceUnits[1])) {
                    return true;
                }
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

    // Returns the current value of the duration timer
    public String getDuration() {
        long millis = duration;
        if (duration_start != -1) {
            millis = SystemClock.elapsedRealtime() - duration_start + duration;
        }
        // Format milliseconds to output text
        String tmp_duration = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        SpeedoValues.DURATION.saveString(tmp_duration);
        return tmp_duration;
    }

    // Starts the duration timer
    public void startDurationTimer() {
        if (duration_start == -1) {
            duration_start = SystemClock.elapsedRealtime();
        }
    }

    // Stops the duration timer
    public void stopDurationTimer() {
        if (duration_start != -1) {
            long millis = SystemClock.elapsedRealtime() - duration_start;
            duration += millis;
            duration_start = -1;
        }
    }

    // Initialize the duration timer
    public void resetDuration() {
        duration = 0;
        duration_start = -1;
    }
}