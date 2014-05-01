package de.dhbw.e_mobility.e_app;

import android.util.Log;

/**
 * Created by Benny on 30.04.2014.
 */
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

    SpeedoValues(String theCommand) {
        value = 0;
        command = theCommand;
    }

    public void setValue(float theValue) {
        value = theValue;

        Log.d("SPEEDO-VALUES", command + ": " + theValue);
    }

    public String getCommand() {
        return command;
    }

    public float getValue() {
        return value;
    }


    @Override
    public String toString() {
        return "";
    }
}
