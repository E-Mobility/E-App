package de.dhbw.e_mobility.e_app.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

public class MyBroadcastReceiver extends BroadcastReceiver {

	private Handler deviceProviderHandler;
	private boolean isACLconnected = false;

	// CONNECTION_STATE for BluetoothAdapter (Ab API 11 implementiert)
	public static final int STATE_DISCONNECTED = 0;
	public static final int STATE_CONNECTING = 1;
	public static final int STATE_CONNECTED = 2;
	public static final int STATE_DISCONNECTING = 3;

	/**
	 * Constructor.
	 */
	public MyBroadcastReceiver(Handler theHandler) {
		deviceProviderHandler = theHandler;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		// BluetoothDevice-ACTIONS
		if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
			Log.d("BluetoothDevice-ACTION", "ACTION_ACL_CONNECTED");
			isACLconnected = true;
			deviceProviderHandler.obtainMessage(
					BluetoothDeviceProvider.BLUETOOTH_ACTION_ACL_CONNECTED)
					.sendToTarget();
		} else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED
				.equals(action)) {
			Log.d("BluetoothDevice-ACTION", "ACTION_ACL_DISCONNECT_REQUESTED");
			deviceProviderHandler
					.obtainMessage(
							BluetoothDeviceProvider.BLUETOOTH_ACTION_ACL_DISCONNECT_REQUESTED)
					.sendToTarget();
		} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
			Log.d("BluetoothDevice-ACTION", "ACTION_ACL_DISCONNECTED");
			isACLconnected = false;
			deviceProviderHandler.obtainMessage(
					BluetoothDeviceProvider.BLUETOOTH_ACTION_ACL_DISCONNECTED)
					.sendToTarget();
		} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
			BluetoothDevice deviceExtra = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			// TODO funktioniert nicht?
			// int extraState =
			// intent.getParcelableExtra(BluetoothDevice.EXTRA_BOND_STATE);
			int extraState = deviceExtra.getBondState();
			Bundle bundle = new Bundle();
			Message msg = new Message();
			if (extraState == BluetoothDevice.BOND_BONDED) {
				Log.d("BluetoothDevice-ACTION",
						"ACTION_BOND_STATE_CHANGED-BOND_BONDED");
				bundle.putString(BluetoothDeviceProvider.BLUETOOTH_DEVICE_NAME,
						deviceExtra.getName());
				bundle.putString(BluetoothDeviceProvider.BLUETOOTH_DEVICE_ADDRESS,
						deviceExtra.getAddress());
				msg.what = BluetoothDeviceProvider.BLUETOOTH_ACTION_BOND_STATE_CHANGED_BOND_BONDED;
			} else if (extraState == BluetoothDevice.BOND_BONDING) {
				Log.d("BluetoothDevice-ACTION",
						"ACTION_BOND_STATE_CHANGED-BOND_BONDING");
				bundle.putString(BluetoothDeviceProvider.BLUETOOTH_DEVICE_NAME,
						deviceExtra.getName());
				msg.what = BluetoothDeviceProvider.BLUETOOTH_ACTION_BOND_STATE_CHANGED_BOND_BONDING;
			} else if (extraState == BluetoothDevice.BOND_NONE) {
				Log.d("BluetoothDevice-ACTION",
						"ACTION_BOND_STATE_CHANGED-BOND_NONE");
				msg.what = BluetoothDeviceProvider.BLUETOOTH_ACTION_BOND_STATE_CHANGED_BOND_NONE;
			}
			msg.setData(bundle);
			deviceProviderHandler.sendMessage(msg);
		} else if (BluetoothDevice.ACTION_CLASS_CHANGED.equals(action)) {
			Log.d("BluetoothDevice-ACTION", "ACTION_CLASS_CHANGED");
			deviceProviderHandler.obtainMessage(
					BluetoothDeviceProvider.BLUETOOTH_ACTION_CLASS_CHANGED)
					.sendToTarget();
		} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			Log.d("BluetoothDevice-ACTION", "ACTION_FOUND");

			// When discovery finds a device
			// Get the BluetoothDevice object from the Intent
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			// If it's already paired, skip it, because it's been listed
			// already
			if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
				Bundle bundle = new Bundle();
				bundle.putString(BluetoothDeviceProvider.BLUETOOTH_DEVICE_NAME,
						device.getName());
				bundle.putString(
						BluetoothDeviceProvider.BLUETOOTH_DEVICE_ADDRESS,
						device.getAddress());
				Message msg = new Message();
				msg.what = BluetoothDeviceProvider.BLUETOOTH_ACTION_FOUND;
				msg.setData(bundle);
				deviceProviderHandler.sendMessage(msg);
			}
		} else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
			Log.d("BluetoothDevice-ACTION", "ACTION_NAME_CHANGED");
			deviceProviderHandler.obtainMessage(
					BluetoothDeviceProvider.BLUETOOTH_ACTION_NAME_CHANGED)
					.sendToTarget();
		} else if (action
				.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
			Log.d("BluetoothDevice-ACTION", "ACTION_PAIRING_REQUEST");
			deviceProviderHandler.obtainMessage(
					BluetoothDeviceProvider.BLUETOOTH_ACTION_PAIRING_REQUEST)
					.sendToTarget();
		} else if (action.equals("android.bleutooth.device.action.UUID")) {
			Log.d("BluetoothDevice-ACTION",
					"android.bleutooth.device.action.UUID");
			if (isACLconnected) {
				isACLconnected = false;
				BluetoothDevice deviceExtra = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Parcelable[] uuidExtra = intent
						.getParcelableArrayExtra("android.bluetooth.device.extra.UUID");
				Bundle bundle = new Bundle();
				bundle.putString(BluetoothDeviceProvider.BLUETOOTH_DEVICE_UUID,
						uuidExtra[0].toString());
				bundle.putString(
						BluetoothDeviceProvider.BLUETOOTH_DEVICE_ADDRESS,
						deviceExtra.getAddress());
				Message msg = new Message();
				msg.what = BluetoothDeviceProvider.BLUETOOTH_ACTION_UUID;
				msg.setData(bundle);
				deviceProviderHandler.sendMessage(msg);
			}
		}

		// BluetoothAdapter-ACTIONS
		else if (action
				.equals("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")) {
			BluetoothDevice deviceExtra = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			int extraState = intent
					.getParcelableExtra("android.bluetooth.adapter.extra.CONNECTION_STATE");
			Bundle bundle = new Bundle();
			Message msg = new Message();
			if (extraState == STATE_CONNECTED) {
				Log.d("BluetoothAdapter-ACTION",
						"ACTION_CONNECTION_STATE_CHANGED-STATE_CONNECTED");
				bundle.putString(BluetoothDeviceProvider.BLUETOOTH_DEVICE_NAME,
						deviceExtra.getName());
				bundle.putString(
						BluetoothDeviceProvider.BLUETOOTH_DEVICE_ADDRESS,
						deviceExtra.getAddress());
				msg.what = BluetoothDeviceProvider.BLUETOOTH_ACTION_CONNECTION_STATE_CHANGED_CONNECTED;
			} else if (extraState == STATE_CONNECTING) {
				Log.d("BluetoothAdapter-ACTION",
						"ACTION_CONNECTION_STATE_CHANGED-STATE_CONNECTING");
				bundle.putString(BluetoothDeviceProvider.BLUETOOTH_DEVICE_NAME,
						deviceExtra.getName());
				msg.what = BluetoothDeviceProvider.BLUETOOTH_ACTION_CONNECTION_STATE_CHANGED_CONNECTING;
			} else if (extraState == STATE_DISCONNECTED) {
				Log.d("BluetoothAdapter-ACTION",
						"ACTION_CONNECTION_STATE_CHANGED-STATE_DISCONNECTED");
				msg.what = BluetoothDeviceProvider.BLUETOOTH_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTED;
			} else if (extraState == STATE_DISCONNECTING) {
				Log.d("BluetoothAdapter-ACTION",
						"ACTION_CONNECTION_STATE_CHANGED-STATE_DISCONNECTING");
				bundle.putString(BluetoothDeviceProvider.BLUETOOTH_DEVICE_NAME,
						deviceExtra.getName());
				msg.what = BluetoothDeviceProvider.BLUETOOTH_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTING;
			}
			msg.setData(bundle);
			deviceProviderHandler.sendMessage(msg);
		} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
			Log.d("BluetoothAdapter-ACTION", "ACTION_DISCOVERY_FINISHED");
			deviceProviderHandler
					.obtainMessage(
							BluetoothDeviceProvider.BLUETOOTH_ACTION_DISCOVERY_FINISHED)
					.sendToTarget();
		} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
			Log.d("BluetoothAdapter-ACTION", "ACTION_DISCOVERY_STARTED");
			deviceProviderHandler.obtainMessage(
					BluetoothDeviceProvider.BLUETOOTH_ACTION_DISCOVERY_STARTED)
					.sendToTarget();
		} else if (BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED.equals(action)) {
			Log.d("BluetoothAdapter-ACTION", "ACTION_LOCAL_NAME_CHANGED");
			deviceProviderHandler
					.obtainMessage(
							BluetoothDeviceProvider.BLUETOOTH_ACTION_LOCAL_NAME_CHANGED)
					.sendToTarget();
		} else if (BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE.equals(action)) {
			Log.d("BluetoothAdapter-ACTION", "ACTION_REQUEST_DISCOVERABLE");
			deviceProviderHandler
					.obtainMessage(
							BluetoothDeviceProvider.BLUETOOTH_ACTION_REQUEST_DISCOVERABLE)
					.sendToTarget();
		} else if (BluetoothAdapter.ACTION_REQUEST_ENABLE.equals(action)) {
			Log.d("BluetoothAdapter-ACTION", "ACTION_REQUEST_ENABLE");
			deviceProviderHandler.obtainMessage(
					BluetoothDeviceProvider.BLUETOOTH_ACTION_REQUEST_ENABLE)
					.sendToTarget();
		} else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
			Log.d("BluetoothAdapter-ACTION", "ACTION_SCAN_MODE_CHANGED");
			deviceProviderHandler.obtainMessage(
					BluetoothDeviceProvider.BLUETOOTH_ACTION_SCAN_MODE_CHANGED)
					.sendToTarget();
		} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
			Log.d("BluetoothAdapter-ACTION", "ACTION_STATE_CHANGED");
			deviceProviderHandler.obtainMessage(
					BluetoothDeviceProvider.BLUETOOTH_ACTION_STATE_CHANGED)
					.sendToTarget();
		}
	}
}
