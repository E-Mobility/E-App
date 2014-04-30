package de.dhbw.e_mobility.e_app;

import android.app.Activity;
import android.os.Bundle;

public class TachoHochActivity extends Activity {

    // Get ActivityHandler object
    private ActivityHandler activityHandler = ActivityHandler.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tacho_hoch);

        System.out.println("Geschwindigkeitsbsp (20km/h) = ");
        System.out.println(20 * activityHandler.getSpeedFactor());
    }

    private enum TACHO_ELEMENTS {
        GESCHWINDIGKEIT("hoch_geschwindigkeit"), GESCHWINDIGKEIT_ZAHL("hoch_geschwindigkeit_zahl"),
        GESCHWINDIGKEIT_EINHEIT("hoch_geschwindigkeit_einheit"), MODUS("hoch_modus"),
        TOUR("hoch_tour"), TOUR_RESET("hoch_tour_reset"), TOUR_DISTANZ("hoch_tour_distanz"),
        TOUR_DISTANZ_ZAHL("hoch_tour_distanz_zahl"), TOUR_DISTANZ_EINHEIT("hoch_tour_distanz_einheit"),
        TOUR_DAUER("hoch_tour_dauer"), TOUR_DAUER_ZAHL("hoch_tour_dauer_zahl"), TOUR_DAUER_EINHEIT("hoch_tour_dauer_einheit"),
        AKKUSTAND("hoch_akkustand"), AKKUSTAND_ZAHL("hoch_akkustand_zahl"), AKKUSTAND_EINHEIT("hoch_akkustand_einheit"), AKKUSTAND_ANZEIGE("hoch_akkustand_anzeige"),
        UNTERSTUETZUNG("hoch_unterstuetzung"), UNTERSTUETZUNG_ZAHL("hoch_unterstuetzung_zahl"), UNTERSTUETZUNG_EINHEIT("hoch_unterstuetzung_einheit"), UNTERSTUETZUNG_ANZEIGE("hoch_unterstuetzung_anzeige");

        String value;

        TACHO_ELEMENTS(String theValue) {
            value = theValue;
        }
    }
}
