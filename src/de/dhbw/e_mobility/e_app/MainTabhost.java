package de.dhbw.e_mobility.e_app;

import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.ViewFlipper;

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
        // TODO changeContentView();

		activityHandler.setMainContext(this);
		deviceProvider.init();

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

        // changeContentView();

		// TODO wirft fehler?! TabHost tabHost = (TabHost) findViewById(R.id.main_tabhost);
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
			// TODO CurrentTab-Workaround �ndern
			// (damit Intent nicht "einfriert")
		}
	}

    /*
    private void changeContentView() {
        System.out.println("CHANGE");
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                || orientation == ActivityInfo.SCREEN_ORIENTATION_USER) {
            //setContentView(R.layout.tacho_quer);
            setContentView(findViewById(R.id.tacho_hoch));
        } else {
            // setContentView(R.layout.main_tabs);
            setContentView(findViewById(R.id.tabhost));
        }
    }
    */

	// TODO Controller Konfiguration Layout l�sst sich nicht in Manifest
	// einstellen??

	// TODO Vollbild habe ich entfernt, weil sonst z.b. die Pairinganfrage im
	// Hintergrund bleibt
}
