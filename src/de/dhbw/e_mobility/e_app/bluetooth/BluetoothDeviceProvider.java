package de.dhbw.e_mobility.e_app.bluetooth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import de.dhbw.e_mobility.e_app.ActivityHandler;
import de.dhbw.e_mobility.e_app.R;

public class BluetoothDeviceProvider {

	// TODO alles abhängigkeiten von settings in activityhandler verlegen..
	// sonst kann das nicht laufen, wenn settings nicht aktiv..

	// State of connection/login
	private static final int STATE_NONE = 0;
	private static final int STATE_BLUETOOTH_NONE = 1;
	// private static final int STATE_BLUETOOTH_OFF = 2;
	private static final int STATE_BLUETOOTH_ON = 3;
	private static final int STATE_DEVICE_ONLINE = 4;
	private static final int STATE_PAIRED = 5;
	private static final int STATE_CONNECTED = 6;
	private static final int STATE_LOGEDIN = 7;
	private int state = STATE_NONE;

	// Get ActivityHandler object
	private ActivityHandler activityHandler = ActivityHandler.getInstance();

	// Handler of other objects
	// private Handler settingsHandler;
	// private Handler dialogDiscoveryHandler;

	// Private objects
	private BluetoothAdapter bluetoothAdapter = null;
	private MyBroadcastReceiver myBroadcastReceiver = null;
	private BluetoothConnectionService bluetoothConnectionService = null;
	private BluetoothDevice bluetoothDevice = null;

	// Lists for displaying the paired and discovered devices
	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	private ArrayAdapter<String> discoveredDevicesArrayAdapter;

	// Singelton
	private static BluetoothDeviceProvider instance;

	public static synchronized BluetoothDeviceProvider getInstance() {
		if (instance == null) {
			instance = new BluetoothDeviceProvider();
		}
		return instance;
	}

	/**
	 * Constructor.
	 */
	private BluetoothDeviceProvider() {
		// Give own Handler to ActivityHandler
		activityHandler.setHandler(ActivityHandler.HANDLLER_DEVICE_PROVIDER,
				setupHandler());
		// activityHandler.unsetHandler(ActivityHandler.HANDLLER_DEVICE_PROVIDER);
		// // TODO onRemove?

		// Get local bluetooth adapter
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			// No bluetooth adapter is available
			setState(STATE_BLUETOOTH_NONE);
			activityHandler.fireToast(R.string.settings_bluetoothNotAvailable);
			// activityHandler.fireToast(settingsHandler,
			// R.string.settings_bluetoothNotAvailable);
			return;
		}

		setState(STATE_NONE);

		String bluetoothPassword = "1234"; // TODO
		// bluetoothDevice is set by "setDevice(...)"

		Handler myHandler = setupHandler();
		// myBroadcastReceiver = new MyBroadcastReceiver(myHandler);
		myBroadcastReceiver = new MyBroadcastReceiver();
		bluetoothConnectionService = new BluetoothConnectionService(myHandler,
				bluetoothPassword);

		// Register actions for BluetoothDevice
		Context theContext = activityHandler.getMainContext();
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothDevice.ACTION_ACL_CONNECTED));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothDevice.ACTION_ACL_DISCONNECTED));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothDevice.ACTION_BOND_STATE_CHANGED));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothDevice.ACTION_CLASS_CHANGED));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothDevice.ACTION_FOUND));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothDevice.ACTION_NAME_CHANGED));

		// Register actions for BluetoothAdapter
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				"android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED"));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_STARTED));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_REQUEST_ENABLE));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				BluetoothAdapter.ACTION_STATE_CHANGED));

		// Register hidden bluetooth actions
		theContext.registerReceiver(myBroadcastReceiver, new IntentFilter(
				"android.bleutooth.device.action.UUID"));
	}

	// // Saves the activity handler from settings object
	// public void setSettingsActivityHandler(Handler theHandler) {
	// settingsHandler = theHandler;
	// }
	//
	// // Unsets the activity handler from settings object
	// public void unsetSettingsActivityHandler() {
	// settingsHandler = null;
	// }
	//
	// // Saves the activity handler from discovery dialog object
	// public void setBluetoothDialogDiscoveryHandler(Handler theHandler) {
	// dialogDiscoveryHandler = theHandler;
	// }
	//
	// // Unsets the activity handler from discovery dialog object
	// public void unsetBluetoothDialogDiscoveryHandler() {
	// dialogDiscoveryHandler = null;
	// }

	// Sets the bluetooth device
	public void setDevice(String address) {
		bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
	}

	// Unregister the BroadcastReceiver
	public void unregisterReceiver(Context theContext) {
		theContext.unregisterReceiver(myBroadcastReceiver);
	}

	// Sets the given state
	private void setState(int theState) {
		Log.d("DEVICE-PROVIDER", "setState() " + state + " -> " + theState);
		state = theState;
	}

	// Returns the current state
	private int getState() {
		return state;
	}

	private Handler setupHandler() {
		return new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg.what == BluetoothConnectionService.STATE_CONNECTED) {
					// // Device has a connection now
					// setState(STATE_CONNECTED);
					// doOnResult();
				} else if (msg.what == BluetoothConnectionService.STATE_LOGEDIN) {
					// Login was successful
					setState(STATE_LOGEDIN);
					doOnResult();
				} else if (msg.what == BluetoothConnectionService.STATE_LOGEDOUT) {
					// Logout was successful
					setState(STATE_NONE);
				}

				// BluetoothDevice-ACTIONS
				else if (msg.what == MyBroadcastReceiver.BT_ACTION_ACL_CONNECTED) {
					Log.d("BluetoothDevice-ACTIONS", "BT_ACTION_ACL_CONNECTED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_ACL_CONNECTED_BONDED) {
					Log.d("BluetoothDevice-ACTIONS",
							"BT_ACTION_ACL_CONNECTED_BONDED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_ACL_DISCONNECT_REQUESTED) {
					Log.d("BluetoothDevice-ACTIONS",
							"BT_ACTION_ACL_DISCONNECT_REQUESTED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_ACL_DISCONNECTED) {
					Log.d("BluetoothDevice-ACTIONS",
							"BT_ACTION_ACL_DISCONNECTED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_BOND_STATE_CHANGED_BOND_BONDED) {
					Log.d("BluetoothDevice-ACTIONS",
							"BT_ACTION_BOND_STATE_CHANGED_BOND_BONDED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_BOND_STATE_CHANGED_BOND_BONDING) {
					Log.d("BluetoothDevice-ACTIONS",
							"BT_ACTION_BOND_STATE_CHANGED_BOND_BONDING");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_BOND_STATE_CHANGED_BOND_NONE) {
					Log.d("BluetoothDevice-ACTIONS",
							"BT_ACTION_BOND_STATE_CHANGED_BOND_NONE");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_CLASS_CHANGED) {
					Log.d("BluetoothDevice-ACTIONS", "BT_ACTION_CLASS_CHANGED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_FOUND) {
					Log.d("BluetoothDevice-ACTIONS", "BT_ACTION_FOUND");
					// Add device to discoveredDevicesArrayAdapter
					Bundle bundle = msg.getData();
					discoveredDevicesArrayAdapter
							.add(bundle
									.getString(ActivityHandler.BLUETOOTH_DEVICE_NAME)
									+ "\n"
									+ bundle.getString(ActivityHandler.BLUETOOTH_DEVICE_ADDRESS));
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_NAME_CHANGED) {
					Log.d("BluetoothDevice-ACTIONS", "BT_ACTION_NAME_CHANGED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_PAIRING_REQUEST) {
					Log.d("BluetoothDevice-ACTIONS",
							"BT_ACTION_PAIRING_REQUEST");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_UUID) {
					Log.d("BluetoothDevice-ACTIONS", "BT_ACTION_UUID");
				}

				// BluetoothAdapter-ACTIONS
				else if (msg.what == MyBroadcastReceiver.BT_ACTION_CONNECTION_STATE_CHANGED_CONNECTED) {
					Log.d("BluetoothAdapter-ACTIONS",
							"BT_ACTION_CONNECTION_STATE_CHANGED_CONNECTED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_CONNECTION_STATE_CHANGED_CONNECTING) {
					Log.d("BluetoothAdapter-ACTIONS",
							"BT_ACTION_CONNECTION_STATE_CHANGED_CONNECTING");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTED) {
					Log.d("BluetoothAdapter-ACTIONS",
							"BT_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTING) {
					Log.d("BluetoothAdapter-ACTIONS",
							"BT_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTING");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_DISCOVERY_FINISHED) {
					Log.d("BluetoothAdapter-ACTIONS",
							"BT_ACTION_DISCOVERY_FINISHED");
					// When discovery is finished, change the Activity title
					if (discoveredDevicesArrayAdapter.getCount() == 0) {
						// discoveredDevicesArrayAdapter
						// .add(getString(R.string.deviceList_noPairedDevices));
						activityHandler.fireToast(R.string.discovery_noDevices);
						// activityHandler.fireToast(settingsHandler,
						// R.string.discovery_noDevices);
					}
					activityHandler.fireToHandler(
							ActivityHandler.HANDLLER_DISCOVERY,
							BluetoothDialogDiscovery.BT_DISCOVERY_FINISHED);
					// activityHandler.fireToHandler(dialogDiscoveryHandler,
					// BluetoothDialogDiscovery.BT_DISCOVERY_FINISHED);
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_DISCOVERY_STARTED) {
					Log.d("BluetoothAdapter-ACTIONS",
							"BT_ACTION_DISCOVERY_STARTED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_LOCAL_NAME_CHANGED) {
					Log.d("BluetoothAdapter-ACTIONS",
							"BT_ACTION_LOCAL_NAME_CHANGED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_REQUEST_DISCOVERABLE) {
					Log.d("BluetoothAdapter-ACTIONS",
							"BT_ACTION_REQUEST_DISCOVERABLE");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_REQUEST_ENABLE) {
					Log.d("BluetoothAdapter-ACTIONS",
							"BT_ACTION_REQUEST_ENABLE");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_SCAN_MODE_CHANGED) {
					Log.d("BluetoothAdapter-ACTIONS",
							"BT_ACTION_SCAN_MODE_CHANGED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_STATE_CHANGED) {
					Log.d("BluetoothAdapter-ACTIONS", "BT_ACTION_STATE_CHANGED");
				}
			}
		};
	}

	// Checks if bluetooth is turned on
	private void checkBluetooth() {
		if (bluetoothAdapter.isEnabled()) {
			setState(STATE_BLUETOOTH_ON);
			doOnResult();
		} else {
			Log.d("LOGIN", "Bluetooth is not turened on");
			activityHandler.fireToHandler(ActivityHandler.HANDLLER_SETTINGS,
					ActivityHandler.ASK_FOR_BLUETOOTH,
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			// activityHandler.fireToHandler(settingsHandler,
			// ActivityHandler.ASK_FOR_BLUETOOTH,
			// BluetoothAdapter.ACTION_REQUEST_ENABLE);
		}
	}

	// Checks if device is available
	private void checkDeviceIsOnline() {
		if (bluetoothDevice != null) { // TODO DEL
			// if(device.isOnline()) { //
			// MyBroadcastReceiver.BLUETOOTH_ACTION_ACL_CONNECTED
			setState(STATE_DEVICE_ONLINE);
			doOnResult();
			// } TODO
		} else {
			// Discovery for devices
			Log.d("LOGIN", "No device available / Device is offline");
			activityHandler.fireToHandler(ActivityHandler.HANDLLER_SETTINGS,
					ActivityHandler.START_DISCOVERING_DEVICES);
			// activityHandler.fireToHandler(settingsHandler,
			// ActivityHandler.START_DISCOVERING_DEVICES);
		}
	}

	// Checks if device is paired
	private void checkIsPaired() {
		if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED
				|| createBond()) {
			// oder if
			// (MyBroadcastReceiver.BLUETOOTH_ACTION_ACL_CONNECTED_BONDED == 0)
			// {// TODO?
			setState(STATE_PAIRED);
			doOnResult();
		} else {
			Log.d("LOGIN", "Device is not paired / Pairing unsuccessful");
			activityHandler.fireToast(R.string.device_not_paired);
			// activityHandler.fireToast(settingsHandler,
			// R.string.device_not_paired);
		}
	}

	// Checks connection and login state to controller
	private void checkConnectionAndLogin() {
		bluetoothConnectionService.checkConnectionAndLogin(bluetoothDevice);
	}

	// TODO DEL
	// // Do login to the controller
	// private void doLogin() {
	// bluetoothConnectionService.login();
	// }

	// Does the next step on login
	public void doOnResult() {
		if (getState() == STATE_NONE) {
			checkBluetooth();
		} else if (getState() == STATE_BLUETOOTH_ON) {
			activityHandler.fireToHandler(ActivityHandler.HANDLLER_SETTINGS,
					ActivityHandler.UPDATE_BT_INFO,
					R.string.bluetooth_info_enabled);
			// activityHandler.fireToHandler(settingsHandler,
			// ActivityHandler.UPDATE_BT_INFO,
			// R.string.bluetooth_info_enabled);
			checkDeviceIsOnline();
		} else if (getState() == STATE_DEVICE_ONLINE) {
			checkIsPaired();
		} else if (getState() == STATE_PAIRED) {
			checkConnectionAndLogin();
		} else if (getState() == STATE_CONNECTED) {
			// Log.d("LOGIN",
			// "Connection successful with " + bluetoothDevice.getName());
			// activityHandler.fireToHandler(settingsHandler,
			// ActivityHandler.UPDATE_BT_INFO,
			// R.string.bluetooth_info_connected,
			// bluetoothDevice.getName());
			// doLogin();
		} else if (getState() == STATE_LOGEDIN) {
			Log.d("LOGIN", "Login was successful (" + bluetoothDevice.getName()
					+ ")");
			activityHandler
					.fireToHandler(ActivityHandler.HANDLLER_SETTINGS,
							ActivityHandler.UPDATE_BT_INFO,
							R.string.bluetooth_info_loggedin,
							bluetoothDevice.getName());
			// .fireToHandler(settingsHandler,
			// ActivityHandler.UPDATE_BT_INFO,
			// R.string.bluetooth_info_loggedin,
			// bluetoothDevice.getName());
		} else if (getState() == STATE_BLUETOOTH_NONE) {
			// Es ist kein Bluetooth auf diesem Gerät möglich!
			Log.d("LOGIN", "Bluetooth not available");
			activityHandler.fireToHandler(ActivityHandler.HANDLLER_SETTINGS,
					ActivityHandler.UPDATE_BT_INFO,
					R.string.bluetooth_info_none);
			// activityHandler.fireToHandler(settingsHandler,
			// ActivityHandler.UPDATE_BT_INFO,
			// R.string.bluetooth_info_none);
		}
	}

	// Returns true if state is loged in
	public boolean isLogedin() {
		return (getState() == STATE_LOGEDIN);
	}

	// Start login to controller
	public void login() {
		if (getState() == STATE_BLUETOOTH_NONE) {
			Log.d("LOGIN", "Bluetooth is not available!");
			return;
		}
		setState(STATE_NONE);
		doOnResult();
	}

	// Logout from controller
	public void logout() {
		if (getState() == STATE_BLUETOOTH_NONE) {
			Log.d("LOGIN", "Bluetooth is not available!");
			return;
		}
		// bluetoothConnectionService.logout();
		bluetoothConnectionService.stop();
	}

	// Stop the bluetooth connection services
	public void stopService() { // TODO wird zu logout??
		if (bluetoothConnectionService != null) {
			bluetoothConnectionService.stop();
		}
	}

	// Creates a bond between this devices
	private boolean createBond() {
		// If not already bonded
		Class bluetoothDeviceClass;
		try {
			bluetoothDeviceClass = Class
					.forName("android.bluetooth.BluetoothDevice");
			Method createBondMethod = bluetoothDeviceClass
					.getMethod("createBond");
			Boolean returnValue = (Boolean) createBondMethod
					.invoke(bluetoothDevice);
			return returnValue.booleanValue();
			// TODO make something with the catches
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

	// Starts discovering for bluetooth devices
	public void startDiscovery() {
		// If it's already discover first cancel it
		if (bluetoothAdapter.isDiscovering()) {
			cancelDiscovery();
		}
		// Start discovery
		bluetoothAdapter.startDiscovery();
	}

	// Stops discovering for bluetooth devices
	public void cancelDiscovery() {
		bluetoothAdapter.cancelDiscovery();
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

	// Init the paired device list
	public boolean initPairedDevices() {
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter
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

	// Clears the list for the discoverd devices
	public void clearDiscoveredDeviceList() {
		discoveredDevicesArrayAdapter.clear();
	}

	// Updates the list for the discoverd devices
	public void updateDiscoveredDeviceListState() {
		if (discoveredDevicesArrayAdapter.getCount() == 0) {
			clearDiscoveredDeviceList();
			discoveredDevicesArrayAdapter.add(activityHandler
					.getStr(R.string.discovery_noDevices));
		}
	}
}
