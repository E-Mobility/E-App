package de.dhbw.e_mobility.e_app.mode;

import de.dhbw.e_mobility.e_app.bluetooth.BluetoothCommands;
import de.dhbw.e_mobility.e_app.bluetooth.BluetoothDeviceProvider;

public abstract class Mode {

    private BluetoothDeviceProvider deviceProvider;

    public Mode() {
        deviceProvider = BluetoothDeviceProvider.getInstance();
    }

    void sendCommand(BluetoothCommands theCommand, String theValue) {
        theCommand.setValue(theValue);
        deviceProvider.sendCommand(theCommand);
    }


    public void initConfig() {

        setDefaultConfig();
        setModConfig();


    }

    private void setDefaultConfig() {
//        sendCommand(, );
        // TODO

        /*
                AT_KPC_N("at-kpc"), // Proportionaler Faktor zu Gashebel (0-16)
                AT_KIC_N("at-kic"), // Integraler Faktor zu Gashebel (0-16)
                AT_KPS_N("at-kps"), // Proportionaler Faktor zur
                // Geschwindigkeitsregulierung (0-16)
                AT_KIS_N("at-kis"), // Integraler Faktor zur
                // Geschwindigkeitsregulierung (0-16)
                AT_KK_N("at-kk"), // Fixer Faktor zur Geschwindigkeitsregulierung
                // (0-100)
                AT_CL1_0_N("at-cl1.0"), // Anfahrstrom f�r Profil 0 (in mA)
                AT_CL2_0_N("at-cl2.0"), // Dauerfahrstrom f�r Profil 0 (in mA)
                AT_SL1_0_N("at-sl1.0"), // Max.-Geschwindigkeit Anfahhilfe f�r Profil 0
                // (in 1/10 km/h)
                AT_SL2_0_N("at-sl2.0"), // Max.-Geschwindigkeit f�r Profil 0 (in 1/10
                // km/h)
                AT_CLT_0_N("at-clt.0"), // Timer regelt von cl2 auf cl1 zur�ck f�r
                // Profil 0 (in s)
                AT_pedal_N("at-pedal"), // Pedaliererkennung (0: Gashebel, 1: ohne, 2:
                // mit Richtung)
                // ------------------------ Realit�t: 0: Gashebel, 1: mit Richtung, 2:
                // gar nicht

                AT_PR_N("at-pr"), // Profil ausw�hlen (0-1: Profil, 2: Cruse-Control)
                // ------------------ Cruse-Control=Button verhalten w�hlt beim Start
                // das Profil

                AT_PTIME_N("at-ptime"), // Reaktionszeit der Pedaliererkennung (in
                // 100ms)

                AT_EON("at-eon"), // Motor einschalten
                AT_EOFF("at-eoff"), // Motor ausschalten
                AT_OTO_N("at-oto"), // Timout f�r Autologout setzten (in s)

                AT_CCM_N("at-ccm"), // Cruse-Control-Mode setzten (0: aus, 1: ein)
                AT_THM_N("at-thm"), // Gashebelmodus setzten (0: Transparent, 1:
                // Linear, 2: Sensibel)
                AT_VA_N("at-va"), // Korrekturwert f�r Spannungsanzeige (-30...30 in
                // 1/10 V)
                */
    }

    protected abstract void setModConfig();
}
