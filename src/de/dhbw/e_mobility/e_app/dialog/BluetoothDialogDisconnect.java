package de.dhbw.e_mobility.e_app.dialog;

import de.dhbw.e_mobility.e_app.R;
import de.dhbw.e_mobility.e_app.dialog.YesNoDialog;

public class BluetoothDialogDisconnect extends YesNoDialog {

    @Override
    protected int getText() {
        return R.string.dialog_disconnect;
    }
}
