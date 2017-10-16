package br.com.frascaapps.detectorqueda2rodas;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
//import android.content.res.AssetManager;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telecom.ConnectionService;
import android.view.Menu;
import android.view.MenuItem;
//import android.webkit.WebSettings;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.webkit.WebView;

import br.com.frascaapps.detectorqueda2rodas.util.FileUtils;
import br.com.frascaapps.detectorqueda2rodas.util.Logger;
import br.com.frascaapps.detectorqueda2rodas.MonitoramentoSensores.LocalBinder;

import static br.com.frascaapps.detectorqueda2rodas.R.id.parent;

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
//    public static final String ASSET_PATH = "file:///android_asset/";
//    private WebView webViewGrafico = null;
    Intent myMonitoramentoSensoresActivity = null;
    Messenger mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButtonHabilitaLeitura);
        toggle.setOnCheckedChangeListener (new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {

//
                    if (isChecked) {
                        if (MainActivity.this.myMonitoramentoSensoresActivity == null) {
                            MainActivity.this.myMonitoramentoSensoresActivity = new Intent(MainActivity.this, MonitoramentoSensores.class);
                            MainActivity.this.myMonitoramentoSensoresActivity.setPackage(getPackageName());
                        }
//                        Intent i = new Intent(activity, MonitoramentoSensores.class);

                        //Intent i = new Intent(MainActivity.this.myMonitoramentoSensoresActivity);
                          // Botão ligado
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                         startActivity(startMain);
                        Logger.log("\n\n\nInicio do serviço solicitado pelo usuário");
//                        startService(i);
//                        startService(MainActivity.this.myMonitoramentoSensoresActivity);
                        Intent intent = new Intent(MainActivity.this, MonitoramentoSensores.class);
                        startService(intent);
                        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    } else {
                        // Botão desligado
                        Logger.log("Parada do serviço solicitada pelo usuário");
//                        boolean bReturn = stopService(MainActivity.this.myMonitoramentoSensoresActivity);
//                        if (!mBound) return;
                        // Create and send a message to the service, using a supported 'what' value
                        Message msg = Message.obtain(null, MonitoramentoSensores.MSG_PARAR_MONITORAMENTO, 0, 0);
                        try {
                            Logger.log("Envindo mensagem de parada ao serviço.");
                            mService.send(msg);
                            Logger.log("Mensagem de parada enviada com sucesso.");
                        } catch (RemoteException e) {
                            Logger.log("Erro ao enviar mensagem de parada ao serviço: " + e.toString() + "\n" + e.getStackTrace().toString());
                            e.printStackTrace();
                        }
                        unbindService(mConnection);
                        mBound = false;

                    }
                } catch (Exception e) {
                    Logger.log("toggleButtonHabilitaLeitura.onCheckedChanged - Erro: " + e.toString() + "\n" + e.getStackTrace().toString());
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

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mService = new Messenger(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mBound = false;
        }
    };

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
        Intent intent;
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.menuExpurgar:
                expurgarDados();
                break;
            case R.id.menuMapa:
                intent = new Intent(this, MapaActivity.class);
                //intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
                break;
            case R.id.menuBackup:
                gerarBackupBD();
                break;
            default:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void expurgarDados() {
        try {
            Logger.log("Expurgo solicitado pelo usuário");

            new Thread(new Runnable() {
                public void run() {
                    Message msg = new Message();
                    try {
                        // Expurgando arquivo de log
                        Logger.expurgarLog();
                        // Expurgando banco de dados
                        Database db = Database.getInstance(activity.getBaseContext());
                        db.expurgarBD();
                        //Retorno para o handler Callback
                        msg.arg1=1; //1 = Sucesso
                        Bundle retorno = new Bundle();
                        retorno.putString("Sucesso","Expurgo realizado com sucesso!");
                        msg.setData(retorno);
                    } catch (Exception e) {
                        Logger.log("MainActivity - run() Erro ao expurgar dados. Detalhes: " + e.toString() + "\n" + e.getStackTrace().toString());
                        //Montando mensagem de retorno de erro
                        msg.arg1=0; //0 = Erro
                        Bundle retorno = new Bundle();
                        retorno.putString("Erro","Erro ao realizar expurgo. Verifique o log.");
                        msg.setData(retorno);
                    } finally {
                        handler.sendMessage(msg);
                    }
                }
            }).start();

        } catch (Exception e) {
            Logger.log("MainActivity - Erro ao expurgar dados. Detalhes: " + e.toString() + "\n" + e.getStackTrace().toString());
        }
    }

    private void gerarBackupBD() {
        Message msg = new Message();
        try {
            // Realizando backup do banco de dados
            Logger.log("Geração de backup solicitada pelo usuário");
            Database db = Database.getInstance(this);
            db.gerarBackupBD();
            //Retorno para o handler Callback
            msg.arg1=1; //1 = Sucesso
            Bundle retorno = new Bundle();
            retorno.putString("Sucesso","Backup gerado com sucesso!");
            msg.setData(retorno);
        } catch (Exception e) {
            Logger.log("MainActivity - Erro ao gerar backup do banco de dados. Detalhes: " + e.toString() + "\n" + e.getStackTrace().toString());
            //Montando mensagem de retorno de erro
            msg.arg1=0; //0 = Erro
            Bundle retorno = new Bundle();
            retorno.putString("Erro","Erro ao gerar backup do banco de dados. Verifique o log.");
            msg.setData(retorno);
        } finally {
            handler.sendMessage(msg);
        }
    }

    final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.arg1==1) {
                // Mensagem de sucesso ao usuário
                Toast.makeText(activity.getBaseContext(), msg.getData().getString("Sucesso"), Toast.LENGTH_LONG).show();
            } else {
                // Mensagem de erro ao usuário
                Toast.makeText(activity.getBaseContext(), msg.getData().getString("Erro"), Toast.LENGTH_LONG).show();
            }
            return false;
        }
    });

    //        textViewX.setText("Posição X: " + x.intValue() + " Float: " + x);
    //        textViewY.setText("Posição Y: " + y.intValue() + " Float: " + y);
    //        textViewZ.setText("Posição Z: " + z.intValue() + " Float: " + z);
}
