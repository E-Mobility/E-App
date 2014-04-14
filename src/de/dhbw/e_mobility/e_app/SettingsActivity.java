package de.dhbw.e_mobility.e_app;

import java.util.HashMap;

import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuItem;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothCommands;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDeviceProvider;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDialogDisconnect;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDialogDiscovery;

/**
 * This is the main Activity that displays the current connection session.
 */
public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	// TODO INFO
	// http://developer.android.com/guide/topics/ui/settings.html

	// Intent Request Codes
	private static final int BLUETOOTH_REQUEST_ENABLE = 1;
	private static final int BLUETOOTH_REQUEST_DISCONNECT = 2;
	private static final int BLUETOOTH_REQUEST_DISCOVERY = 3;
	private static final int SETTINGS_REQUEST_ADVANCED = 4;
	private static final int SETTINGS_REQUEST_COMMAND = 5;

	private enum SettingsElements {
		SCREEN_MAIN("settings_screen"), //
		BLUETOOTH("settings_bluetooth"), //
		PASSWORD("settings_password"), //
		AUTOLOG("settings_autolog"), //
		SPEED("settings_speed"), //
		ADVANCED("settings_advanced"), //
		CONTROLLER("settings_controller"), //

		AT_0("AT_0_", BluetoothCommands.AT_0), //
		AT_DFLT("AT_DFLT_", BluetoothCommands.AT_DFLT), //
		AT_LOGOUT("AT_LOGOUT_", BluetoothCommands.AT_LOGOUT), //
		AT_CCAP("AT_CCAP_", BluetoothCommands.AT_CCAP), //
		AT_CDIST("AT_CDIST_", BluetoothCommands.AT_CDIST), //
		AT_EON("AT_EON_", BluetoothCommands.AT_EON), //
		AT_EOFF("AT_EOFF_", BluetoothCommands.AT_EOFF), //
		AT_PARAM_LIST("AT_PARAM_LIST_", BluetoothCommands.AT_PARAM_LIST), //
		LOGIN("AT_LOGIN_", BluetoothCommands.LOGIN), //

		AT_UPWD_N("AT_UPWD_N", BluetoothCommands.AT_UPWD_N), //
		AT_PUSH_N("AT_PUSH_N", BluetoothCommands.AT_PUSH_N), //
		AT_PUSHINT_N("AT_PUSHINT_N", BluetoothCommands.AT_PUSHINT_N), //
		AT_CALIBL_N("AT_CALIBL_N", BluetoothCommands.AT_CALIBL_N), //
		AT_CALIBH_N("AT_CALIBH_N", BluetoothCommands.AT_CALIBH_N), //
		AT_PP_N("AT_PP_N", BluetoothCommands.AT_PP_N), //
		AT_VL_N("AT_VL_N", BluetoothCommands.AT_VL_N), //
		AT_VLL_N("AT_VLL_N", BluetoothCommands.AT_VLL_N), //
		AT_CL_N("AT_CL_N", BluetoothCommands.AT_CL_N), //
		AT_CLL_N("AT_CLL_N", BluetoothCommands.AT_CLL_N), //
		AT_MM_N("AT_MM_N", BluetoothCommands.AT_MM_N), //
		AT_KPC_N("AT_KPC_N", BluetoothCommands.AT_KPC_N), //
		AT_KIC_N("AT_KIC_N", BluetoothCommands.AT_KIC_N), //
		AT_KPS_N("AT_KPS_N", BluetoothCommands.AT_KPS_N), //
		AT_KIS_N("AT_KIS_N", BluetoothCommands.AT_KIS_N), //
		AT_KK_N("AT_KK_N", BluetoothCommands.AT_KK_N), //
		AT_LIGHT_N("AT_LIGHT_N", BluetoothCommands.AT_LIGHT_N), //
		AT_LVl_N("AT_LVL_N", BluetoothCommands.AT_LVL_N), //
		AT_CL1_0_N("AT_CL1_0_N", BluetoothCommands.AT_CL1_0_N), //
		AT_CL1_1_N("AT_CL1_1_N", BluetoothCommands.AT_CL1_1_N), //
		AT_CL2_0_N("AT_CL2_0_N", BluetoothCommands.AT_CL2_0_N), //
		AT_CL2_1_N("AT_CL2_1_N", BluetoothCommands.AT_CL2_1_N), //
		AT_SL1_0_N("AT_SL1_0_N", BluetoothCommands.AT_SL1_0_N), //
		AT_SL1_1_N("AT_SL1_1_N", BluetoothCommands.AT_SL1_1_N), //
		AT_SL2_0_N("AT_SL2_0_N", BluetoothCommands.AT_SL2_0_N), //
		AT_SL2_1_N("AT_SL2_1_N", BluetoothCommands.AT_SL2_1_N), //
		AT_CLT_0_N("AT_CLT_0_N", BluetoothCommands.AT_CLT_0_N), //
		AT_CLT_1_N("AT_CLT_1_N", BluetoothCommands.AT_CLT_1_N), //
		AT_PEDAL_N("AT_PEDAL_N", BluetoothCommands.AT_pedal_N), //
		AT_OTO_N("AT_OTO_N", BluetoothCommands.AT_OTO_N), //
		AT_CCM_N("AT_CCM_N", BluetoothCommands.AT_CCM_N), //
		AT_THM_N("AT_THM_N", BluetoothCommands.AT_THM_N), //
		AT_PR_N("AT_PR_N", BluetoothCommands.AT_PR_N), //
		AT_PTIME_N("AT_PTIME_N", BluetoothCommands.AT_PTIME_N), //
		AT_VA_N("AT_VA_N", BluetoothCommands.AT_VA_N); //

		private String key;
		private BluetoothCommands command;

		SettingsElements(String val) {
			this(val, null);
		}

		SettingsElements(String val, BluetoothCommands theCommand) {
			key = val;
			command = theCommand;
		}

		public BluetoothCommands getCommand() {
			return command;
		}

		public String getKey() {
			return key;
		}

		@Override
		public String toString() {
			return key;
		}
	}

	private BluetoothCommands clickedCommand;

	// TODO del
	private HashMap<String, SettingsElements> settings_elements;

	// Saves all SettingsElements in a HashMap
	private void saveSettingsElements() {
		settings_elements = new HashMap<String, SettingsElements>();
		for (SettingsElements element : SettingsElements.values()) {
			settings_elements.put(element.getKey(), element);
		}
	}

	// Get ActivityHandler object
	private ActivityHandler activityHandler = ActivityHandler.getInstance();

	// Get SettingsProvider object
	private SettingsProvider settingsProvider = SettingsProvider.getInstance();

	// Get BluetoothDeviceProvider object
	private BluetoothDeviceProvider deviceProvider = BluetoothDeviceProvider
			.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.activity_settings);

		// Handler myHandler = getHandler();
		// deviceProvider.setSettingsActivityHandler(myHandler);

		// Set setOnPreferenceClickListener to preferences
		Preference pref_bluetooth = getPreference(SettingsElements.BLUETOOTH);
		if (pref_bluetooth != null) {
			pref_bluetooth
					.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							// If already loged in asking for logout
							if (settingsProvider.isLoggedIn()) {
								startActivityForResult(new Intent(
										getApplicationContext(),
										BluetoothDialogDisconnect.class),
										BLUETOOTH_REQUEST_DISCONNECT);
								return true;
							}
							deviceProvider.login();
							return true;
						}
					});
		}
		CheckBoxPreference pref_advanced = (CheckBoxPreference) getPreference(SettingsElements.ADVANCED);
		if (pref_advanced != null) {
			pref_advanced
					.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							Log.d("ADVANCED SETTINGS?", (settingsProvider
									.isAdvancedSettings() ? "true" : "false"));
							if (settingsProvider.isAdvancedSettings()) {
								startActivityForResult(new Intent(
										getApplicationContext(),
										SettingsAdvancedDialog.class),
										SETTINGS_REQUEST_ADVANCED);
							}
							return true;
						}
					});
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

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		initPreference(sharedPreferences, SettingsElements.PASSWORD);
		initPreference(sharedPreferences, SettingsElements.AUTOLOG);
		initPreference(sharedPreferences, SettingsElements.SPEED);
		initPreference(sharedPreferences, SettingsElements.ADVANCED);

		String tmpKey;
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
							BluetoothCommands tmpCommand = element.getCommand();
							changedPref.setTitle(tmpCommand.getCommand() + " ("
									+ newValue + ")");
							deviceProvider.saveCommandValue(tmpCommand,
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
							startActivityForResult(
									new Intent(getApplicationContext(),
											ReallyDialog.class),
									SETTINGS_REQUEST_COMMAND);

							BluetoothCommands tmpCommand = element.getCommand();
							clickedCommand = tmpCommand;
							// deviceProvider.sendCommand(tmpCommand);
							return true;
						}
					});
				}

			}
		}

		// TODO
	}

	// Initialize values of all command elements
	private void initAdvancedCommands() {
		String tmpKey, tmpValue;
		BluetoothCommands tmpCommand;
		for (SettingsElements element : SettingsElements.values()) {
			tmpKey = element.getKey();
			if (tmpKey.endsWith("_N")) {
				// Set title and text of the element with the current value
				EditTextPreference tmpPref = (EditTextPreference) getPreference(element);
				if (tmpPref != null) {
					tmpCommand = element.getCommand();
					tmpValue = tmpCommand.getValue();
					tmpPref.setTitle(tmpKey + " (" + tmpValue + ")");
					tmpPref.setText(tmpValue);
					Log.d("Command-init", tmpKey + "=" + tmpValue);
				}
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		activityHandler.add(this);
		activityHandler.setHandler(ActivityHandler.HANDLLER_SETTINGS,
				setupHandler());
		updateBluetoothInfo();
	}

	@Override
	protected void onStop() {
		super.onStop();
		activityHandler.del(this);
		activityHandler.unsetHandler(ActivityHandler.HANDLLER_SETTINGS);
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
			tmpPref.setSummary(settingsProvider.getBluetoothState());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_settings, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_advanced_settings) {
			// startActivity(new Intent(this, SettingsActivity.class));
			// TODO Advanced Settings einschalten
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public Handler setupHandler() {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {

				if (msg.what == ActivityHandler.ASK_FOR_BLUETOOTH) {
					// ASKING FOR ENABLE THE BLUETOOTH ADAPTER
					startActivityForResult(
							new Intent(msg.getData().getString(
									ActivityHandler.MESSAGE_TEXT)),
							BLUETOOTH_REQUEST_ENABLE);
				} else if (msg.what == ActivityHandler.START_DISCOVERING_DEVICES) {
					// STARTING THE DISCOVERY DIALOG
					startActivityForResult(new Intent(getApplicationContext(),
							BluetoothDialogDiscovery.class),
							BLUETOOTH_REQUEST_DISCOVERY);

				} else if (msg.what == ActivityHandler.UPDATE_BT_INFO) {
					updateBluetoothInfo();
				}
			}

		};
	}

	@Override
	public void onActivityResult(int resCode, int reqCode, Intent data) {

		if (resCode == BLUETOOTH_REQUEST_ENABLE) {
			// If Enable-Bluetooth is true
			if (reqCode == Activity.RESULT_OK) {
				// Do next step for login
				deviceProvider.doOnResult();
			}
		} else if (resCode == BLUETOOTH_REQUEST_DISCONNECT) {
			// If Bluetooth-Deivce should disconnect
			if (reqCode == Activity.RESULT_OK) {
				deviceProvider.logoutAndResetDevice();
			}
		} else if (resCode == BLUETOOTH_REQUEST_DISCOVERY) {
			// If Bluetooth-Deivce was selected
			if (reqCode == Activity.RESULT_OK) {
				// Save selected device
				deviceProvider.setDevice(data.getExtras().getString(
						ActivityHandler.BLUETOOTH_DEVICE_ADDRESS));
				// Do next step for login
				deviceProvider.doOnResult();
			}
		} else if (resCode == SETTINGS_REQUEST_ADVANCED) {
			// If message is confirmed
			if (reqCode == Activity.RESULT_OK) {
				PreferenceScreen controllerPref = (PreferenceScreen) getPreference(SettingsElements.CONTROLLER);
				if (controllerPref != null) {
					controllerPref.setEnabled(true);
				}
			} else {
				CheckBoxPreference advancedPref = (CheckBoxPreference) getPreference(SettingsElements.ADVANCED);
				if (advancedPref != null) {
					advancedPref.setChecked(false);
				}
			}
		} else if (resCode == SETTINGS_REQUEST_COMMAND) {
			// TODO bessere lösung für zwischenspeicherung finden..
			if (reqCode == Activity.RESULT_OK) {
				deviceProvider.sendCommand(clickedCommand);
			}
			clickedCommand = null;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updatePreference(sharedPreferences, key);
	}

	// Finds and returns the wanted preference element
	private Preference getPreference(SettingsElements element) {
		Preference tmpPref = (Preference) findPreference(element.toString());
		if (tmpPref == null) {
			Log.e("Settings", "Preference not found! (" + element.toString()
					+ ")");
		}
		return tmpPref;
	}

	private void updatePreference(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(SettingsElements.AUTOLOG.toString())) {
			// TODO
		} else if (key.equals(SettingsElements.ADVANCED.toString())) {
			PreferenceScreen controllerPref = (PreferenceScreen) getPreference(SettingsElements.CONTROLLER);
			if (controllerPref != null) {
				boolean value = sharedPreferences.getBoolean(key, false);
				settingsProvider.setAdvancedSettings(value);
				controllerPref.setEnabled(value);
				CheckBoxPreference advancedPref = (CheckBoxPreference) getPreference(SettingsElements.ADVANCED);
				if (advancedPref != null) {
					advancedPref.setChecked(value);
				}
			}
		} else if (key.equals(SettingsElements.PASSWORD.toString())) {
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
		} else {
			Preference tmpPref = (Preference) findPreference(key);
			if (tmpPref == null) {
				Log.e("Settings", "Preference not found! (" + key + ")");
			} else {
				String value = sharedPreferences.getString(key, "");
				tmpPref.setSummary(value);
			}
		}
		// TODO
	}

	private void initPreference(SharedPreferences sharedPreferences,
			SettingsElements element) {
		updatePreference(sharedPreferences, element.toString());
	}

	// TODO
	// Experten Ansicht nur wenn bluetooth verbunden / eingeloggt

	// TODO
	// Listenelemente
	// # Bluetooth
	// # Passwort
	// # Automatischer Login (ein/aus)
	// (# Gesamtstatistik zurücksetzten)
	// (# Export / Import)
	// # Geschwindigkeitsmaßeinheit (km/h | mph)
	// # Erweiterte Bluetootheinstellungen

	// # Vollbild
	// # Display an lassen
}
