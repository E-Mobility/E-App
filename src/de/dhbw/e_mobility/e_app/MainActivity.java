package de.dhbw.e_mobility.e_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	// TODO Activities beim wechseln schliessen oder so, weil sonst mehrere
	// uebereinander geoeffnet sind..

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.action_speedo:
			startActivity(new Intent(this, SpeedoActivity.class));
			return true;
		case R.id.action_help:
			startActivity(new Intent(this, HelpActivity.class));
			return true;
		case R.id.action_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
