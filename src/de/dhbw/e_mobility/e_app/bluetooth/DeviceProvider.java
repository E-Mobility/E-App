package de.dhbw.e_mobility.e_app.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.lang.reflect.Method;
import java.util.Set;

import de.dhbw.e_mobility.e_app.R;
import de.dhbw.e_mobility.e_app.common.ActivityHandler;
import de.dhbw.e_mobility.e_app.common.IntentKeys;
import de.dhbw.e_mobility.e_app.dialog.BluetoothDialogDiscovery;
import de.dhbw.e_mobility.e_app.settings.SettingsProvider;

public class DeviceProvider {

    // Singelton
    private static DeviceProvider instance;

    // Current BluetoothInfoState
    private BluetoothInfoState bluetoothInfoState;

    // Get ActivityHandler object
    private ActivityHandler activityHandler = ActivityHandler.getInstance();

    // Get SettingsProvider object
    private SettingsProvider settingsProvider = SettingsProvider.getInstance();

    // Private objects
    private BluetoothAdapter bluetoothAdapter = null;
    private MyBroadcastReceiver myBroadcastReceiver = null;
    private ConnectionService connectionService = null;
    private BluetoothDevice bluetoothDevice = null;

    // Lists for displaying the paired and discovered devices
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ArrayAdapter<String> discoveredDevicesArrayAdapter;

    // Constructor
    private DeviceProvider() {
        // Give own Handler to ActivityHandler
        activityHandler.setHandler(IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(), setupHandler());

        // Get local bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // No bluetooth adapter is available
            bluetoothInfoState = BluetoothInfoState.NONE;
            enableBluetoothPreference(false);
            activityHandler.fireToast(R.string.settings_bluetoothNotAvailable);
        }
    }

    // Singleton
    public static synchronized DeviceProvider getInstance() {
        if (instance == null) {
            instance = new DeviceProvider();
        }
        return instance;
    }

    // Initializes all
    public void init() {
        setDevice(activityHandler.getDeviceAddress());
        activityHandler.saveDeviceAddress(null);

        updateBluetoothInfo(BluetoothInfoState.INITIALIZED);

        myBroadcastReceiver = new MyBroadcastReceiver();
        connectionService = new ConnectionService();

        // Register actions for BluetoothDevice
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_CLASS_CHANGED));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED));

        // Register actions for BluetoothAdapter
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED"));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // Register hidden bluetooth actions
        activityHandler.getMainContext().registerReceiver(myBroadcastReceiver, new IntentFilter("android.bleutooth.device.action.UUID"));
    }

    // Sets the bluetooth device
    public void setDevice(String address) {
        if (address != null) {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        }
    }

    // Resets the current bluetooth device
    public void resetDevice() {
        bluetoothDevice = null;
    }

    // Unregister the BroadcastReceiver
    public void unregisterReceiver() {
        activityHandler.getMainContext().unregisterReceiver(myBroadcastReceiver);
    }

    // Update the bluetooth info
    private void updateBluetoothInfo(BluetoothInfoState theState) {
        bluetoothInfoState = theState;
        if (theState == BluetoothInfoState.LOGGED_IN) {
            settingsProvider.setBluetoothState(theState, bluetoothDevice.getName());
        } else {
            settingsProvider.setBluetoothState(theState);
        }
        activityHandler.fireToHandler(IntentKeys.HANDLLER_SETTINGS.getValue(), IntentKeys.UPDATE_BT_INFO.getValue());
    }

    // Setup the handler for this class
    private Handler setupHandler() {
        return new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == IntentKeys.BLUETOOTH_INFO_STATE.getValue()) {
                    // TODO was ist hier mit NULLpointer??
                    BluetoothInfoState infoState = BluetoothInfoState.values()[msg.getData().getInt(IntentKeys.MESSAGE_NUMBER.toString())];

                    updateBluetoothInfo(infoState);
                    if (infoState == BluetoothInfoState.LOGGED_IN) {
                        // Login was successful
                        Log.d("LOGIN", "Login was successful (" + bluetoothDevice.getName() + ")");
                        doOnResult();
                    } else if (infoState == BluetoothInfoState.CONNECTION_FAILED) {
                        // Connection to controller failed
                        enableBluetoothPreference(true);
                    } else if (infoState == BluetoothInfoState.LOGIN_TIMEOUT) {
                        // Timeout during login
                        activityHandler.fireToast(infoState.toString());
                    }
                }

                // BluetoothDevice-ACTIONS
                else if (msg.what == MyBroadcastReceiver.BT_ACTION_ACL_CONNECTED) {
                    if(bluetoothInfoState == BluetoothInfoState.PAIRED) {
                        // Connection with controller
                        //bluetoothInfoState = BluetoothInfoState.ACL_CONNECTED;
                        updateBluetoothInfo(BluetoothInfoState.ACL_CONNECTED);
                        doOnResult();
                    }
//                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_ACL_CONNECTED_BONDED) {
//                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_ACL_DISCONNECT_REQUESTED) {
                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_ACL_DISCONNECTED) {
                    // Disconnected with controller
                    activityHandler.fireToast(BluetoothInfoState.ACL_DISCONNECTED.toString());
                    updateBluetoothInfo(BluetoothInfoState.INITIALIZED);
                    enableBluetoothPreference(true);
                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_BOND_STATE_CHANGED_BOND_BONDED) {
                    // Bonding was successful
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        activityHandler.fireToast(R.string.bluetooth_bound_bounded, IntentKeys.DEVICE_NAME.toString() + "(" + bundle.getString(IntentKeys.DEVICE_ADDRESS.toString()) + ")");
                        doOnResult();
                    }
//                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_BOND_STATE_CHANGED_BOND_BONDING) {
                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_BOND_STATE_CHANGED_BOND_NONE) {
                    // Controller not bonded
                    // TODO!!!!!!
//                    updateBluetoothInfo(BluetoothInfoState.CONNECTION_FAILED);
                    enableBluetoothPreference(true);
//                    doOnResult();

                    // Evtl. Ausgabe: Gerät ist noch nicht gekoppelt
                    // Möglicher Punkt um Pairinganfrage (bei Vollbild) hervorzuheben
//                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_CLASS_CHANGED) {
                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_FOUND) {
                    // Add device to discoveredDevicesArrayAdapter
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        discoveredDevicesArrayAdapter.add(bundle.getString(IntentKeys.DEVICE_NAME.toString()) + "\n" + bundle.getString(IntentKeys.DEVICE_ADDRESS.toString()));
                    }
//                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_NAME_CHANGED) {
//                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_PAIRING_REQUEST) {
//                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_UUID) {
                }

                // BluetoothAdapter-ACTIONS
//                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_CONNECTION_STATE_CHANGED) {
                else if (msg.what == MyBroadcastReceiver.BT_ACTION_DISCOVERY_FINISHED) {
                    // Finished discovering
                    if (discoveredDevicesArrayAdapter != null && discoveredDevicesArrayAdapter.getCount() == 0) {
                        activityHandler.fireToast(R.string.discovery_noDevices);
                    }
                    activityHandler.fireToHandler(
                            IntentKeys.HANDLLER_DISCOVERY.getValue(),
                            BluetoothDialogDiscovery.BT_DISCOVERY_FINISHED);
                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_DISCOVERY_STARTED) {
                    // Started discovering
                    // TODO
                    activityHandler.fireToast(R.string.discovery_scanning);
//                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_LOCAL_NAME_CHANGED) {
//                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_REQUEST_DISCOVERABLE) {
//                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_REQUEST_ENABLE) {
//                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_SCAN_MODE_CHANGED) {
                } else if (msg.what == MyBroadcastReceiver.BT_ACTION_STATE_CHANGED) {
                    // Bluetooth was enabled
                    // TODO
                    activityHandler.fireToast(R.string.bluetooth_info_enabled);
                }
            }
        };
    }

    // Checks if bluetooth is turned on
    private void checkBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothInfoState = BluetoothInfoState.ON;
            doOnResult();
        } else {
            Log.d("LOGIN", "Bluetooth is not turened on");
            activityHandler.fireToHandler(IntentKeys.HANDLLER_SETTINGS.getValue(),
                    IntentKeys.ASK_FOR_BLUETOOTH.getValue(),
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
        }
    }

    // Checks if device is already known
    private void checkDeviceKnown() {
        if (bluetoothDevice != null) {
            bluetoothInfoState = BluetoothInfoState.DEVICE_KNOWN;
            doOnResult();
        } else {
            // Discovery for devices
            Log.d("LOGIN", "No device available / Device is offline");
            activityHandler.fireToHandler(IntentKeys.HANDLLER_SETTINGS.getValue(),
                    IntentKeys.START_DISCOVERING_DEVICES.getValue());
        }
    }

    // Checks if device is paired
    private void checkIsPaired() {
        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
//                || createBond()) {
            bluetoothInfoState = BluetoothInfoState.PAIRED;
            doOnResult();
        } else {
            createBond();
//            Log.d("LOGIN", "Device is not paired / Pairing unsuccessful");
//            activityHandler.fireToast(R.string.device_not_paired);
//            updateBluetoothInfo(BluetoothInfoState.UNPAIRED);
//            enableBluetoothPreference(true);
        }
    }

    // Checks connection and login state to controller
    private void checkConnectionAndLogin() {
        // Update password
        Command.LOGIN.setValue(activityHandler.getPassword());
        connectionService.checkConnectionAndLogin(bluetoothDevice);
    }

    // Enables the preference element for bluetooth settings
    private void enableBluetoothPreference(boolean value) {
        if (value) {
            activityHandler.fireToHandler(IntentKeys.HANDLLER_SETTINGS.getValue(),
                    IntentKeys.ENABLE_BLUETOOTH_PREF.getValue());
            return;
        }
        // Reset the current saved device
        resetDevice();
        activityHandler.fireToHandler(IntentKeys.HANDLLER_SETTINGS.getValue(),
                IntentKeys.DISABLE_BLUETOOTH_PREF.getValue());
    }

    // Does the next step on login
    public void doOnResult() {
        if (bluetoothInfoState == BluetoothInfoState.INITIALIZED) {
            checkBluetooth();
            return;
        }
        if (bluetoothInfoState == BluetoothInfoState.ON) {
            Log.d("UPDATE", "onResult ON");
            updateBluetoothInfo(BluetoothInfoState.ON);
            checkDeviceKnown();
            return;
        }
        if (bluetoothInfoState == BluetoothInfoState.DEVICE_KNOWN) {
            checkIsPaired();
            return;
        }
        if (bluetoothInfoState == BluetoothInfoState.PAIRED) {
            checkConnectionAndLogin();
            return;
        }
        if (bluetoothInfoState == BluetoothInfoState.ACL_CONNECTED) {
//            updateBluetoothInfo(BluetoothInfoState.ACL_CONNECTED);
            return;
        }
        if (bluetoothInfoState == BluetoothInfoState.LOGGED_IN) {
            enableBluetoothPreference(true);
            // Save device
            activityHandler.saveDeviceAddress(bluetoothDevice.getAddress());
            // Asking for parameter list
            sendCommand(Command.AT_PARAM_LIST);
            // Set default value for at-push
//            Command.AT_PUSH_N.setValue("1");
//            sendCommand(Command.AT_PUSH_N);
            // TODO TODO TODO! enable push but also try to get param list
            return;
        }
        if (bluetoothInfoState == BluetoothInfoState.NONE) {
            // There is no bluetooth available on this device
            Log.d("LOGIN", "Bluetooth not available");
            updateBluetoothInfo(BluetoothInfoState.NONE);
            return;
        }
    }

    // Start login to controller
    public void login() {
        if (bluetoothInfoState == BluetoothInfoState.NONE) {
            Log.d("LOGIN", "Bluetooth is not available!");
            return;
        }
        bluetoothInfoState = BluetoothInfoState.INITIALIZED;
        doOnResult();
    }

    // Logout from controller
    public void logout() {
        if (bluetoothInfoState == BluetoothInfoState.NONE) {
            Log.d("LOGIN", "Bluetooth is not available!");
        } else if (bluetoothInfoState == BluetoothInfoState.LOGGED_IN) {
            connectionService.logout();
        }
    }

    // Logout and reset the saved device
    public void logoutAndResetDevice() {
        logout();
        bluetoothDevice = null;
        activityHandler.saveDeviceAddress(null);
    }

    // Creates a bond between this devices
    private boolean createBond() {
        // If not already bonded
        Class bluetoothDeviceClass;
        try {
            bluetoothDeviceClass = Class.forName("android.bluetooth.BluetoothDevice");
            Method createBondMethod = bluetoothDeviceClass
                    .getMethod("createBond");
            return (Boolean) createBondMethod
                    .invoke(bluetoothDevice);
        } catch (Exception e) {
            Log.e("DEVICE-PROVIDER", "Fail in creating bond [" + e.getClass().getSimpleName().toString() + "]:" + e);
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
    public void saveCommandValue(Command command, String newValue) {
        command.setValue(newValue);
        connectionService.sendCommand(command);
    }

    // Run the given command
    public void sendCommand(Command command) {
        if (command == Command.LOGIN) {
            login();
        } else if (command == Command.AT_LOGOUT) {
            logout();
        } else {
            connectionService.sendCommand(command);
        }
    }
}
