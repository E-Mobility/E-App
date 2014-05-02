package de.dhbw.e_mobility.e_app.bluetooth;

public enum BluetoothInfoState {

    NONE("Kein Bluetooth verfügbar!"), DISCONNECTED("Nicht verbunden"), OFF(
            "Bluetooth ist ausgeschaltet"), ON("Bluetooth ist eingeschaltet"), CONNECTED(
            "Login fehlgeschlagen bei "), LOGGED_IN("Angemeldet an ");

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
