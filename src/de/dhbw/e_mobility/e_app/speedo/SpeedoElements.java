package de.dhbw.e_mobility.e_app.speedo;

import de.dhbw.e_mobility.e_app.R;

public enum SpeedoElements {
    BLUETOOTH_ICON(R.id.tab_bluetooth_icon, R.id.landscape_bluetooth_icon), CLOCK(R.id.tab_clock, R.id.landscape_clock),
    SPEED_TITLE(R.id.portrait_speed_title), SPEED(R.id.portrait_speed, R.id.landscape_speed), SPEED_UNIT(R.id.portrait_speed_unit, R.id.landscape_speed_unit),
    MODE(R.id.portrait_mode, R.id.landscape_mode),
    TOUR_TITLE(R.id.portrait_tour_title), TOUR_RESET(R.id.portrait_tour_reset),
    DISTANCE_TITLE(R.id.portrait_distance_title), DISTANCE(R.id.portrait_distance), DISTANCE_UNIT(R.id.portrait_distance_unit),
    DURATION_TITLE(R.id.portrait_duration_title), DURATION(R.id.portrait_duration), DURATION_UNIT(R.id.portrait_duration_unit),
    BATTERY_TITLE(R.id.portrait_battery_title), BATTERY(R.id.portrait_battery, R.id.landscape_battery), BATTERY_UNIT(R.id.portrait_battery_unit, R.id.landscape_battery_unit), BATTERY_PROGRESS_BAR(R.id.portrait_battery_progress_bar, R.id.landscape_battery_progress_bar),
    ASSISTANCE_TITLE(R.id.portrait_assistance_title), ASSISTANCE(R.id.portrait_assistance, R.id.landscape_assistance), ASSISTANCE_UNIT(R.id.portrait_assistance_unit, R.id.landscape_assistance_unit), ASSISTANCE_PROGRESS_BAR(R.id.portrait_assistance_progress_bar, R.id.landscape_assistance_progress_bar);

    private int portrait;
    private int landscape;

    // Constructor
    SpeedoElements(int thePortrait, int theLandscape) {
        portrait = thePortrait;
        landscape = theLandscape;
    }

    // Constructor
    SpeedoElements(int thePortrait) {
        this(thePortrait, 0);
    }

    // Returns the portrait element
    public int getPortrait() {
        return portrait;
    }

    // Returns the landscape element
    public int getLandscape() {
        return landscape;
    }

    @Override
    @Deprecated
    public String toString() {
        return null;
    }
}
