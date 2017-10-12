package br.com.frascaapps.detectorqueda2rodas;

import android.content.Intent;
//import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
//import android.webkit.WebSettings;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.webkit.WebView;

import br.com.frascaapps.detectorqueda2rodas.util.FileUtils;
import br.com.frascaapps.detectorqueda2rodas.util.Logger;

//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//import br.com.frascaapps.detectorqueda2rodas.util.Logger;
//
//import static android.provider.AlarmClock.EXTRA_MESSAGE;
//import static android.webkit.WebSettings.*;

public class MainActivity extends AppCompatActivity {

    protected AppCompatActivity activity = this;
    public static final String ASSET_PATH = "file:///android_asset/";
    private WebView webViewGrafico = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButtonHabilitaLeitura);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 Intent i = new Intent(activity, MonitoramentoSensores.class);

                if (isChecked) {
                    // Botão ligado
//                    Intent startMain = new Intent(Intent.ACTION_MAIN);
//                    startMain.addCategory(Intent.CATEGORY_HOME);
//                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                     startActivity(startMain);
                    startService(i);
                } else {
                    // Botão desligado
                    stopService(i);
                }
            }
        });

/*        // COnfiguração da webview para exibição do gráfico
        webViewGrafico = (WebView) findViewById(R.id.webViewGrafico);
        WebSettings webSettings = webViewGrafico.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);

        carregarGrafico();*/
    }

    /*private void carregarGrafico() {
        String content = null;
        try {
            AssetManager assetManager = getAssets();
            InputStream in = assetManager.open("template_grafico.html");
            byte[] bytes = readFully(in);
            content = new String(bytes, "UTF-8");
        } catch (Exception e) {
            Logger.log("Erro ao carregar gráfico: " + e.toString());
        }
        //String formattedContent = String.format(content, mushrooms, onions, olives, pepperoni);
        //webView.loadDataWithBaseURL(ASSET_PATH, formattedContent, "text/html", "utf-8", null);
        webViewGrafico.loadDataWithBaseURL(ASSET_PATH, content, "text/html", "utf-8", null);
        webViewGrafico.requestFocusFromTouch();
    }

    private static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }*/

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
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.menuExpurgar:
                Logger.expurgarLog();
                Database db = Database.getInstance(this);
                db.expurgarBD();
                break;
            default:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //        textViewX.setText("Posição X: " + x.intValue() + " Float: " + x);
    //        textViewY.setText("Posição Y: " + y.intValue() + " Float: " + y);
    //        textViewZ.setText("Posição Z: " + z.intValue() + " Float: " + z);
}
