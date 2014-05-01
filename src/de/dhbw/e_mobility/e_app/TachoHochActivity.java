package de.dhbw.e_mobility.e_app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TachoHochActivity extends Activity {

    private final int updateInterval = 1000; // Update interval in ms
    // Get ActivityHandler object
    private ActivityHandler activityHandler = ActivityHandler.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tacho_hoch);

        UpdateSpeedo updateSpeedo = new UpdateSpeedo();

        updateSpeedo.start();

        // http://developer.android.com/reference/android/app/Activity.html#runOnUiThread(java.lang.Runnable)
    }

    private void updateElements() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
// Change speed unit if it was changed
                TextView speed_unit_view = (TextView) findViewById(TACHO_ELEMENTS.GESCHWINDIGKEIT_EINHEIT.getValue());
                if (speed_unit_view != null) {
                    String kmh = "km/h", mph = "mph";

                    if (!activityHandler.isKmh()) {
                        if (speed_unit_view.getText().equals(kmh)) {
                            speed_unit_view.setText(mph);
                        }
                    } else {
                        if (speed_unit_view.getText().equals(mph)) {
                            speed_unit_view.setText(kmh);
                        }
                    }
                }

                // Update speed
                TextView speed_view = (TextView) findViewById(TACHO_ELEMENTS.GESCHWINDIGKEIT_ZAHL.getValue());
                if (speed_view != null) {
                    float speedFactor = activityHandler.getSpeedFactor();
                    String speed = String.valueOf(SpeedoValues.V.getValue() * speedFactor);

                    if (!speed_view.getText().equals(speed)) {
                        speed_view.setText(speed);
                    }
                }
            }
        });

    }

    private enum TACHO_ELEMENTS {
        GESCHWINDIGKEIT(R.id.hoch_geschwindigkeit), GESCHWINDIGKEIT_ZAHL(R.id.hoch_geschwindigkeit_zahl),
        GESCHWINDIGKEIT_EINHEIT(R.id.hoch_geschwindigkeit_einheit), MODUS(R.id.hoch_modus),
        TOUR(R.id.hoch_tour), TOUR_RESET(R.id.hoch_tour_reset), TOUR_DISTANZ(R.id.hoch_tour_distanz),
        TOUR_DISTANZ_ZAHL(R.id.hoch_tour_distanz_zahl), TOUR_DISTANZ_EINHEIT(R.id.hoch_tour_distanz_einheit),
        TOUR_DAUER(R.id.hoch_tour_dauer), TOUR_DAUER_ZAHL(R.id.hoch_tour_dauer_zahl), TOUR_DAUER_EINHEIT(R.id.hoch_tour_dauer_einheit),
        AKKUSTAND(R.id.hoch_akkustand), AKKUSTAND_ZAHL(R.id.hoch_akkustand_zahl), AKKUSTAND_EINHEIT(R.id.hoch_akkustand_einheit), AKKUSTAND_ANZEIGE(R.id.hoch_akkustand_anzeige),
        UNTERSTUETZUNG(R.id.hoch_unterstuetzung), UNTERSTUETZUNG_ZAHL(R.id.hoch_unterstuetzung_zahl), UNTERSTUETZUNG_EINHEIT(R.id.hoch_unterstuetzung_einheit), UNTERSTUETZUNG_ANZEIGE(R.id.hoch_unterstuetzung_anzeige);

        private int value;

        TACHO_ELEMENTS(int theValue) {
            value = theValue;
        }

        private int getValue() {
            return value;
        }
    }

    // This thread updates the displayed elements
    private class UpdateSpeedo extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(updateInterval);
                    updateElements();
                } catch (InterruptedException e) {
                    Log.d("SPEEDO", "Sleep was interrupted");
                    break;
                }
            }
        }
    }
}
