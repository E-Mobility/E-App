package de.dhbw.e_mobility.e_app;

public class ReallyDialog extends YesNoDialog {
    @Override
    protected int getText() {
        return R.string.dialog_really_text;
    }
}
