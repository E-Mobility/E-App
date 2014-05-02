package de.dhbw.e_mobility.e_app.speedo;

public enum SpeedoValues {
    U("U"), // (V) Spannung / voltage
    I("I"), // (A) Stromstärke / amperage
    C("C"), // (Ah) Stromverbrauch / power consumption
    V("V"), // (km/h) Geschwindigkeit / speed
    T("T"), // (%) Gasgriffstellung / accelerator throttle position
    M("M"), // (A) Aktueller max. Strom / current max amperage
    D("D"), // (multiple of 50x speedometer impulse) Distanz / distnace
    P("P"); // Aktuelles Profil / current profile

    private float value;
    private String command;

    // Constructor
    SpeedoValues(String theCommand) {
        value = 0;
        command = theCommand;
    }

    // Returns the appendent command
    public String getCommand() {
        return command;
    }

    // Returns the appendent value
    public float getValue() {
        return value;
    }

    // Saves the value
    public void setValue(float theValue) {
        value = theValue;
    }

    @Override
    @Deprecated
    public String toString() {
        return null;
    }
}
