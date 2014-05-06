package de.dhbw.e_mobility.e_app.common;

public enum IntentKeys {
    DEVICE_ADDRESS("device_address"), DEVICE_NAME("device_name"), MESSAGE_TEXT("message_text"), MESSAGE_NUMBER("message_number"),
    BLUETOOTH_INFO_STATE(1), ENABLE_BLUETOOTH_PREF(2),DISABLE_BLUETOOTH_PREF(2),
    ASK_FOR_BLUETOOTH(10), START_DISCOVERING_DEVICES(11), UPDATE_BT_INFO(12), PAIRING_UNSUCCESSFUL(13),
    HANDLLER_DEVICE_PROVIDER(20), HANDLLER_SETTINGS(21), HANDLLER_DISCOVERY(22);

    String strValue;
    int intValue;

    // Constructor
    IntentKeys(String theValue) {
        this(theValue, 0);
    }

    // Constructor
    IntentKeys(int theValue) {
        this(null, theValue);
    }

    // Constructor
    IntentKeys(String theStrValue, int theIntValue) {
        strValue = theStrValue;
        intValue = theIntValue;
    }

    @Override
    // Returns the string value
    public String toString() {
        return strValue;
    }

    // Returns the integer value
    public int getValue() {
        return intValue;
    }
}
