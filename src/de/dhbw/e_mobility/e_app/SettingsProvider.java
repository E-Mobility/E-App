package de.dhbw.e_mobility.e_app;

import de.dhbw.e_mobility.e_app.bluetooth.BluetoothInfoState;

public class SettingsProvider {

	private static SettingsProvider settingsProvider = null;

	public static SettingsProvider getInstance() {
		if (settingsProvider == null) {
			settingsProvider = new SettingsProvider();
		}
		return settingsProvider;
	}

	private SettingsProvider() {
		advancedSettings = false;
		bluetoothState = null;
		deviceName = "";

		at_pushint = -1;
		at_calibl = -1;
		at_calibh = -1;
		at_pp = -1;
		at_vl = -1;
		at_vll = -1;
		at_cl = -1;
		at_cll = -1;
		at_mm = -1;
		at_kpc = -1;
		at_kic = -1;
		at_kps = -1;
		at_kis = -1;
		at_kk = -1;
		at_light = -1;
		at_lvl = -1;
		at_cl1_0 = -1;
		at_cl1_1 = -1;
		at_cl2_0 = -1;
		at_cl2_1 = -1;
		at_sl1_0 = -1;
		at_sl1_1 = -1;
		at_sl2_0 = -1;
		at_sl2_1 = -1;
		at_clt_0 = -1;
		at_clt_1 = -1;
		at_pedal = -1;
		at_oto = -1;
		at_ccm = -1;
		at_thm = -1;
		at_pr = -1;
		at_ptime = -1;
		at_va = -1;
	}

	private boolean advancedSettings;
	private BluetoothInfoState bluetoothState;
	private String deviceName;

	// Parameter
	private int at_pushint; // Pushintervall setzten (in 100ms)
	private int at_calibl; // Kalibrierung für unteren Strommessbereich
	private int at_calibh; // Kalibrierung für oberen Strommessbereich
	private int at_pp; // Prozentsatz wie viel von 5V als Vollgas gelten
	private int at_vl; // Erste Alarmschwelle (in 1/10 V)
	private int at_vll; // Zweite Alarmschwelle (in 1/10 V) --> Shutting Down
						// System
	private int at_cl; // Erste Alarmschwelle (in mAh)
	private int at_cll; // Zweite Alarmschwelle (in mAh)
	private int at_mm; // Abstand zwischen zwei Tachoimpulsen (in mm)
	private int at_kpc; // Proportionaler Faktor zu Gashebel (0-16)
	private int at_kic; // Integraler Faktor zu Gashebel (0-16)
	private int at_kps; // Proportionaler Faktor zur Geschwindigkeitsregulierung
						// (0-16)
	private int at_kis; // Integraler Faktor zur Geschwindigkeitsregulierung
						// (0-16)
	private int at_kk; // Fixer Faktor zur Geschwindigkeitsregulierung (0-100)
	private int at_light; // Licht aus/ein schalten (0-1)
	private int at_lvl; // Lichtspannung einstellen (1-4 = 6V-9V)
	private int at_cl1_0; // Anfahrstrom für Profil 0 (in mA)
	private int at_cl1_1; // Anfahrstrom für Profil 1 (in mA)
	private int at_cl2_0; // Dauerfahrstrom für Profil 0 (in mA)
	private int at_cl2_1; // Dauerfahrstrom für Profil 1 (in mA)
	private int at_sl1_0; // Max.-Geschwindigkeit Anfahhilfe für Profil 0 (in
							// 1/10 km/h)
	private int at_sl1_1; // Max.-Geschwindigkeit Anfahhilfe für Profil 1 (in
							// 1/10 km/h)
	private int at_sl2_0; // Max.-Geschwindigkeit für Profil 0 (in 1/10 km/h)
	private int at_sl2_1; // Max.-Geschwindigkeit für Profil 1 (in 1/10 km/h)
	private int at_clt_0; // Timer regelt von cl2 auf cl1 zurück für Profil 0
							// (in s)
	private int at_clt_1; // Timer regelt von cl2 auf cl1 zurück für Profil 1
							// (in s)
	private int at_pedal; // Pedaliererkennung (0: Gashebel, 1: ohne, 2: mit
							// Richtung)
	// ------------------ //Realität: 0: Gashebel, 1: mit Richtung, 2: gar nicht
	private int at_oto; // Timout für Autologout setzten (in s)
	private int at_ccm; // Cruse-Control-Mode setzten (0: aus, 1: ein)
	private int at_thm; // Gashebelmodus setzten (0: Transparent, 1: Linear, 2:
						// Sensibel)
	private int at_pr; // Profil auswählen (0-1: Profil, 2: Cruse-Control)
	// --------------- //Cruse-Control=Button verhalten wählt beim Start das
	// Profil
	private int at_ptime; // Reaktionszeit der Pedaliererkennung (in 100ms)
	private int at_va; // Korrekturwert für Spannungsanzeige (-30...30 in 1/10
						// V)

	// Saves all parameter from parameterlist
	public void saveParameters(String parList) {
		String command, value;
		String[] tmp;
		for (String line : parList.split("\n")) {
			tmp = line.split("=");
			command = tmp[0];
			value = tmp[1];

			if (command.equals("at-pushint")) {
				at_pushint = Integer.valueOf(value);
			} else if (command.equals("at-calibl")) {
				at_calibl = Integer.valueOf(value);
			} else if (command.equals("at-calibh")) {
				at_calibh = Integer.valueOf(value);
			} else if (command.equals("at-pp")) {
				at_pp = Integer.valueOf(value);
			} else if (command.equals("at-vl")) {
				at_vl = Integer.valueOf(value);
			} else if (command.equals("at-vll")) {
				at_vll = Integer.valueOf(value);
			} else if (command.equals("at-cl")) {
				at_cl = Integer.valueOf(value);
			} else if (command.equals("at-cll")) {
				at_cll = Integer.valueOf(value);
			} else if (command.equals("at-mm")) {
				at_mm = Integer.valueOf(value);
			} else if (command.equals("at-kpc")) {
				at_kpc = Integer.valueOf(value);
			} else if (command.equals("at-kic")) {
				at_kic = Integer.valueOf(value);
			} else if (command.equals("at-kps")) {
				at_kps = Integer.valueOf(value);
			} else if (command.equals("at-kis")) {
				at_kis = Integer.valueOf(value);
			} else if (command.equals("at-kk")) {
				at_kk = Integer.valueOf(value);
			} else if (command.equals("at-light")) {
				at_light = Integer.valueOf(value);
			} else if (command.equals("at-lvl")) {
				at_lvl = Integer.valueOf(value);
			} else if (command.equals("at-cl1.0")) {
				at_cl1_0 = Integer.valueOf(value);
			} else if (command.equals("at-cl1.1")) {
				at_cl1_1 = Integer.valueOf(value);
			} else if (command.equals("at-cl2.0")) {
				at_cl2_0 = Integer.valueOf(value);
			} else if (command.equals("at-cl2.1")) {
				at_cl2_1 = Integer.valueOf(value);
			} else if (command.equals("at-sl1.0")) {
				at_sl1_0 = Integer.valueOf(value);
			} else if (command.equals("at-sl1.1")) {
				at_sl1_1 = Integer.valueOf(value);
			} else if (command.equals("at-sl2.0")) {
				at_sl2_0 = Integer.valueOf(value);
			} else if (command.equals("at-sl2.1")) {
				at_sl2_1 = Integer.valueOf(value);
			} else if (command.equals("at-clt.0")) {
				at_clt_0 = Integer.valueOf(value);
			} else if (command.equals("at-clt.1")) {
				at_clt_1 = Integer.valueOf(value);
			} else if (command.equals("at-pedal")) {
				at_pedal = Integer.valueOf(value);
			} else if (command.equals("at-oto")) {
				at_oto = Integer.valueOf(value);
			} else if (command.equals("at-ccm")) {
				at_ccm = Integer.valueOf(value);
			} else if (command.equals("at-thm")) {
				at_thm = Integer.valueOf(value);
			} else if (command.equals("at-pr")) {
				at_pr = Integer.valueOf(value);
			} else if (command.equals("at-ptime")) {
				at_ptime = Integer.valueOf(value);
			} else if (command.equals("at-va")) {
				at_va = Integer.valueOf(value);
			}
		}
	}

	/**
	 * @return the at_pushint
	 */
	public int getAt_pushint() {
		return at_pushint;
	}

	/**
	 * @param at_pushint
	 *            the at_pushint to set
	 */
	public void setAt_pushint(int at_pushint) {
		this.at_pushint = at_pushint;
	}

	/**
	 * @return the at_calibl
	 */
	public int getAt_calibl() {
		return at_calibl;
	}

	/**
	 * @param at_calibl
	 *            the at_calibl to set
	 */
	public void setAt_calibl(int at_calibl) {
		this.at_calibl = at_calibl;
	}

	/**
	 * @return the at_calibh
	 */
	public int getAt_calibh() {
		return at_calibh;
	}

	/**
	 * @param at_calibh
	 *            the at_calibh to set
	 */
	public void setAt_calibh(int at_calibh) {
		this.at_calibh = at_calibh;
	}

	/**
	 * @return the at_pp
	 */
	public int getAt_pp() {
		return at_pp;
	}

	/**
	 * @param at_pp
	 *            the at_pp to set
	 */
	public void setAt_pp(int at_pp) {
		this.at_pp = at_pp;
	}

	/**
	 * @return the at_vl
	 */
	public int getAt_vl() {
		return at_vl;
	}

	/**
	 * @param at_vl
	 *            the at_vl to set
	 */
	public void setAt_vl(int at_vl) {
		this.at_vl = at_vl;
	}

	/**
	 * @return the at_vll
	 */
	public int getAt_vll() {
		return at_vll;
	}

	/**
	 * @param at_vll
	 *            the at_vll to set
	 */
	public void setAt_vll(int at_vll) {
		this.at_vll = at_vll;
	}

	/**
	 * @return the at_cl
	 */
	public int getAt_cl() {
		return at_cl;
	}

	/**
	 * @param at_cl
	 *            the at_cl to set
	 */
	public void setAt_cl(int at_cl) {
		this.at_cl = at_cl;
	}

	/**
	 * @return the at_cll
	 */
	public int getAt_cll() {
		return at_cll;
	}

	/**
	 * @param at_cll
	 *            the at_cll to set
	 */
	public void setAt_cll(int at_cll) {
		this.at_cll = at_cll;
	}

	/**
	 * @return the at_mm
	 */
	public int getAt_mm() {
		return at_mm;
	}

	/**
	 * @param at_mm
	 *            the at_mm to set
	 */
	public void setAt_mm(int at_mm) {
		this.at_mm = at_mm;
	}

	/**
	 * @return the at_kpc
	 */
	public int getAt_kpc() {
		return at_kpc;
	}

	/**
	 * @param at_kpc
	 *            the at_kpc to set
	 */
	public void setAt_kpc(int at_kpc) {
		this.at_kpc = at_kpc;
	}

	/**
	 * @return the at_kic
	 */
	public int getAt_kic() {
		return at_kic;
	}

	/**
	 * @param at_kic
	 *            the at_kic to set
	 */
	public void setAt_kic(int at_kic) {
		this.at_kic = at_kic;
	}

	/**
	 * @return the at_kps
	 */
	public int getAt_kps() {
		return at_kps;
	}

	/**
	 * @param at_kps
	 *            the at_kps to set
	 */
	public void setAt_kps(int at_kps) {
		this.at_kps = at_kps;
	}

	/**
	 * @return the at_kis
	 */
	public int getAt_kis() {
		return at_kis;
	}

	/**
	 * @param at_kis
	 *            the at_kis to set
	 */
	public void setAt_kis(int at_kis) {
		this.at_kis = at_kis;
	}

	/**
	 * @return the at_kk
	 */
	public int getAt_kk() {
		return at_kk;
	}

	/**
	 * @param at_kk
	 *            the at_kk to set
	 */
	public void setAt_kk(int at_kk) {
		this.at_kk = at_kk;
	}

	/**
	 * @return the at_light
	 */
	public int getAt_light() {
		return at_light;
	}

	/**
	 * @param at_light
	 *            the at_light to set
	 */
	public void setAt_light(int at_light) {
		this.at_light = at_light;
	}

	/**
	 * @return the at_lvl
	 */
	public int getAt_lvl() {
		return at_lvl;
	}

	/**
	 * @param at_lvl
	 *            the at_lvl to set
	 */
	public void setAt_lvl(int at_lvl) {
		this.at_lvl = at_lvl;
	}

	/**
	 * @return the at_cl1_0
	 */
	public int getAt_cl1_0() {
		return at_cl1_0;
	}

	/**
	 * @param at_cl1_0
	 *            the at_cl1_0 to set
	 */
	public void setAt_cl1_0(int at_cl1_0) {
		this.at_cl1_0 = at_cl1_0;
	}

	/**
	 * @return the at_cl1_1
	 */
	public int getAt_cl1_1() {
		return at_cl1_1;
	}

	/**
	 * @param at_cl1_1
	 *            the at_cl1_1 to set
	 */
	public void setAt_cl1_1(int at_cl1_1) {
		this.at_cl1_1 = at_cl1_1;
	}

	/**
	 * @return the at_cl2_0
	 */
	public int getAt_cl2_0() {
		return at_cl2_0;
	}

	/**
	 * @param at_cl2_0
	 *            the at_cl2_0 to set
	 */
	public void setAt_cl2_0(int at_cl2_0) {
		this.at_cl2_0 = at_cl2_0;
	}

	/**
	 * @return the at_cl2_1
	 */
	public int getAt_cl2_1() {
		return at_cl2_1;
	}

	/**
	 * @param at_cl2_1
	 *            the at_cl2_1 to set
	 */
	public void setAt_cl2_1(int at_cl2_1) {
		this.at_cl2_1 = at_cl2_1;
	}

	/**
	 * @return the at_sl1_0
	 */
	public int getAt_sl1_0() {
		return at_sl1_0;
	}

	/**
	 * @param at_sl1_0
	 *            the at_sl1_0 to set
	 */
	public void setAt_sl1_0(int at_sl1_0) {
		this.at_sl1_0 = at_sl1_0;
	}

	/**
	 * @return the at_sl1_1
	 */
	public int getAt_sl1_1() {
		return at_sl1_1;
	}

	/**
	 * @param at_sl1_1
	 *            the at_sl1_1 to set
	 */
	public void setAt_sl1_1(int at_sl1_1) {
		this.at_sl1_1 = at_sl1_1;
	}

	/**
	 * @return the at_sl2_0
	 */
	public int getAt_sl2_0() {
		return at_sl2_0;
	}

	/**
	 * @param at_sl2_0
	 *            the at_sl2_0 to set
	 */
	public void setAt_sl2_0(int at_sl2_0) {
		this.at_sl2_0 = at_sl2_0;
	}

	/**
	 * @return the at_sl2_1
	 */
	public int getAt_sl2_1() {
		return at_sl2_1;
	}

	/**
	 * @param at_sl2_1
	 *            the at_sl2_1 to set
	 */
	public void setAt_sl2_1(int at_sl2_1) {
		this.at_sl2_1 = at_sl2_1;
	}

	/**
	 * @return the at_clt_0
	 */
	public int getAt_clt_0() {
		return at_clt_0;
	}

	/**
	 * @param at_clt_0
	 *            the at_clt_0 to set
	 */
	public void setAt_clt_0(int at_clt_0) {
		this.at_clt_0 = at_clt_0;
	}

	/**
	 * @return the at_clt_1
	 */
	public int getAt_clt_1() {
		return at_clt_1;
	}

	/**
	 * @param at_clt_1
	 *            the at_clt_1 to set
	 */
	public void setAt_clt_1(int at_clt_1) {
		this.at_clt_1 = at_clt_1;
	}

	/**
	 * @return the at_pedal
	 */
	public int getAt_pedal() {
		return at_pedal;
	}

	/**
	 * @param at_pedal
	 *            the at_pedal to set
	 */
	public void setAt_pedal(int at_pedal) {
		this.at_pedal = at_pedal;
	}

	/**
	 * @return the at_oto
	 */
	public int getAt_oto() {
		return at_oto;
	}

	/**
	 * @param at_oto
	 *            the at_oto to set
	 */
	public void setAt_oto(int at_oto) {
		this.at_oto = at_oto;
	}

	/**
	 * @return the at_ccm
	 */
	public int getAt_ccm() {
		return at_ccm;
	}

	/**
	 * @param at_ccm
	 *            the at_ccm to set
	 */
	public void setAt_ccm(int at_ccm) {
		this.at_ccm = at_ccm;
	}

	/**
	 * @return the at_thm
	 */
	public int getAt_thm() {
		return at_thm;
	}

	/**
	 * @param at_thm
	 *            the at_thm to set
	 */
	public void setAt_thm(int at_thm) {
		this.at_thm = at_thm;
	}

	/**
	 * @return the at_pr
	 */
	public int getAt_pr() {
		return at_pr;
	}

	/**
	 * @param at_pr
	 *            the at_pr to set
	 */
	public void setAt_pr(int at_pr) {
		this.at_pr = at_pr;
	}

	/**
	 * @return the at_ptime
	 */
	public int getAt_ptime() {
		return at_ptime;
	}

	/**
	 * @param at_ptime
	 *            the at_ptime to set
	 */
	public void setAt_ptime(int at_ptime) {
		this.at_ptime = at_ptime;
	}

	/**
	 * @return the at_va
	 */
	public int getAt_va() {
		return at_va;
	}

	/**
	 * @param at_va
	 *            the at_va to set
	 */
	public void setAt_va(int at_va) {
		this.at_va = at_va;
	}

	/**
	 * @return the advancedSettings
	 */
	public boolean isAdvancedSettings() {
		return advancedSettings;
	}

	/**
	 * @param advancedSettings
	 *            the advancedSettings to set
	 */
	public void setAdvancedSettings(boolean advancedSettings) {
		this.advancedSettings = advancedSettings;
	}

	/**
	 * @param bluetoothState
	 *            the bluetoothState to set
	 */
	public void setBluetoothState(BluetoothInfoState bluetoothState) {
		setBluetoothState(bluetoothState, "");
	}

	/**
	 * @param bluetoothState
	 *            the bluetoothState to set
	 * @param deviceName
	 *            the deviceName to set
	 */
	public void setBluetoothState(BluetoothInfoState bluetoothState,
			String deviceName) {
		this.bluetoothState = bluetoothState;
		this.deviceName = deviceName;
	}

	/**
	 * @return true if currently logged in
	 */
	public boolean isLoggedIn() {
		return (bluetoothState == BluetoothInfoState.LOGGED_IN);
	}

	/**
	 * @return the bluetooth info text
	 */
	public String getBluetoothState() {
		if (bluetoothState == null) {
			return "";
		}
		return bluetoothState.toString() + deviceName;
	}
}
