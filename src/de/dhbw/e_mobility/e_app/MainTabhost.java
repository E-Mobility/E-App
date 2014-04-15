package de.dhbw.e_mobility.e_app;

import android.app.ActivityGroup;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDeviceProvider;

public class MainTabhost extends ActivityGroup {

	// Get ActivityHandler object
	private ActivityHandler activityHandler = ActivityHandler.getInstance();
	private BluetoothDeviceProvider deviceProvider = BluetoothDeviceProvider
			.getInstance();

	private TabSpec tachoTab;
	private Intent tachoQuer;
	private Intent tachoHoch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tabs);

		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup(this.getLocalActivityManager());

		tachoTab = tabHost.newTabSpec("tacho");
		tachoTab.setIndicator("Tacho");

		tachoQuer = new Intent(this, TachoQuerActivity.class);
		tachoHoch = new Intent(this, TachoHochActivity.class);
		tachoTab.setContent(tachoHoch);

		TabSpec tourTab = tabHost.newTabSpec("statusTour");
		tourTab.setIndicator("Tour Status");
		tourTab.setContent(new Intent(this, StatusTourActivity.class));

		TabSpec totalTab = tabHost.newTabSpec("statusTotal");
		totalTab.setIndicator("Gesamt Status");
		totalTab.setContent(new Intent(this, StatusTotalActivity.class));

		TabSpec settingsTab = tabHost.newTabSpec("settings");
		settingsTab.setIndicator("Einstellungen");
		settingsTab.setContent(new Intent(this, SettingsActivity.class));

		tabHost.addTab(tachoTab);
		tabHost.addTab(tourTab);
		tabHost.addTab(totalTab);
		tabHost.addTab(settingsTab);

		activityHandler.setMainContext(this);
		deviceProvider.init();
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
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
		boolean tachoIsCurrent = (tabHost.getCurrentTab() == 0);
		if (tachoIsCurrent) {
			tabHost.setCurrentTab(1);
		}

		if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
				|| newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
				|| newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER) {
			tachoTab.setContent(tachoQuer);
		} else {
			tachoTab.setContent(tachoHoch);
		}
		if (tachoIsCurrent) {
			tabHost.setCurrentTab(0);
			// TODO CurrentTab-Workaround ändern
			// (damit Intent nicht "einfriert")
		}
	}

	// TODO Controller Konfiguration Layout lässt sich nicht in Manifest
	// einstellen??
}
