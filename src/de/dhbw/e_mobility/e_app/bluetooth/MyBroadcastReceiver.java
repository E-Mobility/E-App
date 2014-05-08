package de.dhbw.e_mobility.e_app.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import de.dhbw.e_mobility.e_app.common.ActivityHandler;
import de.dhbw.e_mobility.e_app.common.IntentKeys;

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
    public static final int BT_ACTION_CONNECTION_STATE_CHANGED = 30;
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
        BluetoothDevice extraDevice = intent
                .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        boolean bonded = false;
        if (extraDevice != null) {
            bonded = (extraDevice.getBondState() == BluetoothDevice.BOND_BONDED);
        }

        // BluetoothDevice-ACTIONS
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            Log.d("BluetoothDevice-ACTION", "ACTION_ACL_CONNECTED");
            // TODO-!
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_ACL_CONNECTED);
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED
                .equals(action)) {
            Log.d("BluetoothDevice-ACTION", "ACTION_ACL_DISCONNECT_REQUESTED");
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            Log.d("BluetoothDevice-ACTION", "ACTION_ACL_DISCONNECTED");
            // TODO-!
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_ACL_DISCONNECTED);
        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            // TODO-!
            if (bonded) {
                Log.d("BluetoothDevice-ACTION", "ACTION_BOND_STATE_CHANGED_BONDED");
                Bundle bundle = new Bundle();
                bundle.putString(IntentKeys.DEVICE_NAME.toString(),
                        extraDevice.getName());
                bundle.putString(IntentKeys.DEVICE_ADDRESS.toString(),
                        extraDevice.getAddress());
                activityHandler.fireToHandler(
                        IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                        BT_ACTION_BOND_STATE_CHANGED_BOND_BONDED, bundle);
            } else if(extraDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                Log.d("BluetoothDevice-ACTION", "ACTION_BOND_STATE_CHANGED_BONDING");
                activityHandler.fireToHandler(
                        IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                        BT_ACTION_BOND_STATE_CHANGED_BOND_BONDING);
                // TODO TESTEN!!
            } else {
                Log.d("BluetoothDevice-ACTION", "ACTION_BOND_STATE_CHANGED_NONE");
                activityHandler.fireToHandler(
                        IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                        BT_ACTION_BOND_STATE_CHANGED_BOND_NONE);
            }
        } else if (BluetoothDevice.ACTION_CLASS_CHANGED.equals(action)) {
            Log.d("BluetoothDevice-ACTION", "ACTION_CLASS_CHANGED");
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            Log.d("BluetoothDevice-ACTION", "ACTION_FOUND");
            // TODO-!
            // If device is already paired it is already in the paired list
            if (!bonded) {
                Bundle bundle = new Bundle();
                bundle.putString(IntentKeys.DEVICE_NAME.toString(),
                        extraDevice.getName());
                bundle.putString(IntentKeys.DEVICE_ADDRESS.toString(),
                        extraDevice.getAddress());
                activityHandler.fireToHandler(
                        IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                        BT_ACTION_FOUND, bundle);
            }
        } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
            Log.d("BluetoothDevice-ACTION", "ACTION_NAME_CHANGED");
        } else if (action
                .equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
            Log.d("BluetoothDevice-ACTION", "ACTION_PAIRING_REQUEST");
        } else if (action.equals("android.bleutooth.device.action.UUID")) {
            Log.d("BluetoothDevice-ACTION",
                    "android.bleutooth.device.action.UUID");
        }

        // BluetoothAdapter-ACTIONS
        else if (action
                .equals("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_CONNECTION_STATE_CHANGED");
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_DISCOVERY_FINISHED");
            // TODO-!
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_DISCOVERY_FINISHED);
        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_DISCOVERY_STARTED");
            // TODO-!
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_DISCOVERY_STARTED);
        } else if (BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_LOCAL_NAME_CHANGED");
        } else if (BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_REQUEST_DISCOVERABLE");
        } else if (BluetoothAdapter.ACTION_REQUEST_ENABLE.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_REQUEST_ENABLE");
        } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_SCAN_MODE_CHANGED");
        } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            Log.d("BluetoothAdapter-ACTION", "ACTION_STATE_CHANGED");
            // TODO-!
            activityHandler.fireToHandler(
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                    BT_ACTION_STATE_CHANGED);
        }
    }
}
