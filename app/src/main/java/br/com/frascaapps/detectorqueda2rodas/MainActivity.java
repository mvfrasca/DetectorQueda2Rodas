package br.com.frascaapps.detectorqueda2rodas;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
//import android.widget.EditText;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.ToggleButton;
//import android.app.ActivityManager.;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    protected AppCompatActivity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        MonitoramentoSensores monitor = new MonitoramentoSensores();

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButtonHabilitaLeitura);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 Intent i = new Intent(activity, MonitoramentoSensores.class);
                 if (isChecked) {
                    // Botão ligado
                    startService(i);
                    //AlertDialog.Builder builder = new AlertDialog.Builder(getParent().getApplicationContext());
//                    TextView editText = (TextView) findViewById(R.id.textViewAcelerometro);
//                    String message = editText.getText().toString();
//
//                    builder.setMessage(message)
//                            .setTitle(R.string.app_name);
//                    AlertDialog dialog = builder.create();

                } else {
                    // Botão desligado
                    stopService(i);
                }
            }
        });

            }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //        textViewX.setText("Posição X: " + x.intValue() + " Float: " + x);
    //        textViewY.setText("Posição Y: " + y.intValue() + " Float: " + y);
    //        textViewZ.setText("Posição Z: " + z.intValue() + " Float: " + z);
}
