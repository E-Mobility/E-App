package de.dhbw.e_mobility.e_app;

import de.dhbw.e_mobility.e_app.bluetooth.BluetoothCommands;

public enum SettingsElements {
    SCREEN_MAIN("settings_screen"), //
    BLUETOOTH("settings_bluetooth"), //
    PASSWORD("settings_password"), //
    AUTOLOG("settings_autolog"), //
    SPEED("settings_speed"), //
    ADVANCED("settings_advanced"), //
    CONTROLLER("settings_controller"), //
    DEVICE("settings_device"), //

    AT_0("AT_0_", BluetoothCommands.AT_0, "Attention without response"), //
    AT_DFLT("AT_DFLT_", BluetoothCommands.AT_DFLT,
            "Auf Werkseinstellungen zuruecksetzten"), //
    AT_LOGOUT("AT_LOGOUT_", BluetoothCommands.AT_LOGOUT, "Abmelden"), //
    AT_CCAP("AT_CCAP_", BluetoothCommands.AT_CCAP,
            "Akkukapazitaetszaehler zuruecksetzten"), //
    AT_CDIST("AT_CDIST_", BluetoothCommands.AT_CDIST,
            "Tachoimpulszaehler zuruecksetzten"), //
    AT_EON("AT_EON_", BluetoothCommands.AT_EON, "Motor einschalten"), //
    AT_EOFF("AT_EOFF_", BluetoothCommands.AT_EOFF, "Motor ausschalten"), //
    AT_PARAM_LIST("AT_PARAM_LIST_", BluetoothCommands.AT_PARAM_LIST,
            "Parameterliste ausgeben"), //
    LOGIN("AT_LOGIN_", BluetoothCommands.LOGIN, "Anmelden"), //

    AT_UPWD_N("AT_UPWD_N", BluetoothCommands.AT_UPWD_N,
            "Controller Passwort aendern"), //
    AT_PUSH_N("AT_PUSH_N", BluetoothCommands.AT_PUSH_N,
            "0: Aus, 1: Mechanische, 2: Plain-Text Ausgabe"), //
    AT_PUSHINT_N("AT_PUSHINT_N", BluetoothCommands.AT_PUSHINT_N,
            "Pushintervall setzten (in 100ms)"), //
    AT_CALIBL_N("AT_CALIBL_N", BluetoothCommands.AT_CALIBL_N,
            "Kalibrierung fuer unteren Strommessbereich"), //
    AT_CALIBH_N("AT_CALIBH_N", BluetoothCommands.AT_CALIBH_N,
            "Kalibrierung fuer oberen Strommessbereich"), //
    AT_PP_N("AT_PP_N", BluetoothCommands.AT_PP_N,
            "Prozentsatz wie viel von 5V als Vollgas gelten"), //
    AT_VL_N("AT_VL_N", BluetoothCommands.AT_VL_N,
            "Erste Alarmschwelle (in 1/10 V)"), //
    AT_VLL_N("AT_VLL_N", BluetoothCommands.AT_VLL_N,
            "Zweite Alarmschwelle (in 1/10 V)"), //
    AT_CL_N("AT_CL_N", BluetoothCommands.AT_CL_N,
            "Erste Alarmschwelle (in mAh)"), //
    AT_CLL_N("AT_CLL_N", BluetoothCommands.AT_CLL_N,
            "Zweite Alarmschwelle (in mAh)"), //
    AT_MM_N("AT_MM_N", BluetoothCommands.AT_MM_N,
            "Abstand zwischen zwei Tachoimpulsen (in mm)"), //
    AT_KPC_N("AT_KPC_N", BluetoothCommands.AT_KPC_N,
            "Proportionaler Faktor zu Gashebel (0-16)"), //
    AT_KIC_N("AT_KIC_N", BluetoothCommands.AT_KIC_N,
            "Integraler Faktor zu Gashebel (0-16)"), //
    AT_KPS_N("AT_KPS_N", BluetoothCommands.AT_KPS_N,
            "Proportionaler Faktor zur Geschwindigkeitsregulierung (0-16)"), //
    AT_KIS_N("AT_KIS_N", BluetoothCommands.AT_KIS_N,
            "Integraler Faktor zur Geschwindigkeitsregulierung (0-16)"), //
    AT_KK_N("AT_KK_N", BluetoothCommands.AT_KK_N,
            "Fixer Faktor zur Geschwindigkeitsregulierung (0-100)"), //
    AT_LIGHT_N("AT_LIGHT_N", BluetoothCommands.AT_LIGHT_N,
            "Licht aus/ein schalten (0-1)"), //
    AT_LVl_N("AT_LVL_N", BluetoothCommands.AT_LVL_N,
            "Lichtspannung einstellen (1-4 = 6V-9V)"), //
    AT_CL1_0_N("AT_CL1_0_N", BluetoothCommands.AT_CL1_0_N,
            "Anfahrstrom fuer Profil 0 (in mA)"), //
    AT_CL1_1_N("AT_CL1_1_N", BluetoothCommands.AT_CL1_1_N,
            "Anfahrstrom fuer Profil 1 (in mA)"), //
    AT_CL2_0_N("AT_CL2_0_N", BluetoothCommands.AT_CL2_0_N,
            "Dauerfahrstrom fuer Profil 0 (in mA)"), //
    AT_CL2_1_N("AT_CL2_1_N", BluetoothCommands.AT_CL2_1_N,
            "Dauerfahrstrom fuer Profil 1 (in mA)"), //
    AT_SL1_0_N("AT_SL1_0_N", BluetoothCommands.AT_SL1_0_N,
            "Max.-Geschwindigkeit Anfahhilfe fuer Profil 0 (in 1/10 km/h)"), //
    AT_SL1_1_N("AT_SL1_1_N", BluetoothCommands.AT_SL1_1_N,
            "Max.-Geschwindigkeit Anfahhilfe fuer Profil 1 (in 1/10 km/h)"), //
    AT_SL2_0_N("AT_SL2_0_N", BluetoothCommands.AT_SL2_0_N,
            "Max.-Geschwindigkeit fuer Profil 0 (in 1/10 km/h)"), //
    AT_SL2_1_N("AT_SL2_1_N", BluetoothCommands.AT_SL2_1_N,
            "Max.-Geschwindigkeit fuer Profil 1 (in 1/10 km/h)"), //
    AT_CLT_0_N("AT_CLT_0_N", BluetoothCommands.AT_CLT_0_N,
            "Timer regelt von cl2 auf cl1 zurueck fuer Profil 0 (in s)"), //
    AT_CLT_1_N("AT_CLT_1_N", BluetoothCommands.AT_CLT_1_N,
            "Timer regelt von cl2 auf cl1 zurueck fuer Profil 1 (in s)"), //
    AT_PEDAL_N("AT_PEDAL_N", BluetoothCommands.AT_pedal_N,
            "Pedaliererkennung (0: Gashebel, 1: ohne, 2: mit Richtung)"), //
    AT_OTO_N("AT_OTO_N", BluetoothCommands.AT_OTO_N,
            "Timout fuer Autologout setzten (in s)"), //
    AT_CCM_N("AT_CCM_N", BluetoothCommands.AT_CCM_N,
            "Cruse-Control-Mode setzten (0: aus, 1: ein)"), //
    AT_THM_N("AT_THM_N", BluetoothCommands.AT_THM_N,
            "Gashebelmodus setzten (0: Transparent, 1: Linear, 2: Sensibel)"), //
    AT_PR_N("AT_PR_N", BluetoothCommands.AT_PR_N,
            "Profil auswaehlen (0-1: Profil, 2: Cruse-Control)"), //
    AT_PTIME_N("AT_PTIME_N", BluetoothCommands.AT_PTIME_N,
            "Reaktionszeit der Pedaliererkennung (in 100ms)"), //
    AT_VA_N("AT_VA_N", BluetoothCommands.AT_VA_N,
            "Korrekturwert fuer Spannungsanzeige (-30...30 in 1/10 V)"); //

    private String key;
    private BluetoothCommands command;
    private String summary;

    // Constructor
    SettingsElements(String theKey) {
        this(theKey, null, null);
    }

    // Constructor
    SettingsElements(String theKey, BluetoothCommands theCommand, String theSummary) {
        key = theKey;
        command = theCommand;
        summary = theSummary;
    }

    // Returns the BluetoothCommands element
    public BluetoothCommands getCommand() {
        return command;
    }

    // Returns the element key
    public String getKey() {
        return key;
    }

    // Returns the summary of the element
    public String getSummary() {
        return summary;
    }

    @Override
    @Deprecated
    public String toString() {
        return null;
    }
}
