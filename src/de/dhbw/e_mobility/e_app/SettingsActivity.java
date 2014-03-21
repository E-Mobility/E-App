package de.dhbw.e_mobility.e_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDeviceProvider;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDialogDisconnect;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDialogDiscovery;

/**
 * This is the main Activity that displays the current connection session.
 */
public class SettingsActivity extends PreferenceActivity {

	// Message types sent from the BluetoothConnectionService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_LONG_TOAST = 5;
	public static final int MESSAGE_SHORT_TOAST = 6;

	// Intent Request Codes
	private static final int BLUETOOTH_REQUEST_ENABLE = 1;
	private static final int BLUETOOTH_REQUEST_DISCONNECT = 2;
	private static final int BLUETOOTH_REQUEST_DISCOVERY = 3;

	// Preferences Elements
	private static final String SETTINGS_BLUETOOTH = "settings_bluetooth";
	// TODO R.id.bluetooth
	// Message parameter
	public static final String MESSAGE_TEXT = "settings_bluetooth";

	//
	private ActivityHandler activityHandler = ActivityHandler.getInstance();
	private BluetoothDeviceProvider deviceProvider = BluetoothDeviceProvider
			.getInstance();

	// private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.activity_settings);

		// Setup own handler
		// mHandler = setupHandler();
		// deviceProvider = new BluetoothDeviceProvider(setupHandler(),
		// getApplicationContext());

		// Get preference for bluetooth and initialize the click events
		Preference pref_bluetooth = (Preference) findPreference(SETTINGS_BLUETOOTH);
		pref_bluetooth
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						if (deviceProvider.isBluetoothDisabled()) {
							startActivityForResult(
									new Intent(deviceProvider
											.getBluetoothEnableQuestion()),
									BLUETOOTH_REQUEST_ENABLE);
							return true;
						}
						if (deviceProvider.getConnectedDeviceName() != null) {
							startActivityForResult(new Intent(
									getApplicationContext(),
									BluetoothDialogDisconnect.class),
									BLUETOOTH_REQUEST_DISCONNECT);
							return true;
						}
						// BluetoothDiscoveryDialog
						// .setDeviceProvider(deviceProvider);
						startActivityForResult(new Intent(
								getApplicationContext(),
								BluetoothDialogDiscovery.class),
								BLUETOOTH_REQUEST_DISCOVERY);
						return true;
					}
				});
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
	protected void onResume() {
		super.onResume();
		updateBluetoothInfo();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_settings, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_advanced_settings) {
			// startActivity(new Intent(this, SettingsActivity.class));
			// TODO Advanced Settings einschalten
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private Handler setupHandler() {
		// TODO Anpassen -- evtl. nur Toasts ausgeben
		// The Handler that gets information back from the BluetoothConnectionService
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == MESSAGE_LONG_TOAST) {
					Toast.makeText(getApplicationContext(),
							msg.getData().getString(MESSAGE_TEXT),
							Toast.LENGTH_LONG).show();
				} else if (msg.what == MESSAGE_SHORT_TOAST) {
					Toast.makeText(getApplicationContext(),
							msg.getData().getString(MESSAGE_TEXT),
							Toast.LENGTH_SHORT).show();

				} else if (msg.what == ActivityHandler.UPDATE_BLUETOOTHINFO) {
					updateBluetoothInfo();
				}
			}
		};
	}

	// Updates the summary of the bluetooth preference
	private void updateBluetoothInfo() {
		Preference bluetooth_pref = (Preference) findPreference(SETTINGS_BLUETOOTH);
		if (deviceProvider.isBluetoothDisabled()) {
			bluetooth_pref.setSummary(R.string.settings_bluetoothDisabled);
			return;
		}
		String tmpStr = deviceProvider.getConnectedDeviceName();
		if (tmpStr != null) {
			bluetooth_pref.setSummary(R.string.settings_bluetoothConnected
					+ tmpStr);
			return;
		}
		bluetooth_pref.setSummary(R.string.settings_bluetoothEnabled);
	}

	@Override
	public void onActivityResult(int resCode, int reqCode, Intent data) {

		if (resCode == BLUETOOTH_REQUEST_ENABLE) {
			// If Enable-Bluetooth is true
			if (reqCode != Activity.RESULT_OK) {
				Toast.makeText(this, R.string.settings_bluetoothDisabled,
						Toast.LENGTH_SHORT).show();
			}
		} else if (resCode == BLUETOOTH_REQUEST_DISCONNECT) {
			// If Bluetooth-Deivce should disconnect
			if (reqCode == Activity.RESULT_OK) {
				deviceProvider.stopService();
			}
		} else if (resCode == BLUETOOTH_REQUEST_DISCOVERY) {
			// If Bluetooth-Deivce was selected
			if (reqCode == Activity.RESULT_OK) {
				deviceProvider.connectDevice(data);
			}
		}
		// updateBluetoothInfo(); TODO reicht mit onResume() ((prüfen ob
		// korrekt, ob onResume zu früh aufgerufen wird??
	}

}
