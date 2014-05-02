package de.dhbw.e_mobility.e_app.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import de.dhbw.e_mobility.e_app.ActivityHandler;
import de.dhbw.e_mobility.e_app.IntentKeys;

public class MyBroadcastReceiver extends BroadcastReceiver {

    // BluetoothDevice-ACTIONS
    public static final int BT_ACTION_ACL_CONNECTED = 10;
    public static final int BT_ACTION_ACL_CONNECTED_BONDED = 11;
    public static final int BT_ACTION_ACL_DISCONNECT_REQUESTED = 12;
    public static final int BT_ACTION_ACL_DISCONNECTED = 13;
    public static final int BT_ACTION_BOND_STATE_CHANGED_BOND_BONDED = 14;
    public static final int BT_ACTION_BOND_STATE_CHANGED_BOND_BONDING = 15;
    public static final int BT_ACTION_BOND_STATE_CHANGED_BOND_NONE = 16;
    public static final int BT_ACTION_CLASS_CHANGED = 17;
    public static final int BT_ACTION_FOUND = 18;
    public static final int BT_ACTION_NAME_CHANGED = 19;
    public static final int BT_ACTION_PAIRING_REQUEST = 20;
    public static final int BT_ACTION_UUID = 21;

    // BluetoothAdapter-ACTIONS
    public static final int BT_ACTION_CONNECTION_STATE_CHANGED_CONNECTED = 30;
    public static final int BT_ACTION_CONNECTION_STATE_CHANGED_CONNECTING = 31;
    public static final int BT_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTED = 32;
    public static final int BT_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTING = 33;
    public static final int BT_ACTION_DISCOVERY_FINISHED = 34;
    public static final int BT_ACTION_DISCOVERY_STARTED = 35;
    public static final int BT_ACTION_LOCAL_NAME_CHANGED = 36;
    public static final int BT_ACTION_REQUEST_DISCOVERABLE = 37;
    public static final int BT_ACTION_REQUEST_ENABLE = 38;
    public static final int BT_ACTION_SCAN_MODE_CHANGED = 39;
    public static final int BT_ACTION_STATE_CHANGED = 40;

    // CONNECTION_STATE for BluetoothAdapter (Not implemented before API 11)
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_DISCONNECTING = 3;

    // Get ActivityHandler object
    private ActivityHandler activityHandler = ActivityHandler.getInstance();

    // Constructor
    public MyBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // BluetoothDevice-ACTIONS
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            Log.d("BluetoothDevice-ACTION", "ACTION_ACL_CONNECTED");
            BluetoothDevice deviceExtra = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // Already bonded device
            if (deviceExtra.getBondState() == BluetoothDevice.BOND_BONDED) {
                // TODO was ist hier mit NULLpointer??
                activityHandler.fireToHandler(
                        IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                        BT_ACTION_ACL_CONNECTED_BONDED);
            } else {
                activityHandler.fireToHandler(
                        IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                        BT_ACTION_ACL_CONNECTED);
            }
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED
                .equals(action)) {
            Log.d("BluetoothDevice-ACTION", "ACTION_ACL_DISCONNECT_REQUESTED");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_ACL_DISCONNECT_REQUESTED);
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            Log.d("BluetoothDevice-ACTION", "ACTION_ACL_DISCONNECTED");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_ACL_DISCONNECTED);
        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_BOND_STATE_CHANGED_BOND_NONE);
        } else if (BluetoothDevice.ACTION_CLASS_CHANGED.equals(action)) {
            Log.d("BluetoothDevice-ACTION", "ACTION_CLASS_CHANGED");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_CLASS_CHANGED);
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            Log.d("BluetoothDevice-ACTION", "ACTION_FOUND");

            // Get bluetooth object from intent
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            // If device is already paired it is already in the paired list
            // TODO was ist hier mit NULLpointer??
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                Bundle bundle = new Bundle();
                bundle.putString(IntentKeys.DEVICE_NAME.toString(),
                        device.getName());
                bundle.putString(IntentKeys.DEVICE_ADDRESS.toString(),
                        device.getAddress());
                activityHandler.fireToHandler(
                        IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                        BT_ACTION_FOUND, bundle);
            }
        } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
            Log.d("BluetoothDevice-ACTION", "ACTION_NAME_CHANGED");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_NAME_CHANGED);
            // TODO was ist hier mit NULLpointer??
        } else if (action
                .equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
            Log.d("BluetoothDevice-ACTION", "ACTION_PAIRING_REQUEST");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_PAIRING_REQUEST);
        } else if (action.equals("android.bleutooth.device.action.UUID")) {
            Log.d("BluetoothDevice-ACTION",
                    "android.bleutooth.device.action.UUID");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(), BT_ACTION_UUID);
        }

        // BluetoothAdapter-ACTIONS
        else if (action
                .equals("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")) {
            int extraState = intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", 0);

            if (extraState == STATE_CONNECTED) {
                Log.d("BluetoothAdapter-ACTION",
                        "ACTION_CONNECTION_STATE_CHANGED-STATE_CONNECTED");
                activityHandler.fireToHandler(
                        IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                        BT_ACTION_CONNECTION_STATE_CHANGED_CONNECTED);
            } else if (extraState == STATE_CONNECTING) {
                Log.d("BluetoothAdapter-ACTION",
                        "ACTION_CONNECTION_STATE_CHANGED-STATE_CONNECTING");
                activityHandler.fireToHandler(
                        IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                        BT_ACTION_CONNECTION_STATE_CHANGED_CONNECTING);
            } else if (extraState == STATE_DISCONNECTED) {
                Log.d("BluetoothAdapter-ACTION",
                        "ACTION_CONNECTION_STATE_CHANGED-STATE_DISCONNECTED");
                activityHandler.fireToHandler(
                        IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                        BT_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTED);
            } else if (extraState == STATE_DISCONNECTING) {
                Log.d("BluetoothAdapter-ACTION",
                        "ACTION_CONNECTION_STATE_CHANGED-STATE_DISCONNECTING");
                // TODO alle signale pruefen
                activityHandler.fireToHandler(
                        IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                        BT_ACTION_CONNECTION_STATE_CHANGED_DISCONNECTING);
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_DISCOVERY_FINISHED");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_DISCOVERY_FINISHED);
        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_DISCOVERY_STARTED");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_DISCOVERY_STARTED);
        } else if (BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_LOCAL_NAME_CHANGED");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_LOCAL_NAME_CHANGED);
        } else if (BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_REQUEST_DISCOVERABLE");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_REQUEST_DISCOVERABLE);
        } else if (BluetoothAdapter.ACTION_REQUEST_ENABLE.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_REQUEST_ENABLE");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_REQUEST_ENABLE);
        } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_SCAN_MODE_CHANGED");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_SCAN_MODE_CHANGED);
        } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_STATE_CHANGED");
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_STATE_CHANGED);
        }
    }
}
