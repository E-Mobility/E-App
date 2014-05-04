package de.dhbw.e_mobility.e_app.speedo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import de.dhbw.e_mobility.e_app.R;

public class SpeedoPortraitActivity extends SpeedoActivity {

    private boolean timerAn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speedo_portrait);

        // Initialize the button
        Button tour_reset_view = (Button) getElement(SpeedoElements.TOUR_RESET);
        tour_reset_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timerAn) {
                    activityHandler.stopDurationTimer();
                } else {
                    activityHandler.startDurationTimer();
                }
                timerAn = !timerAn;
            }
        });
    }

    @Override
    protected boolean isPortrait() {
        return true;
    }
}
