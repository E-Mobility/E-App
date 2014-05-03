package de.dhbw.e_mobility.e_app.speedo;

import android.app.Activity;
import android.os.Bundle;

import de.dhbw.e_mobility.e_app.R;

public class SpeedoLandscapeActivity extends SpeedoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speedo_landscape);
    }

    @Override
    protected boolean isPortrait() {
        return false;
    }
}