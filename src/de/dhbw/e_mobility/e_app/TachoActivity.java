package de.dhbw.e_mobility.e_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDeviceProvider;

public class TachoActivity extends Activity {

	// Get ActivityHandler object
	private ActivityHandler activityHandler = ActivityHandler.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tacho_hoch);

		activityHandler.setMainContext(this);

		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		// setContentView(R.layout.main_screen);
		//
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		// R.layout.custom_title);

		// Inflate the menu; this adds items to the action bar if it is present.
	}

	@Override
	protected void onStart() {
		super.onStart();
		activityHandler.add(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		activityHandler.del(this);
	}

	
}
