package de.dhbw.e_mobility.e_app.speedo;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import de.dhbw.e_mobility.e_app.common.ActivityHandler;
import de.dhbw.e_mobility.e_app.R;

public class SpeedoPortraitActivity extends Activity {

    // Update interval in ms
    private final int updateInterval;

    {
        updateInterval = 1000;
    }

    String[] speedUnits;

    // Get ActivityHandler object
    private ActivityHandler activityHandler = ActivityHandler.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speedo_portrait);

        Resources res = getResources();
        speedUnits = res.getStringArray(R.array.settings_speed_entries);

        UpdateSpeedo updateSpeedo = new UpdateSpeedo();
        updateSpeedo.start();

        // http://developer.android.com/reference/android/app/Activity.html#runOnUiThread(java.lang.Runnable)
    }

    // Updates the display elements
    private void updateElements() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Change speed unit if it was changed
                TextView speed_unit_view = (TextView) getElement(SpeedoElements.GESCHWINDIGKEIT_EINHEIT);
                if (speed_unit_view != null) {
                    if (speedUnits.length > 1) {
                        if (!activityHandler.isKmh()) {
                            // TODO was ist hier mit NULLpointer??
                            if (speed_unit_view.getText().equals(speedUnits[0])) {
                                speed_unit_view.setText(speedUnits[1]);
                            }
                        } else {
                            // TODO was ist hier mit NULLpointer??
                            if (speed_unit_view.getText().equals(speedUnits[1])) {
                                speed_unit_view.setText(speedUnits[0]);
                            }
                        }
                    }
                }

                // Update speed
                TextView speed_view = (TextView) getElement(SpeedoElements.GESCHWINDIGKEIT_ZAHL);
                if (speed_view != null) {
                    float speedFactor = activityHandler.getSpeedFactor();
                    String speed = String.valueOf(SpeedoValues.V.getValue() * speedFactor);

                    // TODO was ist hier mit NULLpointer??
                    if (!speed_view.getText().equals(speed)) {
                        speed_view.setText(speed);
                    }
                }
            }
        });

    }

    // Returns the view of the SpeedoElements
    private View getElement(SpeedoElements element) {
        return findViewById(element.getValue());
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
