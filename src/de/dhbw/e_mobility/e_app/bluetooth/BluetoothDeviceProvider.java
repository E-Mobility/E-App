package de.dhbw.e_mobility.e_app.bluetooth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import de.dhbw.e_mobility.e_app.ActivityHandler;
import de.dhbw.e_mobility.e_app.R;

public class BluetoothDeviceProvider {

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the connection services
	private BluetoothConnectionService mConnectionService = null;

	// private BluetoothDevice mDevice;

	// private final Handler mHandler;
	// private Handler discoveryHandler;

	private MyBroadcastReceiver mReceiver;

	// BluetoothDevice-ACTIONS
	public static final int BLUETOOTH_ACTION_ACL_CONNECTED = 1;
	public static final int BLUETOOTH_ACTION_ACL_CONNECTED_BONDED = 12;
	public static final int BLUETOOTH_ACTION_ACL_DISCONNECT_REQUESTED = 2;
	public static final int BLUETOOTH_ACTION_ACL_DISCONNECTED = 3;
	public static final int BLUETOOTH_ACTION_BOND_STATE_CHANGED_BOND_BONDED = 4;
	public static final int BLUETOOTH_ACTION_BOND_STATE_CHANGED_BOND_BONDING = 5;
	public static final int BLUETOOTH_ACTION_BOND_STATE_CHANGED_BOND_NONE = 6;
	public static final int BLUETOOTH_ACTION_CLASS_CHANGED = 7;
	public static final int BLUETOOTH_ACTION_FOUND = 8;
	public static final int BLUETOOTH_ACTION_NAME_CHANGED = 9;
	public static final int BLUETOOTH_ACTION_PAIRING_REQUEST = 10;
	public static final int BLUETOOTH_ACTION_UUID = 11;

	// BluetoothAdapter-ACTIONS
	public static final int BLUETOOTH_ACTION_CONNECTION_STATE_CHANGED_CONNECTED = 21;
	public static final int BLUETOOTH_ACTION_CONNECTION_STATE_CHANGED_CONNECTING = 22;
	public static final int BLUETOOTH_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTED = 23;
	public static final int BLUETOOTH_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTING = 24;
	public static final int BLUETOOTH_ACTION_DISCOVERY_FINISHED = 25;
	public static final int BLUETOOTH_ACTION_DISCOVERY_STARTED = 26;
	public static final int BLUETOOTH_ACTION_LOCAL_NAME_CHANGED = 27;
	public static final int BLUETOOTH_ACTION_REQUEST_DISCOVERABLE = 28;
	public static final int BLUETOOTH_ACTION_REQUEST_ENABLE = 29;
	public static final int BLUETOOTH_ACTION_SCAN_MODE_CHANGED = 30;
	public static final int BLUETOOTH_ACTION_STATE_CHANGED = 31;

	// Result Intent
	public static final String BLUETOOTH_DEVICE_ADDRESS = "device_address";
	public static final String BLUETOOTH_DEVICE_NAME = "device_name";
	public static final String BLUETOOTH_DEVICE_UUID = "device_uuid";

	// Lists for displaying the paired and discovered devices
	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	private ArrayAdapter<String> discoveredDevicesArrayAdapter;

	private static BluetoothDeviceProvider instance;
	private ActivityHandler activityHandler = ActivityHandler.getInstance();

	public static synchronized BluetoothDeviceProvider getInstance() {
		if (instance == null) {
			instance = new BluetoothDeviceProvider();
		}
		return instance;
	}

	/**
	 * Constructor.
	 */
	// private BluetoothDeviceProvider(Handler theHandler, Context context) {
	private BluetoothDeviceProvider() {
		// mHandler = theHandler;

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
			activityHandler.fireToast(R.string.settings_bluetoothNotAvailable);
			// showToast(R.string.settings_bluetoothNotAvailable);
		}

		String password = "1234";

		// mConnectionService = new BluetoothConnectionService(setupHandler(), password);
		mConnectionService = new BluetoothConnectionService(password);

		// Set up own BroadcastReceiver
		mReceiver = new MyBroadcastReceiver(setupHandler());

		Context theContext = activityHandler.getMainContext();

		// Register Actions for BluetoothDevice
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothDevice.ACTION_ACL_CONNECTED));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothDevice.ACTION_ACL_DISCONNECTED));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothDevice.ACTION_BOND_STATE_CHANGED));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothDevice.ACTION_CLASS_CHANGED));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothDevice.ACTION_FOUND));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothDevice.ACTION_NAME_CHANGED));

		// Register Actions for BluetoothAdapter
		theContext.registerReceiver(mReceiver, new IntentFilter(
				"android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED"));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_STARTED));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_REQUEST_ENABLE));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
		theContext.registerReceiver(mReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_STATE_CHANGED));

		// Register Hidden bluetooth actions
		theContext.registerReceiver(mReceiver, new IntentFilter(
				"android.bleutooth.device.action.UUID"));
	}

	// private String getString(int resId) {
	// return activityHandler.getStr(resId);
	// }
	//
	// private void showToast(int resId, String txt) {
	// showToast(getString(resId) + txt);
	// }
	//
	// private void showToast(int resId) {
	// showToast(resId, "");
	// }
	//
	// private void showToast(String txt) {
	// // Bundle bundle = new Bundle();
	// // bundle.putString(SettingsActivity.MESSAGE_TEXT, txt);
	// // Message msg = new Message();
	// // msg.what = SettingsActivity.MESSAGE_LONG_TOAST;
	// // msg.setData(bundle);
	// // mHandler.sendMessage(msg);
	// activityHandler.fireToHandler(SettingsActivity.MESSAGE_LONG_TOAST, txt);
	// }

	// Unregister the BroadcastReceiver
	public void unregisterReceiver(Context theContext) {
		theContext.unregisterReceiver(mReceiver);
	}

	// Connect to a bluetooth device
	public void connectDevice(Intent data) {
		// TODO
		// hier überprüfen ob device schon bonded, connected,...
		// dann settingselemnt anpassen
		// TODO

		// Get the device MAC address
		String address = data.getExtras().getString(BLUETOOTH_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

		createBond(device);
		// TODO in thread auslagern

		mConnectionService.connect(device);

		// TODO und dann?? --> benötige ich die UUID zum verbinden??

		// try {
		// // Try to get the UUID
		// Class cl = Class.forName("android.bluetooth.BluetoothDevice");
		// Class[] par = {};
		// Method method = cl.getMethod("fetchUuidsWithSdp", par);
		// Object[] args = {};
		// method.invoke(device, args);
		// // Thanks to
		// //
		// http://wiresareobsolete.com/wordpress/2010/11/android-bluetooth-rfcomm/
		// // TODO catches verarbeiten
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// } catch (SecurityException e) {
		// e.printStackTrace();
		// } catch (NoSuchMethodException e) {
		// e.printStackTrace();
		// } catch (IllegalArgumentException e) {
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// e.printStackTrace();
		// } catch (InvocationTargetException e) {
		// e.printStackTrace();
		// }
	}

	// Stop the bluetooth connection services
	public void stopService() {
		if (mConnectionService != null) {
			mConnectionService.stop();
		}
	}

	// Checks if bluetooth is disabled
	public boolean isBluetoothDisabled() {
		return !mBluetoothAdapter.isEnabled();
	}

	// // Checks if bluetooth is connected with a device
	// public boolean isConnected() {
	// mConnectionService
	// return (mDevice != null);
	// }

	// Returns the connected device name
	public String getConnectedDeviceName() {
		if (mConnectionService != null) {
			BluetoothDevice tmpDevice = mConnectionService.getRemoteDevice();
			if (tmpDevice != null) {
				return tmpDevice.getName();
			}
		}
		return null;

		// if (isConnected()) {
		// return mDevice.getName();
		// }
		// return null;
	}

	// Returns the question for enabling bluetooth
	public String getBluetoothEnableQuestion() {
		return BluetoothAdapter.ACTION_REQUEST_ENABLE;
	}

	// Creates a bond between this devices
	public boolean createBond(BluetoothDevice btDevice) {
		// If not already bonded
		if (btDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
			Class bluetoothDeviceClass;
			try {
				bluetoothDeviceClass = Class
						.forName("android.bluetooth.BluetoothDevice");
				Method createBondMethod = bluetoothDeviceClass
						.getMethod("createBond");
				Boolean returnValue = (Boolean) createBondMethod
						.invoke(btDevice);
				return returnValue.booleanValue();
				// TODO catches verarbeiten
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return false;
		}
		return false;
	}

	// Starts discovering for bluetooth devices
	public void startDiscovery() {
		// If it's already discover first cancel it
		if (mBluetoothAdapter.isDiscovering()) {
			cancelDiscovery();
		}

		// Start discovery
		mBluetoothAdapter.startDiscovery();
	}

	// Stops discovering for bluetooth devices
	public void cancelDiscovery() {
		mBluetoothAdapter.cancelDiscovery();
	}

	private Handler setupHandler() {
		return new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// BluetoothDevice-ACTIONS
				if (msg.what == BLUETOOTH_ACTION_ACL_CONNECTED) {
				} else if (msg.what == BLUETOOTH_ACTION_ACL_CONNECTED_BONDED) {
				} else if (msg.what == BLUETOOTH_ACTION_ACL_DISCONNECT_REQUESTED) {
				} else if (msg.what == BLUETOOTH_ACTION_ACL_DISCONNECTED) {
				} else if (msg.what == BLUETOOTH_ACTION_BOND_STATE_CHANGED_BOND_BONDED) {
					// showToast(R.string.bluetooth_bound_bounded, msg.getData()
					// .getString(BLUETOOTH_DEVICE_NAME));
					// mDevice = mBluetoothAdapter.getRemoteDevice(msg.getData()
					// .getString(BLUETOOTH_DEVICE_ADDRESS));
					// activityHandler.fireToHandler(SettingsActivity.UPDATE_BLUETOOTHINFO);
					activityHandler.fireToast(R.string.bluetooth_bound_bounded,
							msg.getData().getString(BLUETOOTH_DEVICE_NAME));
					// mHandler.obtainMessage(
					// SettingsActivity.UPDATE_BLUETOOTHINFO)
					// .sendToTarget();
				} else if (msg.what == BLUETOOTH_ACTION_BOND_STATE_CHANGED_BOND_BONDING) {
					activityHandler
							.fireToast(R.string.bluetooth_bound_bounding);
					// showToast(R.string.bluetooth_bound_bounding);
				} else if (msg.what == BLUETOOTH_ACTION_BOND_STATE_CHANGED_BOND_NONE) {
					// showToast(R.string.bluetooth_bound_none);
					activityHandler.fireToast(R.string.bluetooth_bound_none);
					// mDevice = null;
					// activityHandler.fireToHandler(SettingsActivity.UPDATE_BLUETOOTHINFO);
					// mHandler.obtainMessage(
					// SettingsActivity.UPDATE_BLUETOOTHINFO)
					// .sendToTarget();
				} else if (msg.what == BLUETOOTH_ACTION_CLASS_CHANGED) {
				} else if (msg.what == BLUETOOTH_ACTION_FOUND) {
					// Add device to discoveredDevicesArrayAdapter
					Bundle bundle = msg.getData();
					discoveredDevicesArrayAdapter.add(bundle
							.getString(BLUETOOTH_DEVICE_NAME)
							+ "\n"
							+ bundle.getString(BLUETOOTH_DEVICE_ADDRESS));
				} else if (msg.what == BLUETOOTH_ACTION_NAME_CHANGED) {
					// Bundle bundle = msg.getData();
					// BluetoothDevice device = mBluetoothAdapter
					// .getRemoteDevice(bundle
					// .getString(BLUETOOTH_DEVICE_ADDRESS));
					// String deviceName = msg.getData().getString(
					// BLUETOOTH_DEVICE_NAME);
					// System.out.println("Name geändert: " + deviceName);
				} else if (msg.what == BLUETOOTH_ACTION_PAIRING_REQUEST) {
				} else if (msg.what == BLUETOOTH_ACTION_UUID) {
					// Bundle bundle = msg.getData();
					// BluetoothDevice device = mBluetoothAdapter
					// .getRemoteDevice(bundle
					// .getString(BLUETOOTH_DEVICE_ADDRESS));
					// ParcelUuid deviceUUID =
					// ParcelUuid.fromString(msg.getData()
					// .getString(BLUETOOTH_DEVICE_UUID));
					// mConnectionService.connect(device);
					// TODO wird jetzt nicht mehr benötigt?
				}

				// BluetoothAdapter-ACTIONS
				else if (msg.what == BLUETOOTH_ACTION_CONNECTION_STATE_CHANGED_CONNECTED) {
					// TODO wird das überhaupt gefeuert??
					// mDevice = mBluetoothAdapter.getRemoteDevice(msg.getData()
					// .getString(BLUETOOTH_DEVICE_ADDRESS));
					activityHandler.fireToast(
							R.string.settings_bluetoothConnected, msg.getData()
									.getString(BLUETOOTH_DEVICE_NAME));
					// showToast(R.string.settings_bluetoothConnected, msg
					// .getData().getString(BLUETOOTH_DEVICE_NAME));
				} else if (msg.what == BLUETOOTH_ACTION_CONNECTION_STATE_CHANGED_CONNECTING) {
				} else if (msg.what == BLUETOOTH_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTED) {
					// TODO wird das überhaupt gefeuert??
					activityHandler
							.fireToast(R.string.settings_bluetoothDisconnected);
					// mDevice = null;
				} else if (msg.what == BLUETOOTH_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTING) {
					// mDevice = null;
				} else if (msg.what == BLUETOOTH_ACTION_DISCOVERY_FINISHED) {
					// When discovery is finished, change the Activity title
					if (discoveredDevicesArrayAdapter.getCount() == 0) {
						// discoveredDevicesArrayAdapter
						// .add(getString(R.string.deviceList_noPairedDevices));
						activityHandler.fireToast(R.string.discovery_noDevices);
					}
					activityHandler
							.fireToHandler(BLUETOOTH_ACTION_DISCOVERY_FINISHED);
					// discoveryHandler.obtainMessage(
					// BLUETOOTH_ACTION_DISCOVERY_FINISHED).sendToTarget();
				} else if (msg.what == BLUETOOTH_ACTION_DISCOVERY_STARTED) {
				} else if (msg.what == BLUETOOTH_ACTION_LOCAL_NAME_CHANGED) {
				} else if (msg.what == BLUETOOTH_ACTION_REQUEST_DISCOVERABLE) {
				} else if (msg.what == BLUETOOTH_ACTION_REQUEST_ENABLE) {
				} else if (msg.what == BLUETOOTH_ACTION_SCAN_MODE_CHANGED) {
				} else if (msg.what == BLUETOOTH_ACTION_STATE_CHANGED) {
				}
			}
		};
	}

	// Initialize paired array adapters
	public ArrayAdapter<String> getPairedArrayAdapter(
			BluetoothDialogDiscovery bluetoothDialogDiscovery) {
		pairedDevicesArrayAdapter = new ArrayAdapter<String>(
				bluetoothDialogDiscovery, R.layout.dialog_discovery_list_item);
		return pairedDevicesArrayAdapter;
	}

	// Initialize discovered array adapters
	public ArrayAdapter<String> getDiscoveredArrayAdapter(
			BluetoothDialogDiscovery bluetoothDialogDiscovery) {
		discoveredDevicesArrayAdapter = new ArrayAdapter<String>(
				bluetoothDialogDiscovery, R.layout.dialog_discovery_list_item);
		return discoveredDevicesArrayAdapter;
	}

	public boolean initPairedDevices() {
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				pairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			}
			return true;
		}

		// If there are no paired devices, set a default value
		pairedDevicesArrayAdapter.add(activityHandler
				.getStr(R.string.discovery_noPairedDevices));
		return false;
	}

	public void clearDiscoveredDeviceList() {
		discoveredDevicesArrayAdapter.clear();
	}

	public void updateDiscoveredDeviceListState() {
		if (discoveredDevicesArrayAdapter.getCount() == 0) {
			clearDiscoveredDeviceList();
			discoveredDevicesArrayAdapter.add(activityHandler
					.getStr(R.string.discovery_noDevices));
		}
	}

	// // Saves the given Handler
	// public void saveDiscoveryHandler(Handler theHandler) {
	// discoveryHandler = theHandler;
	// }
}
