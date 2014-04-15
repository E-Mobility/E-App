package de.dhbw.e_mobility.e_app.bluetooth;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import de.dhbw.e_mobility.e_app.R;

public class BluetoothDialogDisconnect extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.dialog_disconnect);

		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);

		// Initialize the buttons
		Button but_yes = (Button) findViewById(R.id.disconnect_yes);
		but_yes.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(Activity.RESULT_OK);
				finish();
			}
		});
		Button but_no = (Button) findViewById(R.id.disconnect_no);
		but_no.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		finish();
	}
}
