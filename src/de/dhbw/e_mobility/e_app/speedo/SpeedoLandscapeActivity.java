package de.dhbw.e_mobility.e_app.speedo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import de.dhbw.e_mobility.e_app.R;
import de.dhbw.e_mobility.e_app.bluetooth.Command;
import de.dhbw.e_mobility.e_app.bluetooth.DeviceProvider;

public class SpeedoLandscapeActivity extends SpeedoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speedo_landscape);

        // Initialise the progressbar clicks
        ProgressBar battery_progressBar = (ProgressBar) getElement(SpeedoElements.BATTERY_PROGRESS_BAR);
        if (battery_progressBar != null) {
            battery_progressBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(activityHandler.getMainContext())
                            .setTitle(R.string.battery)
                            .setMessage(R.string.settings_battery_sum)
                            .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    DeviceProvider.getInstance().sendCommand(Command.AT_CCAP);
                                }
                            }).setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
                }
            });
        }
    }

    @Override
    protected boolean isPortrait() {
        return false;
    }
}