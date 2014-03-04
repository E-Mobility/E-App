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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class SettingsActivity extends PreferenceActivity {
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	// Layout Views
	private TextView mTitle;
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	// Intent Request Codes
	protected static final int SET_BLUETOOTH_DEVICE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.activity_settings);
		enableBluetoothPref(false);

		// Get Bluetooth-Preference
		Preference pref_bluetooth = (Preference) findPreference("settings_bluetooth");

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.settings_bluetoothNotAvailable,
					Toast.LENGTH_LONG).show();
		} else {
			// Initialize the click events for the Bluetooth-Preference
			pref_bluetooth
					.setIntent(new Intent(this, DeviceListActivity.class));
			// TODO wenn schon verbunden, den aktuellen namen als summary
			// pref.setSummary("noch ein Device");
			pref_bluetooth
					.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							startActivityForResult(preference.getIntent(),
									SET_BLUETOOTH_DEVICE);
							return true;
						}
					});
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		// If Bluetooth isn't on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			enableBluetoothPref(true);
			if (mChatService == null)
				setupChat();
		}
	}

	/**
	 * Enables the Bluetooth-Preference.
	 * 
	 * @param val
	 */
	private void enableBluetoothPref(boolean val) {
		((Preference) findPreference("settings_bluetooth")).setEnabled(val);
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which
		// Bluetooth was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.

		// TODO keine ueberpruefung ob bluetooth nun aktiv??

		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

//		// Initialize the array adapter for the conversation thread
//		mConversationArrayAdapter = new ArrayAdapter<String>(this,
//				R.layout.message);
//		mConversationView = (ListView) findViewById(R.id.in);
//		mConversationView.setAdapter(mConversationArrayAdapter);
//
//		// Initialize the compose field with a listener for the return key
//		mOutEditText = (EditText) findViewById(R.id.edit_text_out);
//		mOutEditText.setOnEditorActionListener(mWriteListener);
//
//		// Initialize the send button with a listener that for click events
//		mSendButton = (Button) findViewById(R.id.button_send);
//		mSendButton.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				// Send a message using content of the edit text widget
//				TextView view = (TextView) findViewById(R.id.edit_text_out);
//				String message = view.getText().toString();
//				sendMessage(message);
//			}
//		});

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
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
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
	}

//	/**
//	 * Sends a message.
//	 * 
//	 * @param message
//	 *            A string of text to send.
//	 */
//	private void sendMessage(String message) {
//		// Check that we're actually connected before trying anything
//		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
//			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
//					.show();
//			return;
//		}
//
//		// Check that there's actually something to send
//		if (message.length() > 0) {
//			// Get the message bytes and tell the BluetoothChatService to write
//			byte[] send = message.getBytes();
//			mChatService.write(send);
//
//			// Reset out string buffer to zero and clear the edit text field
//			mOutStringBuffer.setLength(0);
//			mOutEditText.setText(mOutStringBuffer);
//		}
//	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
//			case MESSAGE_STATE_CHANGE:
//				if (D)
//					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
//				switch (msg.arg1) {
//				case BluetoothChatService.STATE_CONNECTED:
//					mTitle.setText(R.string.title_connected_to);
//					mTitle.append(mConnectedDeviceName);
//					mConversationArrayAdapter.clear();
//					break;
//				case BluetoothChatService.STATE_CONNECTING:
//					mTitle.setText(R.string.title_connecting);
//					break;
//				case BluetoothChatService.STATE_LISTEN:
//				case BluetoothChatService.STATE_NONE:
//					mTitle.setText(R.string.title_not_connected);
//					break;
//				}
//				break;
//			case MESSAGE_WRITE:
//				byte[] writeBuf = (byte[]) msg.obj;
//				// construct a string from the buffer
//				String writeMessage = new String(writeBuf);
//				mConversationArrayAdapter.add("Me:  " + writeMessage);
//				break;
//			case MESSAGE_READ:
//				byte[] readBuf = (byte[]) msg.obj;
//				// construct a string from the valid bytes in the buffer
//				String readMessage = new String(readBuf, 0, msg.arg1);
//				mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
//						+ readMessage);
//				break;
//			case MESSAGE_DEVICE_NAME:
//				// save the connected device's name
//				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//				Toast.makeText(getApplicationContext(),
//						"Connected to " + mConnectedDeviceName,
//						Toast.LENGTH_SHORT).show();
//				break;
//			case MESSAGE_TOAST:
//				Toast.makeText(getApplicationContext(),
//						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
//						.show();
//				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onActivityResult(int resCode, int reqCode, Intent data) {
		if (resCode == SET_BLUETOOTH_DEVICE) {
			// If Bluetooth-Deivce was selected
			if (reqCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
		} else if (resCode == REQUEST_ENABLE_BT) {
			// If Enable-Bluetooth is true
			if (reqCode == Activity.RESULT_OK) {
				enableBluetoothPref(true);
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				Toast.makeText(this, R.string.settings_bluetoothNotEnabled,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
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
