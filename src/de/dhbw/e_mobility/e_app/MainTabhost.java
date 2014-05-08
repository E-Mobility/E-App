package de.dhbw.e_mobility.e_app;

import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import java.io.File;
import java.io.IOException;

import de.dhbw.e_mobility.e_app.bluetooth.DeviceProvider;
import de.dhbw.e_mobility.e_app.common.ActivityHandler;
import de.dhbw.e_mobility.e_app.settings.SettingsActivity;
import de.dhbw.e_mobility.e_app.settings.SettingsElements;
import de.dhbw.e_mobility.e_app.speedo.SpeedoLandscapeActivity;
import de.dhbw.e_mobility.e_app.speedo.SpeedoPortraitActivity;
import de.dhbw.e_mobility.e_app.total_stats.StatsTotalActivity;
import de.dhbw.e_mobility.e_app.tour_stats.StatsTourActivity;

public class MainTabhost extends ActivityGroup {

    private static String specSpeedo = "speedo";
    private static String specTour = "statusTour";
    private static String specTotal = "statusTotal";
    private static String specSettings = "settings";
    // Get ActivityHandler object
    private ActivityHandler activityHandler = ActivityHandler.getInstance();
    // Get DeviceProvider object
    private DeviceProvider deviceProvider = DeviceProvider
            .getInstance();
    private TabSpec speedoTab;
    private Intent speedoLandscape;
    private Intent speedoPortrait;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tabs);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // TODO changeContentView();

        activityHandler.setMainContext(this);
        deviceProvider.init();

        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this.getLocalActivityManager());

        speedoTab = tabHost.newTabSpec(specSpeedo);
        speedoTab.setIndicator(activityHandler.getStr(R.string.mainTab_speedo));

        speedoLandscape = new Intent(this, SpeedoLandscapeActivity.class);
        speedoPortrait = new Intent(this, SpeedoPortraitActivity.class);
        speedoTab.setContent(speedoPortrait);

        TabSpec tourTab = tabHost.newTabSpec(specTour);
        tourTab.setIndicator(activityHandler.getStr(R.string.mainTab_tour));
        tourTab.setContent(new Intent(this, StatsTourActivity.class));

        TabSpec totalTab = tabHost.newTabSpec(specTotal);
        totalTab.setIndicator(activityHandler.getStr(R.string.mainTab_total));
        totalTab.setContent(new Intent(this, StatsTotalActivity.class));

        TabSpec settingsTab = tabHost.newTabSpec(specSettings);
        settingsTab.setIndicator(activityHandler.getStr(R.string.mainTab_settings));
        settingsTab.setContent(new Intent(this, SettingsActivity.class));

        tabHost.addTab(speedoTab);
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
        if (SettingsElements.LOGGING.getSummary() != null) {
            saveLogcatToFile(getApplicationContext());
        }
    }

    public void saveLogcatToFile(Context context) {
        String fileName = "logcat_" + System.currentTimeMillis() + ".txt";
        File outputFile = new File(context.getExternalCacheDir(), fileName);
        activityHandler.fireToast(outputFile.getPath());

        try {
            @SuppressWarnings("unused") Process process = Runtime.getRuntime().exec("logcat -f " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("SAVE LOGCAT", "TO: " + outputFile.getAbsolutePath());
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
            speedoTab.setContent(speedoLandscape);
        } else {
            speedoTab.setContent(speedoPortrait);
        }

        if (tachoIsCurrent) {
            tabHost.setCurrentTab(0);
            // TODO CurrentTab-Workaround aendern
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
            //setContentView(R.layout.speedo_landscape);
            setContentView(findViewById(R.id.speedo_portrait));
        } else {
            // setContentView(R.layout.main_tabs);
            setContentView(findViewById(R.id.tabhost));
        }
    }
    */
}
