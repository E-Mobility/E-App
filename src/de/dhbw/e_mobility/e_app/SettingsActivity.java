package de.dhbw.e_mobility.e_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDeviceProvider;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDialogDisconnect;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDialogDiscovery;

/**
 * This is the main Activity that displays the current connection session.
 */
public class SettingsActivity extends PreferenceActivity {

	// Intent Request Codes
	private static final int BLUETOOTH_REQUEST_ENABLE = 1;
	private static final int BLUETOOTH_REQUEST_DISCONNECT = 2;
	private static final int BLUETOOTH_REQUEST_DISCOVERY = 3;

	// Preferences Elements
	private static final String SETTINGS_BLUETOOTH = "settings_bluetooth";
	// TODO R.id.bluetooth (??)

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

		// Get preference for bluetooth and initialize the click events
		Preference pref_bluetooth = (Preference) findPreference(SETTINGS_BLUETOOTH);
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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// deviceProvider.unsetSettingsActivityHandler();
		finish();
	}

	// Updates the bluetooth info text
	private void updateBluetoothInfo() {
		((Preference) findPreference(SETTINGS_BLUETOOTH))
				.setSummary(settingsProvider.getBluetoothState());
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

					// ((Preference) findPreference(SETTINGS_BLUETOOTH))
					// .setSummary(msg.getData().getString(
					// ActivityHandler.MESSAGE_TEXT));
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
				// } else {
				// // Bluetooth was not enabled
				// Toast.makeText(this, R.string.settings_bluetoothDisabled,
				// Toast.LENGTH_SHORT).show();
			}
		} else if (resCode == BLUETOOTH_REQUEST_DISCONNECT) {
			// If Bluetooth-Deivce should disconnect
			if (reqCode == Activity.RESULT_OK) {
				deviceProvider.logout();
//				deviceProvider.stopService();
			}
		} else if (resCode == BLUETOOTH_REQUEST_DISCOVERY) {
			// If Bluetooth-Deivce was selected
			if (reqCode == Activity.RESULT_OK) {
				// Save selected device
				deviceProvider.setDevice(data.getExtras().getString(
						ActivityHandler.BLUETOOTH_DEVICE_ADDRESS));
				// Do next step for login
				deviceProvider.doOnResult();
				// deviceProvider.connectDevice(data);
				// } else {
				// // No device was selected
				// Toast.makeText(this, R.string.settings_discovery_no_device,
				// Toast.LENGTH_SHORT).show();
			}
		}
	}
	
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
