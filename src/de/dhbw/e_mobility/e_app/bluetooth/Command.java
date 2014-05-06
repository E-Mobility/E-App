package de.dhbw.e_mobility.e_app.bluetooth;

import android.util.Log;

public enum Command {
    AT("at"), // Attetion
    AT_0("at-0"), // Attention without response
    AT_DFLT("at-dflt"), // Auf Werkseinstellungen zuruecksetzten
    AT_UPWD_N("at-upwd"), // Passwort setzten (max. 9 Zeichen)
    AT_LOGOUT("at-logout"), // Abmelden
    AT_PUSH_N("at-push"), // 0: Aus, 1: Mechanische, 2: Plain-Text Ausgabe
    AT_PUSHINT_N("at-pushint"), // Pushintervall setzten (in 100ms)
    // AT_CALIBL("at-calibl"), // Autokalibrierung fuer unteren Strommessbereich
    AT_CALIBL_N("at-calibl"), // Kalibrierung fuer unteren Strommessbereich
    // AT_CALIBH("at-calibh"), // Autokalibrierung fuer oberen Strommessbereich
    AT_CALIBH_N("at-calibh"), // Kalibrierung fuer oberen Strommessbereich
    AT_PP_N("at-pp"), // Prozentsatz wie viel von 5V als Vollgas gelten
    AT_VL_N("at-vl"), // Erste Alarmschwelle (in 1/10 V)
    AT_VLL_N("at-vll"), // Zweite Alarmschwelle (in 1/10 V)
    // -------------------> Shutting Down System
    AT_CL_N("at-cl"), // Erste Alarmschwelle (in mAh)
    AT_CLL_N("at-cll"), // Zweite Alarmschwelle (in mAh)
    AT_MM_N("at-mm"), // Abstand zwischen zwei Tachoimpulsen (in mm)
    AT_KPC_N("at-kpc"), // Proportionaler Faktor zu Gashebel (0-16)
    AT_KIC_N("at-kic"), // Integraler Faktor zu Gashebel (0-16)
    AT_KPS_N("at-kps"), // Proportionaler Faktor zur Geschwindigkeitsregulierung (0-16)
    AT_KIS_N("at-kis"), // Integraler Faktor zur Geschwindigkeitsregulierung (0-16)
    AT_KK_N("at-kk"), // Fixer Faktor zur Geschwindigkeitsregulierung (0-100)
    AT_LIGHT_N("at-light"), // Licht aus/ein schalten (0-1)
    AT_LVL_N("at-lvl"), // Lichtspannung einstellen (1-4 = 6V-9V)
    AT_CCAP("at-ccap"), // Akkukapazitaetszaehler zuruecksetzten
    AT_CDIST("at-cdist"), // Tachoimpulszaehler zuruecksetzten
    AT_CL1_0_N("at-cl1.0"), // Anfahrstrom fuer Profil 0 (in mA)
    AT_CL1_1_N("at-cl1.1"), // Anfahrstrom fuer Profil 1 (in mA)
    AT_CL2_0_N("at-cl2.0"), // Dauerfahrstrom fuer Profil 0 (in mA)
    AT_CL2_1_N("at-cl2.1"), // Dauerfahrstrom fuer Profil 1 (in mA)
    AT_SL1_0_N("at-sl1.0"), // Max.-Geschwindigkeit Anfahhilfe fuer Profil 0 (in 1/10 km/h)
    AT_SL1_1_N("at-sl1.1"), // Max.-Geschwindigkeit Anfahhilfe fuer Profil 1 (in 1/10 km/h)
    AT_SL2_0_N("at-sl2.0"), // Max.-Geschwindigkeit fuer Profil 0 (in 1/10 km/h)
    AT_SL2_1_N("at-sl2.1"), // Max.-Geschwindigkeit fuer Profil 1 (in 1/10 km/h)
    AT_CLT_0_N("at-clt.0"), // Timer regelt von cl2 auf cl1 zurueck fuer Profil 0 (in s)
    AT_CLT_1_N("at-clt.1"), // Timer regelt von cl2 auf cl1 zurueck fuer Profil 1 (in s)
    AT_pedal_N("at-pedal"), // Pedaliererkennung (0: Gashebel, 1: ohne, 2: mit Richtung)
    // ------------------------ Realitaet: 0: Gashebel, 1: mit Richtung, 2: gar nicht
    AT_EON("at-eon"), // Motor einschalten
    AT_EOFF("at-eoff"), // Motor ausschalten
    AT_OTO_N("at-oto"), // Timout fuer Autologout setzten (in s)
    AT_CCM_N("at-ccm"), // Cruse-Control-Mode setzten (0: aus, 1: ein)
    AT_THM_N("at-thm"), // Gashebelmodus setzten (0: Transparent, 1: Linear, 2: Sensibel)
    AT_PR_N("at-pr"), // Profil auswaehlen (0-1: Profil, 2: Cruse-Control)
    // ------------------ Cruse-Control=Button verhalten waehlt beim Start das Profil
    AT_PTIME_N("at-ptime"), // Reaktionszeit der Pedaliererkennung (in 100ms)
    AT_VA_N("at-va"), // Korrekturwert fuer Spannungsanzeige (-30...30 in 1/10 V)
    AT_PARAM_LIST("at-?"), // Parameterliste ausgeben
    LOGIN(null); // Command for login

    private String command;
    private String value;

    // Constructor
    private Command(String theCommand) {
        command = theCommand;
        value = null;
    }

    // TODO AT_PP_N("at-pp") Gashebel anpassen
    // TODO Alarmschwellen miteinbeziehen
    // TODO licht ein/aus und Stromstaerke / Spannung zum Handy laden verwenden
    // TODO Reset tour: AT_CCAP, AT_CDIST und AT_MM_N

    // Returns the value
    public String getValue() {
        return value;
    }

    // Saves the value
    public void setValue(String theValue) {
        value = theValue;
    }

    // Returns the command
    public String getCommand() {
        return command;
    }

    @Override
    // Returns the executable command
    public String toString() {
        if (this.equals(LOGIN)) {
            if (value == null) {
                // ActivityHandler.getInstance().fireToast(
                // "Standardpasswort (1234) wurde verwendet!");
                // TODO Toast ausgeben
                Log.d("LOGIN", "Standardpasswort (1234) wurde verwendet!");
                return "1234";
            } else {
                return value;
            }
        } else if (value != null) {
            return command + "=" + value;
        }
        return command;
    }
}
