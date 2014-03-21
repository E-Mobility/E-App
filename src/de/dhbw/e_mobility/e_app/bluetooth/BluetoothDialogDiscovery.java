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

package de.dhbw.e_mobility.e_app.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import de.dhbw.e_mobility.e_app.ActivityHandler;
import de.dhbw.e_mobility.e_app.R;

public class BluetoothDialogDiscovery extends Activity {

	private ActivityHandler activityHandler = ActivityHandler.getInstance();

	// BluetoothDeviceProvider provides all bluetooth stuff
	private static BluetoothDeviceProvider deviceProvider = BluetoothDeviceProvider
			.getInstance();

	private boolean hasPairedDevices;

	// // // Sets the device provider
	// public static void setDeviceProvider(
	// BluetoothDeviceProvider theDeviceProvider) {
	// deviceProvider = theDeviceProvider;
	// }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.dialog_discovery);

		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);

		// Initialize the button to perform device discovery
		Button but_scan = (Button) findViewById(R.id.discovery_scan);
		but_scan.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});

		// Tell the deviceProvider about the own Hanlder
		// deviceProvider.saveDiscoveryHandler(setupHandler());

		// Initialize the ArrayAdapters
		ArrayAdapter<String> pairedDevicesArrayAdapter = deviceProvider
				.getPairedArrayAdapter(this);
		ArrayAdapter<String> discoveredDevicesArrayAdapter = deviceProvider
				.getDiscoveredArrayAdapter(this);

		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.discovery_pairedDevicesList);
		pairedListView.setAdapter(pairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// Find and set up the ListView for newly discovered devices
		ListView discoveredDevicesListView = (ListView) findViewById(R.id.discovery_deviceList);
		discoveredDevicesListView.setAdapter(discoveredDevicesArrayAdapter);
		discoveredDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// Add paired devices to list
		hasPairedDevices = deviceProvider.initPairedDevices();
		if (hasPairedDevices) {
			((TextView) findViewById(R.id.discovery_devices))
					.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		activityHandler.add(this, setupHandler());
	}

	@Override
	protected void onStop() {
		super.onStop();
		activityHandler.del(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Make sure we're not doing discovery anymore
		deviceProvider.cancelDiscovery();
		finish();
	}

	private Handler setupHandler() {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == BluetoothDeviceProvider.BLUETOOTH_ACTION_DISCOVERY_FINISHED) {
					setProgressBarIndeterminateVisibility(false);
					setTitle(R.string.discovery_select);
					((Button) findViewById(R.id.discovery_scan))
							.setVisibility(View.VISIBLE);
					deviceProvider.updateDiscoveredDeviceListState();
				}
			}
		};
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.discovery_scanning);

		// Turn on sub-title for new devices
		((TextView) findViewById(R.id.discovery_devices))
				.setVisibility(View.VISIBLE);
		deviceProvider.clearDiscoveredDeviceList();

		deviceProvider.startDiscovery();
	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			if (av == findViewById(R.id.discovery_pairedDevicesList)
					&& !hasPairedDevices) {
				return;
			}

			// Cancel discovery because it's costly and we're about to connect
			deviceProvider.cancelDiscovery();

			// Get the device MAC address, which is the last 17 chars in the
			// View
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);

			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.putExtra(BluetoothDeviceProvider.BLUETOOTH_DEVICE_ADDRESS,
					address);

			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};
}
