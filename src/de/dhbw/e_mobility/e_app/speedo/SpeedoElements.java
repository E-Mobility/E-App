package de.dhbw.e_mobility.e_app.speedo;

import de.dhbw.e_mobility.e_app.R;

public enum SpeedoElements {
    GESCHWINDIGKEIT(R.id.hoch_geschwindigkeit), GESCHWINDIGKEIT_ZAHL(R.id.hoch_geschwindigkeit_zahl),
    GESCHWINDIGKEIT_EINHEIT(R.id.hoch_geschwindigkeit_einheit), MODUS(R.id.hoch_modus),
    TOUR(R.id.hoch_tour), TOUR_RESET(R.id.hoch_tour_reset), TOUR_DISTANZ(R.id.hoch_tour_distanz),
    TOUR_DISTANZ_ZAHL(R.id.hoch_tour_distanz_zahl), TOUR_DISTANZ_EINHEIT(R.id.hoch_tour_distanz_einheit),
    TOUR_DAUER(R.id.hoch_tour_dauer), TOUR_DAUER_ZAHL(R.id.hoch_tour_dauer_zahl), TOUR_DAUER_EINHEIT(R.id.hoch_tour_dauer_einheit),
    AKKUSTAND(R.id.hoch_akkustand), AKKUSTAND_ZAHL(R.id.hoch_akkustand_zahl), AKKUSTAND_EINHEIT(R.id.hoch_akkustand_einheit), AKKUSTAND_ANZEIGE(R.id.hoch_akkustand_anzeige),
    UNTERSTUETZUNG(R.id.hoch_unterstuetzung), UNTERSTUETZUNG_ZAHL(R.id.hoch_unterstuetzung_zahl), UNTERSTUETZUNG_EINHEIT(R.id.hoch_unterstuetzung_einheit), UNTERSTUETZUNG_ANZEIGE(R.id.hoch_unterstuetzung_anzeige);

    private int value;

    // Constructor
    SpeedoElements(int theValue) {
        value = theValue;
    }

    // Returns the value
    public int getValue() {
        return value;
    }

    @Override
    @Deprecated
    public String toString() {
        return null;
    }
}
