package de.dhbw.e_mobility.e_app.bluetooth;

public enum BluetoothInfoState {

	NONE("Kein Bluetooth verfügbar!"), DISCONNECTED("Nicht verbunden"), OFF(
			"Bluetooth ist ausgeschaltet"), ON("Bluetooth ist eingeschaltet"), CONNECTED(
			"Verbunden mit "), LOGGED_IN("Angemeldet an ");

	// TODO "Verbunden mit" ist evtl. verwirrend.. man kann es nicht verwenden,
	// weil login noch nicht erfolgreich.. (rausnehmen??)

	private String text;

	private BluetoothInfoState(String theText) {
		text = theText;
	}

	@Override
	public String toString() {
		return text;
	}
}
