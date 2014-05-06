package de.dhbw.e_mobility.e_app.bluetooth;

public enum BluetoothInfoState {

    NONE("Kein Bluetooth verfügbar!"),
    INITIALIZED("Tippen um zu verbinden"),
    ON("Bluetooth ist eingeschaltet"),
    DEVICE_KNOWN("Gerät bereits gewählt"),
    UNPAIRED("Gerät nicht gekoppelt"),
    PAIRED("Gerät gekoppelt"),
    CONNECTION_FAILED("Verbindung fehlgeschlagen"),
    ACL_CONNECTED("Verbunden"),
    LOGGED_IN("Verbunden mit "),
    ACL_DISCONNECTED("Verbindung beendet");

    private String text;

    // Constructor
    private BluetoothInfoState(String theText) {
        text = theText;
    }

    @Override
    // Returns the text
    public String toString() {
        return text;
    }
}
