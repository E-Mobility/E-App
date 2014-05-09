package de.dhbw.e_mobility.e_app.speedo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import de.dhbw.e_mobility.e_app.R;
import de.dhbw.e_mobility.e_app.bluetooth.Command;
import de.dhbw.e_mobility.e_app.bluetooth.DeviceProvider;

public class SpeedoPortraitActivity extends SpeedoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speedo_portrait);

        // Initialize the button
        Button tour_reset_view = (Button) getElement(SpeedoElements.TOUR_RESET);
        tour_reset_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(activityHandler.getMainContext())
                        .setTitle(R.string.dialog_really_title)
                        .setMessage(R.string.dialog_reset_tour)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                activityHandler.resetDuration();
                                SpeedoValues.DISTANCE.setValue(0);
                            }
                        }).setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                        .show();
            }
        });

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
        return true;
    }
}
