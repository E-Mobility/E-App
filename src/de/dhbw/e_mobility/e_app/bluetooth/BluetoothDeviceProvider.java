package de.dhbw.e_mobility.e_app.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import de.dhbw.e_mobility.e_app.R;
import de.dhbw.e_mobility.e_app.SettingsActivity;

public class BluetoothDeviceProvider {

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;
	// Intent Request Codes
	private final Handler mHandler;

	/**
	 * Constructor.
	 */
	public BluetoothDeviceProvider(Handler theHandler) {
		mHandler = theHandler;

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
			mHandler.obtainMessage(SettingsActivity.MESSAGE_TOAST,
					R.string.settings_bluetoothNotAvailable).sendToTarget();
		}
		
		mChatService = new BluetoothChatService( mHandler, mBluetoothAdapter);
//		mChatService.start();
	}

	// TODO in eine Comand senden / auswerten Klasse
	// /**
	// * Sends a message.
	// *
	// * @param message
	// * A string of text to send.
	// */
	// private void sendMessage(String message) {
	// // Check that we're actually connected before trying anything
	// if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
	// Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
	// .show();
	// return;
	// }
	//
	// // Check that there's actually something to send
	// if (message.length() > 0) {
	// // Get the message bytes and tell the BluetoothChatService to write
	// byte[] send = message.getBytes();
	// mChatService.write(send);
	//
	// // Reset out string buffer to zero and clear the edit text field
	// mOutStringBuffer.setLength(0);
	// mOutEditText.setText(mOutStringBuffer);
	// }
	// }

	// Connect to a bluetooth device
	public void connectDevice(Intent data, boolean secure) {
		// Stop discovering for devices
		mBluetoothAdapter.cancelDiscovery();
		
		// Get the device MAC address
		String address = data.getExtras().getString(
				BluetoothDiscoveryActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}

	// Stop the Bluetooth chat services
	public void stopService() {
		if (mChatService != null)
			mChatService.stop();
	}

	// Checks if bluetooth is disabled
	public boolean isBluetoothDisabled() {
		return !mBluetoothAdapter.isEnabled();
	}

	// Checks if bluetooth is connected with a device
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	// Returns the connected device name
	public String getConnectedDeviceName() {
		// TODO Auto-generated method stub
		return "testDevice";
	}

	// Returns the question for enabling bluetooth
	public String getBluetoothEnableQuestion() {
		return BluetoothAdapter.ACTION_REQUEST_ENABLE;
	}

}
