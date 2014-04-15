package de.dhbw.e_mobility.e_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDeviceProvider;

public class MainActivity extends Activity {

	// Get ActivityHandler object
	private ActivityHandler activityHandler = ActivityHandler.getInstance();
	private BluetoothDeviceProvider deviceProvider = BluetoothDeviceProvider
			.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		activityHandler.setMainContext(this);
		deviceProvider.init();

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (deviceProvider != null) {
			deviceProvider.unregisterReceiver();
			deviceProvider = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
		} else	if (item.getItemId() == R.id.action_tacho) {
				startActivity(new Intent(this, TachoHochActivity.class));
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
