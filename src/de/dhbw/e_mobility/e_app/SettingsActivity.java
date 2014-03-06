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

import android.annotation.SuppressLint;
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
	public static final int MESSAGE_TOAST = 5;

	// Intent Request Codes
	protected static final int BLUETOOTH_REQUEST_ENABLE = 1;
	protected static final int BLUETOOTH_REQUEST_DISCONNECT = 2;
	protected static final int BLUETOOTH_REQUEST_DISCOVERY = 3;

	// Settings Items
	private BluetoothDeviceProvider deviceProvider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.activity_settings);

		deviceProvider = new BluetoothDeviceProvider(setupHandler());

		// Get Bluetooth-Preference
		Preference pref_bluetooth = (Preference) findPreference("settings_bluetooth");

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
							startActivityForResult(
									new Intent(
											""
													+ R.string.settings_bluetoothRequestDisconnect),
									BLUETOOTH_REQUEST_DISCONNECT);
							return true;
						}
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
				if (msg.what == MESSAGE_TOAST) {
					Toast.makeText(getApplicationContext(), msg.arg1,
							Toast.LENGTH_LONG).show();
				}

				switch (msg.what) {
				// case MESSAGE_STATE_CHANGE:
				// if (D)
				// Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				// switch (msg.arg1) {
				// case BluetoothChatService.STATE_CONNECTED:
				// mTitle.setText(R.string.title_connected_to);
				// mTitle.append(mConnectedDeviceName);
				// mConversationArrayAdapter.clear();
				// break;
				// case BluetoothChatService.STATE_CONNECTING:
				// mTitle.setText(R.string.title_connecting);
				// break;
				// case BluetoothChatService.STATE_LISTEN:
				// case BluetoothChatService.STATE_NONE:
				// mTitle.setText(R.string.title_not_connected);
				// break;
				// }
				// break;
				// case MESSAGE_WRITE:
				// byte[] writeBuf = (byte[]) msg.obj;
				// // construct a string from the buffer
				// String writeMessage = new String(writeBuf);
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				// break;
				// case MESSAGE_READ:
				// byte[] readBuf = (byte[]) msg.obj;
				// // construct a string from the valid bytes in the buffer
				// String readMessage = new String(readBuf, 0, msg.arg1);
				// mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
				// + readMessage);
				// break;
				// case MESSAGE_DEVICE_NAME:
				// // save the connected device's name
				// mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				// Toast.makeText(getApplicationContext(),
				// "Connected to " + mConnectedDeviceName,
				// Toast.LENGTH_SHORT).show();
				// break;
				// case MESSAGE_TOAST:
				// Toast.makeText(getApplicationContext(),
				// msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
				// .show();
				// break;
				default:
					break;
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
		Preference bluetooth_pref = (Preference) findPreference("settings_bluetooth");
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
		super.onDestroy();
		deviceProvider.stopService();

	}

	@Override
	public void onActivityResult(int resCode, int reqCode, Intent data) {


		if (resCode == BLUETOOTH_REQUEST_ENABLE) {
			// If Enable-Bluetooth is true
			if (reqCode == Activity.RESULT_OK) {
				updateBluetoothInfo();
			} else {
				Toast.makeText(this, R.string.settings_bluetoothDisabled,
						Toast.LENGTH_SHORT).show();
			}
		} else if (resCode == BLUETOOTH_REQUEST_DISCONNECT) {
			// If Bluetooth-Deivce should disconnect
			if (reqCode == Activity.RESULT_OK) {
				deviceProvider.stopService();
				updateBluetoothInfo();
			}
		} else if (resCode == BLUETOOTH_REQUEST_DISCOVERY) {
			// If Bluetooth-Deivce was selected
			if (reqCode == Activity.RESULT_OK) {
				deviceProvider.connectDevice(data, false);
//				deviceProvider.connectDevice(data, true);
				updateBluetoothInfo();
			}
		}
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
