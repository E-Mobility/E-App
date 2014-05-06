package de.dhbw.e_mobility.e_app.speedo;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.NumberFormat;

import de.dhbw.e_mobility.e_app.R;
import de.dhbw.e_mobility.e_app.common.ActivityHandler;

public abstract class SpeedoActivity extends Activity {

    // Update interval in ms
    private final int updateInterval;

    {
        updateInterval = 500;
    }

    // Get ActivityHandler object
    protected ActivityHandler activityHandler = ActivityHandler.getInstance();
    private NumberFormat speedNumberFormat;
    private NumberFormat distanceNumberFormat;
    private String[] distanceUnits;
    private String[] speedUnits;
    private UpdateSpeedo updateSpeedo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources res = getResources();
        distanceUnits = res.getStringArray(R.array.settings_distance_unit);
        speedUnits = res.getStringArray(R.array.settings_speed_unit);
        updateSpeedo = new UpdateSpeedo();
        // http://developer.android.com/reference/android/app/Activity.html#runOnUiThread(java.lang.Runnable)

        initValues();


        // Settings for format the output numbers
        int speedDecimalPlace = 1, distanceDecimalPlace = 2;
        speedNumberFormat = NumberFormat.getNumberInstance();
        speedNumberFormat.setMinimumFractionDigits(speedDecimalPlace);
        speedNumberFormat.setMaximumFractionDigits(speedDecimalPlace);
        distanceNumberFormat = NumberFormat.getNumberInstance();
        distanceNumberFormat.setMinimumFractionDigits(distanceDecimalPlace);
        distanceNumberFormat.setMaximumFractionDigits(distanceDecimalPlace);
    }

    // Testmetohd TODO del?
    private void initValues() {
//        SpeedoValues.V.setValue((float) 21.78354869688);
//        SpeedoValues.DISTANCE.setValue((float) 5);
    }

    // Updates the display elements
    private void updateElements() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Change speed unit if it was changed
                TextView speed_unit_view = (TextView) getElement(SpeedoElements.SPEED_UNIT);
                if (speed_unit_view != null) {
                    if (speedUnits.length > 1) {
                        if (!activityHandler.isKm()) {
                            // TODO was ist hier mit NULLpointer??
                            if (!speed_unit_view.getText().equals(speedUnits[1])) {
                                speed_unit_view.setText(speedUnits[1]);
                            }
                        } else {
                            // TODO was ist hier mit NULLpointer??
                            if (!speed_unit_view.getText().equals(speedUnits[0])) {
                                speed_unit_view.setText(speedUnits[0]);
                            }
                        }
                    }
                }

                // Change distance unit if it was changed
                TextView distance_unit_view = (TextView) getElement(SpeedoElements.DISTANCE_UNIT);
                if (distance_unit_view != null) {
                    if (distanceUnits.length > 1) {
                        if (!activityHandler.isKm()) {
                            // TODO was ist hier mit NULLpointer??
                            if (!distance_unit_view.getText().equals(distanceUnits[1])) {
                                distance_unit_view.setText(speedUnits[1]);
                            }
                        } else {
                            // TODO was ist hier mit NULLpointer??
                            if (!distance_unit_view.getText().equals(distanceUnits[0])) {
                                distance_unit_view.setText(distanceUnits[0]);
                            }
                        }
                    }
                }

                float unitFactor = activityHandler.getUnitFactor();
                float speedVal = SpeedoValues.V.getValue();
                int assistanceVal = (int) (speedVal * 20 / 5);
                if (assistanceVal > 100) {
                    assistanceVal = 100;
                }

                // Update speed
                TextView speed_view = (TextView) getElement(SpeedoElements.SPEED);
                if (speed_view != null) {
                    if (speedVal == 0) {
                        // Stop duration timer
                        activityHandler.stopDurationTimer();
                    } else {
                        // Start duration timer
                        activityHandler.startDurationTimer();
                    }
                    String speed = String.valueOf(speedNumberFormat.format(speedVal * unitFactor));

                    // TODO was ist hier mit NULLpointer??
                    if (!speed_view.getText().equals(speed)) {
                        speed_view.setText(speed);
                    }
                }

                // Update distance
                float factor = 1;
                if (updateInterval != 0) {
                    // Get factor to calculate distance
                    factor = 60 * 60 * 1000 / updateInterval;
                    // TODO! check this calculation
                }
                SpeedoValues.DISTANCE.setValue(SpeedoValues.DISTANCE.getValue() + (speedVal / factor));
                TextView distance_view = (TextView) getElement(SpeedoElements.DISTANCE);
                if (distance_view != null) {
                    String distance = String.valueOf(distanceNumberFormat.format(SpeedoValues.DISTANCE.getValue() * unitFactor));

                    // TODO was ist hier mit NULLpointer??
                    if (!distance_view.getText().equals(distance)) {
                        distance_view.setText(distance);
                    }
                }

                // Update duration
                TextView duration_view = (TextView) getElement(SpeedoElements.DURATION);
                if (duration_view != null) {
                    String duration = activityHandler.getDuration(); //SpeedoValues.DURATION.getCommand();

                    // TODO was ist hier mit NULLpointer??
                    if (!duration_view.getText().equals(duration)) {
                        duration_view.setText(duration);
                    }
                }

                // Update assistance text
                TextView assistance_view = (TextView) getElement(SpeedoElements.ASSISTANCE);
                if (assistance_view != null) {
                    if (!assistance_view.getText().equals(String.valueOf(assistanceVal))) {
                        assistance_view.setText(String.valueOf(assistanceVal));
                    }
                }

                // Update assistance progress bar
                ProgressBar assistance_progressBar = (ProgressBar) getElement(SpeedoElements.ASSISTANCE_PROGRESS_BAR);
                if (assistance_progressBar != null) {
                    if (assistance_progressBar.getProgress() != assistanceVal) {
                        assistance_progressBar.setProgress(assistanceVal);
                        // TODO change color
                    }
                }
            }
        });

    }

    // Returns true if the current class is the portrait class
    protected abstract boolean isPortrait();

    // Returns the view of the SpeedoElements
    protected View getElement(SpeedoElements element) {
        int id;
        if (!isPortrait()) {
            id = element.getLandscape();
        } else {
            id = element.getPortrait();
        }

        if (id != 0) {
            return findViewById(id);
        }
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateSpeedo.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateSpeedo.interrupt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
