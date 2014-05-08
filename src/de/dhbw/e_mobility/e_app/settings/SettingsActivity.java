package de.dhbw.e_mobility.e_app.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;

import de.dhbw.e_mobility.e_app.R;
import de.dhbw.e_mobility.e_app.bluetooth.Command;
import de.dhbw.e_mobility.e_app.bluetooth.DeviceProvider;
import de.dhbw.e_mobility.e_app.common.ActivityHandler;
import de.dhbw.e_mobility.e_app.common.IntentKeys;
import de.dhbw.e_mobility.e_app.dialog.BluetoothDialogDiscovery;

/**
 * This is the main Activity that displays the current connection session.
 */
public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    // TODO INFO
    // http://developer.android.com/guide/topics/ui/settings.html

    // Intent Request Codes
    private static final int BLUETOOTH_REQUEST_ENABLE = 1;
    private static final int BLUETOOTH_REQUEST_DISCOVERY = 2;

    // Get ActivityHandler object
    private ActivityHandler activityHandler = ActivityHandler.getInstance();

    // Get DeviceProvider object
    private DeviceProvider deviceProvider = DeviceProvider
            .getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.activity_settings);
        setTheme(android.R.style.Theme_Black);

        // Set setOnPreferenceClickListener to preferences
        Preference pref_bluetooth = getPreference(SettingsElements.BLUETOOTH);
        if (pref_bluetooth != null) {
            pref_bluetooth
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            // Disable the preference, that user has to wait for feedback
                            enableBluetoothPreference(false);

                            // If already logged in asking for logout
                            if (deviceProvider.isLoggedIn()) {
                                logout();
                                return true;
                            }
                            deviceProvider.login();
                            return true;
                        }

                        // Asking if user really wants to log out
                        private void logout() {
                            new AlertDialog.Builder(activityHandler.getMainContext())
                                    .setTitle(R.string.dialog_disconnect_title)
                                    .setMessage(R.string.dialog_disconnect)
                                    .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            deviceProvider.logoutAndResetDevice();
                                        }
                                    }).setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    enableBluetoothPreference(true);
                                }
                            }).show();
                        }
                    });
        }
        final CheckBoxPreference pref_advanced = (CheckBoxPreference) getPreference(SettingsElements.ADVANCED);
        if (pref_advanced != null) {
            pref_advanced
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            CheckBoxPreference tmpPref = (CheckBoxPreference) preference;
                            boolean tmpChecked = tmpPref.isChecked();
                            Log.d("ADVANCED SETTINGS?", (tmpChecked ? "true"
                                    : "false"));
                            enableAdvancedSettings(false, tmpPref);
                            if (tmpChecked) {
                                new AlertDialog.Builder(activityHandler.getMainContext())
                                        .setTitle(R.string.dialog_advanced_settings_title)
                                        .setMessage(R.string.dialog_advanced_settings)
                                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                enableAdvancedSettings(true, pref_advanced);
                                            }
                                        }).setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                }).show();
                            }
                            return true;
                        }
                    });
            // Initialize advanced settings
            enableAdvancedSettings(
                    PreferenceManager.getDefaultSharedPreferences(this)
                            .getBoolean(SettingsElements.ADVANCED.getKey(),
                                    false), pref_advanced
            );
        }
        PreferenceScreen pref_controller = (PreferenceScreen) getPreference(SettingsElements.CONTROLLER);
        if (pref_controller != null) {
            pref_controller
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            // Initializing values
                            initAdvancedCommands();
                            return true;
                        }
                    });
        }

        final Preference pref_logging = getPreference(SettingsElements.LOGGING);
        if (pref_logging != null) {
            pref_logging
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            if (pref_logging.getSummary() == null) {
                                SettingsElements.LOGGING.setSummary("save");
                                pref_logging.setSummary("save on close");
                            } else {
                                SettingsElements.LOGGING.setSummary(null);
                                pref_logging.setSummary(null);
                            }
                            return true;
                        }
                    });
            pref_logging.setTitle(R.string.logging);
            SettingsElements.LOGGING.setSummary(null);
            pref_logging.setSummary(null);
        }

        final Preference pref_about = getPreference(SettingsElements.ABOUT);
        if (pref_about != null) {
            pref_about
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            new AlertDialog.Builder(activityHandler.getMainContext())
                                    .setTitle(R.string.settings_about)
                                    .setMessage(R.string.settings_about_text)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                            return true;
                        }
                    });
        }

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        initPreference(sharedPreferences, SettingsElements.PASSWORD);
        activityHandler.resetBattery();
        initPreference(sharedPreferences, SettingsElements.BATTERY);
        initPreference(sharedPreferences, SettingsElements.DISTANCE);

        String tmpKey;
        final Command[] tmpCommand = new Command[1];
        for (final SettingsElements element : SettingsElements.values()) {
            tmpKey = element.getKey();
            if (tmpKey.endsWith("_N")) {
                // Set OnPreferenceChangeListener for all command elements
                EditTextPreference tmpPref = (EditTextPreference) getPreference(element);
                if (tmpPref != null) {
                    // Change-Event
                    tmpPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                        @Override
                        public boolean onPreferenceChange(
                                Preference changedPref, Object changedValue) {
                            // On Change
                            String newValue = (String) changedValue;
                            Log.d("Command-changed", changedPref.getKey() + "="
                                    + newValue);
                            tmpCommand[0] = element.getCommand();
                            changedPref.setTitle(tmpCommand[0].getCommand() + " ("
                                    + newValue + ")");
                            deviceProvider.saveCommandValue(tmpCommand[0],
                                    newValue);
                            return true;
                        }
                    });

                }
            } else if (tmpKey.endsWith("_")) {
                // Set OnClickListener for all command elements
                Preference tmpPref = getPreference(element);
                if (tmpPref != null) {
                    tmpPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(Preference changedPref) {
                            Log.d("Command-click", changedPref.getKey());
                            tmpCommand[0] = element.getCommand();
                            new AlertDialog.Builder(activityHandler.getMainContext())
                                    .setTitle(R.string.dialog_really_title)
                                    .setMessage(R.string.dialog_really)
                                    .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            deviceProvider.sendCommand(tmpCommand[0]);
                                        }
                                    }).setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }).show();
                            return true;
                        }
                    });
                }

            }
        }
    }

    // Enables or disables the advanced settings
    private void enableAdvancedSettings(boolean value,
                                        CheckBoxPreference advancedPref) {
        if (advancedPref != null) {
            advancedPref.setChecked(value);
        }
        PreferenceScreen controllerPref = (PreferenceScreen) getPreference(SettingsElements.CONTROLLER);
        if (controllerPref != null) {
            controllerPref.setEnabled(value);
        }
    }

    // Initialize values of all command elements
    private void initAdvancedCommands() {
        String tmpKey;
        for (final SettingsElements element : SettingsElements.values()) {
            tmpKey = element.getKey();
            if (tmpKey.endsWith("_N")) {
                // Set title and text of the element with the current value
                EditTextPreference tmpPref = (EditTextPreference) getPreference(element);
                if (tmpPref != null) {
                    tmpPref.setSummary(element.getSummary());
                    Command tmpCommand = element.getCommand();
                    tmpPref.setPersistent(false);
                    String tmpValue = tmpCommand.getValue();
                    tmpPref.setTitle(tmpCommand.getCommand() + "(" + tmpValue
                            + ")");
                    tmpPref.setText(tmpValue);
                    Log.d("Command-init", tmpCommand.getCommand() + "="
                            + tmpCommand.getValue());
                }
            } else if (tmpKey.endsWith("_")) {
                // Set title of the element
                Preference tmpPref = getPreference(element);
                if (tmpPref != null) {
                    tmpPref.setSummary(element.getSummary());
                    Command tmpCommand = element.getCommand();
                    String tmpTitle = tmpCommand.getCommand();
                    if (tmpCommand == Command.LOGIN) {
                        tmpTitle = "at-login";
                    } else if (tmpTitle == null) {
                        tmpTitle = "???";
                    }
                    tmpPref.setTitle(tmpTitle);
                    Log.d("Command-init", tmpTitle);
                }

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // activityHandler.add(this);
        activityHandler.setHandler(IntentKeys.HANDLLER_SETTINGS.getValue(),
                setupHandler());
        updateBluetoothInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // activityHandler.del(this);
        activityHandler.unsetHandler(IntentKeys.HANDLLER_SETTINGS.getValue());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // deviceProvider.unsetSettingsActivityHandler();
        finish();
    }

    // Updates the bluetooth info text
    private void updateBluetoothInfo() {
        Preference tmpPref = getPreference(SettingsElements.BLUETOOTH);
        if (tmpPref != null) {
            tmpPref.setSummary(deviceProvider.getBluetoothState());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // Setup the handler for this class
    public Handler setupHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == IntentKeys.ASK_FOR_BLUETOOTH.getValue()) {
                    // ASKING FOR ENABLE THE BLUETOOTH ADAPTER
                    startActivityForResult(
                            new Intent(getStringFromMsg(msg, IntentKeys.MESSAGE_TEXT)),
                            BLUETOOTH_REQUEST_ENABLE);
                } else if (msg.what == IntentKeys.START_DISCOVERING_DEVICES.getValue()) {
                    // STARTING THE DISCOVERY DIALOG
                    startActivityForResult(new Intent(getApplicationContext(),
                                    BluetoothDialogDiscovery.class),
                            BLUETOOTH_REQUEST_DISCOVERY
                    );
                } else if (msg.what == IntentKeys.ENABLE_BLUETOOTH_PREF.getValue()) {
                    enableBluetoothPreference(true);
                } else if (msg.what == IntentKeys.DISABLE_BLUETOOTH_PREF.getValue()) {
                    enableBluetoothPreference(false);
                } else if (msg.what == IntentKeys.UPDATE_BT_INFO.getValue()) {
                    updateBluetoothInfo();
                }
            }

        };
    }

    // Enables / disables the preference element for bluetooth settings
    private void enableBluetoothPreference(boolean value) {
        Preference pref_bluetooth = getPreference(SettingsElements.BLUETOOTH);
        if (pref_bluetooth != null) {
            pref_bluetooth.setEnabled(value);
        }
    }

    // Returns a string from the data bundle
    private String getStringFromData(Intent data, IntentKeys key) {
        // TODO was hat es mit dem NULLpointer auf sich??
        return data.getExtras().getString(key.toString());
    }

    // Returns a string from the message bundle
    private String getStringFromMsg(Message msg, IntentKeys key) {
        // TODO was hat es mit dem NULLpointer auf sich??
        return msg.getData().getString(key.toString());
    }

    @Override
    protected void onActivityResult(int resCode, int reqCode, Intent data) {

        if (resCode == BLUETOOTH_REQUEST_ENABLE) {
            // If Enable-Bluetooth is true
            if (reqCode == Activity.RESULT_OK) {
                // Do next step for login
                deviceProvider.doOnResult();
            } else {
                enableBluetoothPreference(true);
            }
        } else if (resCode == BLUETOOTH_REQUEST_DISCOVERY) {
            // If Bluetooth-Deivce was selected
            if (reqCode == Activity.RESULT_OK) {
                // Save selected device
                String address = getStringFromData(data, IntentKeys.DEVICE_ADDRESS);
                deviceProvider.setDevice(address);
                // Do next step for login
                deviceProvider.doOnResult();
            } else {
                enableBluetoothPreference(true);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        updatePreference(sharedPreferences, key);
    }

    // Finds and returns the wanted preference element
    private Preference getPreference(SettingsElements element) {
        Preference tmpPref = findPreference(element.getKey());
        if (tmpPref == null) {
            Log.e("Settings", "Preference not found! (" + element.getKey()
                    + ")");
        }
        return tmpPref;
    }

    // Updating the preference summary
    private void updatePreference(SharedPreferences sharedPreferences,
                                  String key) {
        if (key.equals(SettingsElements.AUTOLOG.getKey()) || key.equals(SettingsElements.ADVANCED.getKey())
                || key.equals(SettingsElements.DEVICE.getKey()) || key.equals(SettingsElements.LOGGING.getKey())) {
            return;
        }
        if (key.equals(SettingsElements.PASSWORD.getKey())) {
            EditTextPreference tmpPref = (EditTextPreference) findPreference(key);
            if (tmpPref == null) {
                Log.e("Settings", "Preference not found! (" + key + ")");
            } else {
                String value = sharedPreferences.getString(key, "");
                tmpPref.setTitle(activityHandler
                        .getStr(R.string.settings_password)
                        + " ("
                        + value
                        + ")");
            }
        } else if (key.equals(SettingsElements.BATTERY.getKey())) {
            EditTextPreference tmpPref = (EditTextPreference) findPreference(key);
            if (tmpPref == null) {
                Log.e("Settings", "Preference not found! (" + key + ")");
            } else {
                String value = sharedPreferences.getString(key, "");
                // Check if value is valid integer
                try {
                    Integer.parseInt(value);
                } catch (Exception e) {
                    activityHandler.fireToast(R.string.settings_battery_wrong_input);
                    activityHandler.resetBattery();
                    value = sharedPreferences.getString(key, "");
                    tmpPref.setText(value);
                }
                tmpPref.setTitle(activityHandler
                        .getStr(R.string.settings_battery)
                        + " ("
                        + value
                        + ")");
            }
        } else {
            Preference tmpPref = findPreference(key);
            if (tmpPref == null) {
                Log.e("Settings", "Preference not found! (" + key + ")");
            } else {
                tmpPref.setSummary(sharedPreferences.getString(key, ""));
            }
        }
    }

    // Initialize the preference
    private void initPreference(SharedPreferences sharedPreferences,
                                SettingsElements element) {
        updatePreference(sharedPreferences, element.getKey());
    }

    // TODO add "reset complete statistic", "export", "import"
}
