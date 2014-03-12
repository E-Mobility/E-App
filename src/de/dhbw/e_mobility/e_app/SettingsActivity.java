/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.widget.Toast;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDeviceProvider;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDisconnectDialog;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDiscoveryActivity;

/**
 * This is the main Activity that displays the current chat session.
 */
public class SettingsActivity extends PreferenceActivity {

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_LONG_TOAST = 5;
	public static final int MESSAGE_SHORT_TOAST = 6;
	public static final int UPDATE_BLUETOOTHINFO = 7;

	// Intent Request Codes
	private static final int BLUETOOTH_REQUEST_ENABLE = 1;
	private static final int BLUETOOTH_REQUEST_DISCONNECT = 2;
	private static final int BLUETOOTH_REQUEST_DISCOVERY = 3;

	// Preferences Elements
	private static final String SETTINGS_BLUETOOTH = "settings_bluetooth";

	// Message parameter
	public static final String MESSAGE_TEXT = "settings_bluetooth";

	// Settings Items
	private BluetoothDeviceProvider deviceProvider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.activity_settings);

		// Setup own handler
		final Handler mHandler = setupHandler();
		deviceProvider = new BluetoothDeviceProvider(mHandler,
				getApplicationContext());

		// Get Bluetooth-Preference
		Preference pref_bluetooth = (Preference) findPreference(SETTINGS_BLUETOOTH);

		// Initialize the click events for the Bluetooth-Preference
		pref_bluetooth
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						if (deviceProvider.isBluetoothDisabled()) {
							startActivityForResult(
									new Intent(deviceProvider
											.getBluetoothEnableQuestion()),
									BLUETOOTH_REQUEST_ENABLE);
							return true;
						}
						if (deviceProvider.isConnected()) {
							startActivityForResult(new Intent(
									getApplicationContext(),
									BluetoothDisconnectDialog.class),
									BLUETOOTH_REQUEST_DISCONNECT);
							return true;
						}
						BluetoothDiscoveryActivity
								.setDeviceProvider(deviceProvider);
						startActivityForResult(new Intent(
								getApplicationContext(),
								BluetoothDiscoveryActivity.class),
								BLUETOOTH_REQUEST_DISCOVERY);
						return true;
					}
				});
	}

	private Handler setupHandler() {
		// TODO Anpassen -- evtl. nur Toasts ausgeben
		// The Handler that gets information back from the BluetoothChatService
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == MESSAGE_LONG_TOAST) {
					Toast.makeText(getApplicationContext(),
							msg.getData().getString(MESSAGE_TEXT),
							Toast.LENGTH_LONG).show();
				} else if (msg.what == MESSAGE_SHORT_TOAST) {
					Toast.makeText(getApplicationContext(),
							msg.getData().getString(MESSAGE_TEXT),
							Toast.LENGTH_SHORT).show();

				} else if (msg.what == UPDATE_BLUETOOTHINFO) {
					updateBluetoothInfo();
				}
			}
		};
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		updateBluetoothInfo();
	}

	// Updates the summary of the bluetooth preference
	private void updateBluetoothInfo() {
		Preference bluetooth_pref = (Preference) findPreference(SETTINGS_BLUETOOTH);
		if (deviceProvider.isBluetoothDisabled()) {
			bluetooth_pref.setSummary(R.string.settings_bluetoothDisabled);
			return;
		}
		if (deviceProvider.isConnected()) {
			bluetooth_pref.setSummary(R.string.settings_bluetoothConnected
					+ deviceProvider.getConnectedDeviceName());
			return;
		}
		bluetooth_pref.setSummary(R.string.settings_bluetoothEnabled);
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (deviceProvider != null) {
			deviceProvider.stopService(); // TODO evtl weg, damit verbindung
											// bestehen bleibt -- oder onResume
											// wieder neue verbindung herstellen
			deviceProvider.unregisterReceiver(getApplicationContext());
			deviceProvider = null;
		}

		super.onDestroy();
	}

	@Override
	public void onActivityResult(int resCode, int reqCode, Intent data) {

		if (resCode == BLUETOOTH_REQUEST_ENABLE) {
			// If Enable-Bluetooth is true
			if (reqCode != Activity.RESULT_OK) {
				Toast.makeText(this, R.string.settings_bluetoothDisabled,
						Toast.LENGTH_SHORT).show();
			}
			// else {
			// updateBluetoothInfo();
			// }
		} else if (resCode == BLUETOOTH_REQUEST_DISCONNECT) {
			// If Bluetooth-Deivce should disconnect
			if (reqCode == Activity.RESULT_OK) {
				deviceProvider.stopService();
				// updateBluetoothInfo();
			}
		} else if (resCode == BLUETOOTH_REQUEST_DISCOVERY) {
			// If Bluetooth-Deivce was selected
			if (reqCode == Activity.RESULT_OK) {
				deviceProvider.connectDevice(data);
				// deviceProvider.connectDevice(data, true);
				// updateBluetoothInfo();
			}
		}
		updateBluetoothInfo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_settings, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_main:
			startActivity(new Intent(this, MainActivity.class));
			return true;
		case R.id.action_speedo:
			startActivity(new Intent(this, SpeedoActivity.class));
			return true;
		case R.id.action_help:
			startActivity(new Intent(this, HelpActivity.class));
			return true;
		case R.id.action_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
