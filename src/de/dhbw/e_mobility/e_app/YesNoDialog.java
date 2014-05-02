package de.dhbw.e_mobility.e_app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public abstract class YesNoDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.dialog_yes_no);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the buttons
        Button but_yes = (Button) findViewById(R.id.dialog_yes);
        but_yes.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
        Button but_no = (Button) findViewById(R.id.dialog_no);
        but_no.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        TextView txt = (TextView) findViewById(R.id.dialog_text);
        txt.setText(getText());
    }

    protected abstract int getText();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
