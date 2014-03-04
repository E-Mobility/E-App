package de.dhbw.e_mobility.e_app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class BluetoothActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// // Inflate the menu; this adds items to the action bar if it is
		// present.
		// getMenuInflater().inflate(R.menu.menu_home, menu);
		return true;
	}

}
