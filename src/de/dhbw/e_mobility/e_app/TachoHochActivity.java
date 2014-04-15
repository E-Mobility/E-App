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
}
