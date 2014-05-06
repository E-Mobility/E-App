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
                // TODO! asking if user really want.. and reset whole tour
                activityHandler.resetDuration();
                SpeedoValues.DISTANCE.setValue(0);
            }
        });
    }

    @Override
    protected boolean isPortrait() {
        return true;
    }
}
