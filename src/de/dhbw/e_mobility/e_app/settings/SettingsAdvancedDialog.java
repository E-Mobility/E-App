package de.dhbw.e_mobility.e_app.settings;

import de.dhbw.e_mobility.e_app.R;
import de.dhbw.e_mobility.e_app.dialog.YesNoDialog;

public class SettingsAdvancedDialog extends YesNoDialog {

    @Override
    protected int getText() {
        return R.string.dialog_advanced_settings;
    }
}
