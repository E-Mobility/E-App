package de.dhbw.e_mobility.e_app;

import de.dhbw.e_mobility.e_app.bluetooth.BluetoothInfoState;

public class SettingsProvider {

	private static SettingsProvider settingsProvider = null;

	public static SettingsProvider getInstance() {
		if (settingsProvider == null) {
			settingsProvider = new SettingsProvider();
		}
		return settingsProvider;
	}

	private SettingsProvider() {
		bluetoothState = null;
		deviceName = "";
	}

	private BluetoothInfoState bluetoothState;
	private String deviceName;

	/**
	 * @param bluetoothState
	 *            the bluetoothState to set
	 */
	public void setBluetoothState(BluetoothInfoState bluetoothState) {
		setBluetoothState(bluetoothState, "");
	}

	/**
	 * @param bluetoothState
	 *            the bluetoothState to set
	 * @param deviceName
	 *            the deviceName to set
	 */
	public void setBluetoothState(BluetoothInfoState bluetoothState,
			String deviceName) {
		this.bluetoothState = bluetoothState;
		this.deviceName = deviceName;
	}

	/**
	 * @return true if currently logged in
	 */
	public boolean isLoggedIn() {
		return (bluetoothState == BluetoothInfoState.LOGGED_IN);
	}

	/**
	 * @return the bluetooth info text
	 */
	public String getBluetoothState() {
		if (bluetoothState == null) {
			return "";
		}
		return bluetoothState.toString() + deviceName;
	}
}
