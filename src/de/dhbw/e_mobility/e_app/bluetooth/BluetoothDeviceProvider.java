package de.dhbw.e_mobility.e_app.bluetooth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import de.dhbw.e_mobility.e_app.ActivityHandler;
import de.dhbw.e_mobility.e_app.R;
import de.dhbw.e_mobility.e_app.SettingsProvider;

public class BluetoothDeviceProvider {

	private enum BluetoothState {
		NONE, DISCONNECT, ON, DEVICE_ONLINE, PAIRED, CONNECTED, LOGGED_IN;
	}

	private BluetoothState bluetoothState;

	// Get ActivityHandler object
	private ActivityHandler activityHandler = ActivityHandler.getInstance();

	// Get SettingsProvider object
	private SettingsProvider settingsProvider = SettingsProvider.getInstance();

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

		// Get local bluetooth adapter
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			// No bluetooth adapter is available
			bluetoothState = BluetoothState.NONE;
			activityHandler.fireToast(R.string.settings_bluetoothNotAvailable);
			return;
		}
	}

	// Initializes all
	public void init() {
		setDevice(activityHandler.getDeviceAddress());

		bluetoothState = BluetoothState.DISCONNECT;

		myBroadcastReceiver = new MyBroadcastReceiver();
		bluetoothConnectionService = new BluetoothConnectionService();

		// Register actions for BluetoothDevice
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
		activityHandler.getMainContext().registerReceiver(
				myBroadcastReceiver,
				new IntentFilter(
						BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothDevice.ACTION_CLASS_CHANGED));
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothDevice.ACTION_FOUND));
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED));

		// Register actions for BluetoothAdapter
		activityHandler
				.getMainContext()
				.registerReceiver(
						myBroadcastReceiver,
						new IntentFilter(
								"android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED"));
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED));
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE));
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothAdapter.ACTION_REQUEST_ENABLE));
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

		// Register hidden bluetooth actions
		activityHandler.getMainContext().registerReceiver(myBroadcastReceiver,
				new IntentFilter("android.bleutooth.device.action.UUID"));

		// TODO TODO
	}

	// Sets the bluetooth device
	public void setDevice(String address) {
		if (address != null) {
			bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
		}
	}

	// Unregister the BroadcastReceiver
	public void unregisterReceiver() {
		activityHandler.getMainContext()
				.unregisterReceiver(myBroadcastReceiver);
	}

	private void updateBluetoothInfo(BluetoothInfoState theState) {
		if (theState == BluetoothInfoState.CONNECTED
				|| theState == BluetoothInfoState.LOGGED_IN) {
			settingsProvider.setBluetoothState(theState,
					bluetoothDevice.getName());
		} else {
			settingsProvider.setBluetoothState(theState);
		}
		activityHandler.fireToHandler(ActivityHandler.HANDLLER_SETTINGS,
				ActivityHandler.UPDATE_BT_INFO);
	}

	private Handler setupHandler() {
		return new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg.what == ActivityHandler.BLUETOOTH_INFO_STATE) {
					BluetoothInfoState infoState = BluetoothInfoState.values()[msg
							.getData().getInt(ActivityHandler.MESSAGE_NUMBER)];
					if (infoState == BluetoothInfoState.LOGGED_IN) {
						// Login was successful
						Log.d("LOGIN", "Login was successful ("
								+ bluetoothDevice.getName() + ")");
						bluetoothState = BluetoothState.LOGGED_IN;
						// TODO evtl. gleiche status..
						Log.d("UPDATE", "Handler: LOGGED_IN");
						updateBluetoothInfo(BluetoothInfoState.LOGGED_IN);
						doOnResult();
					} else if (infoState == BluetoothInfoState.NONE) {
						// Logout was successful
						bluetoothState = BluetoothState.DISCONNECT;
						Log.d("UPDATE", "Handler: DISCONNECT");
						updateBluetoothInfo(BluetoothInfoState.ON);
					}
				}

				// BluetoothDevice-ACTIONS
				else if (msg.what == MyBroadcastReceiver.BT_ACTION_ACL_CONNECTED) {
					Log.d("BluetoothDevice-ACTIONS", "BT_ACTION_ACL_CONNECTED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_ACL_CONNECTED_BONDED) {
					Log.d("BluetoothDevice-ACTIONS",
							"BT_ACTION_ACL_CONNECTED_BONDED");
					bluetoothState = BluetoothState.CONNECTED;
					Log.d("UPDATE", "Handler: CONNECTED (ACL)");
					updateBluetoothInfo(BluetoothInfoState.CONNECTED);
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_ACL_DISCONNECT_REQUESTED) {
					Log.d("BluetoothDevice-ACTIONS",
							"BT_ACTION_ACL_DISCONNECT_REQUESTED");
				} else if (msg.what == MyBroadcastReceiver.BT_ACTION_ACL_DISCONNECTED) {
					Log.d("BluetoothDevice-ACTIONS",
							"BT_ACTION_ACL_DISCONNECTED");
					bluetoothState = BluetoothState.DISCONNECT;
					Log.d("UPDATE", "Handler: DISCONNECT (ACL)");
					updateBluetoothInfo(BluetoothInfoState.ON);
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
			bluetoothState = BluetoothState.ON;
			doOnResult();
		} else {
			Log.d("LOGIN", "Bluetooth is not turened on");
			activityHandler.fireToHandler(ActivityHandler.HANDLLER_SETTINGS,
					ActivityHandler.ASK_FOR_BLUETOOTH,
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
		}
	}

	// Checks if device is available
	private void checkDeviceIsOnline() {
		if (bluetoothDevice != null) { // TODO DEL
			// if(device.isOnline()) { //
			// MyBroadcastReceiver.BLUETOOTH_ACTION_ACL_CONNECTED
			bluetoothState = BluetoothState.DEVICE_ONLINE;
			doOnResult();
			// } TODO
		} else {
			// Discovery for devices
			Log.d("LOGIN", "No device available / Device is offline");
			activityHandler.fireToHandler(ActivityHandler.HANDLLER_SETTINGS,
					ActivityHandler.START_DISCOVERING_DEVICES);
		}
	}

	// Checks if device is paired
	private void checkIsPaired() {
		if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED
				|| createBond()) {
			// oder if
			// (MyBroadcastReceiver.BLUETOOTH_ACTION_ACL_CONNECTED_BONDED == 0)
			// {// TODO?
			bluetoothState = BluetoothState.PAIRED;
			doOnResult();
		} else {
			Log.d("LOGIN", "Device is not paired / Pairing unsuccessful");
			activityHandler.fireToast(R.string.device_not_paired);
		}
	}

	// Checks connection and login state to controller
	private void checkConnectionAndLogin() {
		// Update password
		BluetoothCommands.LOGIN.setValue(activityHandler.getPassword());

		bluetoothConnectionService.checkConnectionAndLogin(bluetoothDevice);
		// TODO Meldung ausgeben wenn Passwort falsch
		// z.B. Warte 5 Sekunden, wenn kein login erfoglreich, dann meldung
	}

	// Does the next step on login
	public void doOnResult() {
		if (bluetoothState == BluetoothState.DISCONNECT) {
			checkBluetooth();
		} else if (bluetoothState == BluetoothState.ON) {
			Log.d("UPDATE", "onResult ON");
			updateBluetoothInfo(BluetoothInfoState.ON);
			checkDeviceIsOnline();
		} else if (bluetoothState == BluetoothState.DEVICE_ONLINE) {
			checkIsPaired();
		} else if (bluetoothState == BluetoothState.PAIRED) {
			checkConnectionAndLogin();
		} else if (bluetoothState == BluetoothState.CONNECTED) {
		} else if (bluetoothState == BluetoothState.LOGGED_IN) {
		} else if (bluetoothState == BluetoothState.NONE) {
			// Es ist kein Bluetooth auf diesem Gerät möglich!
			Log.d("LOGIN", "Bluetooth not available");
			Log.d("UPDATE", "onResult NONE");
			updateBluetoothInfo(BluetoothInfoState.NONE);
		}
	}

	// // Returns true if state is loged in
	// public boolean isLogedin() {
	// return (bluetoothState == BluetoothState.LOGGED_IN);
	// }

	// public String getBluetoothInfo() {
	// if (bluetoothState == BluetoothState.LOGGED_IN) {
	// return activityHandler.getStr(R.string.bluetooth_info_loggedin)
	// + bluetoothDevice.getName();
	// }
	// if (bluetoothState == BluetoothState.ON) {
	// return activityHandler.getStr(R.string.bluetooth_info_enabled);
	// }
	// return "";
	// }

	// Start login to controller
	public void login() {
		if (bluetoothState == BluetoothState.NONE) {
			Log.d("LOGIN", "Bluetooth is not available!");
			return;
		}
		bluetoothState = BluetoothState.DISCONNECT;
		doOnResult();
	}

	// Logout from controller
	public void logout() {
		if (bluetoothState == BluetoothState.NONE) {
			Log.d("LOGIN", "Bluetooth is not available!");
		} else if (bluetoothState == BluetoothState.LOGGED_IN) {
			// bluetoothConnectionService.logout();
			bluetoothConnectionService.stop();
		}
	}

	// Logout and reset the saved device
	public void logoutAndResetDevice() {
		logout();
		bluetoothDevice = null;
		activityHandler.saveDeviceAddress(null);
	}

	// // Stop the bluetooth connection services
	// public void stopService() { // TODO wird zu logout??
	// if (bluetoothConnectionService != null) {
	// bluetoothConnectionService.stop();
	// }
	// }

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

	// Initialize the paired device list
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

	// Clears the list for the discovered devices
	public void clearDiscoveredDeviceList() {
		discoveredDevicesArrayAdapter.clear();
	}

	// Updates the list for the discovered devices
	public void updateDiscoveredDeviceListState() {
		if (discoveredDevicesArrayAdapter.getCount() == 0) {
			clearDiscoveredDeviceList();
			discoveredDevicesArrayAdapter.add(activityHandler
					.getStr(R.string.discovery_noDevices));
		}
	}

	// Saves the values and run the command
	public void saveCommandValue(BluetoothCommands command, String newValue) {
		command.setValue(newValue);
		// TODO evtl. zwischenspeichern und erst bei erfolg festspeichern
		bluetoothConnectionService.sendCommand(command);
	}

	// Run the given command
	public void sendCommand(BluetoothCommands command) {
		if (command == BluetoothCommands.LOGIN) {
			login();
		} else if (command == BluetoothCommands.AT_LOGOUT) {
			logout();
		} else {
			bluetoothConnectionService.sendCommand(command);
		}
	}

	// TODO device in sharedPref speichern
	// TODO autologin check/unchecked ...
	// TODO defaultpassword
}
