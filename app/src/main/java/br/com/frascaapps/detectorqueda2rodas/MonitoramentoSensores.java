/*
 * Copyright 2013 Thomas Hoffmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.frascaapps.detectorqueda2rodas;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.ActivityCompat;

import br.com.frascaapps.detectorqueda2rodas.util.Logger;
import br.com.frascaapps.detectorqueda2rodas.util.Util;

/**
 * Background service which keeps the step-sensor listener alive to always get
 * the number of steps since boot.
 * <p/>
 * This service won't be needed any more if there is a way to read the
 * step-value without waiting for a sensor event
 */
public class MonitoramentoSensores extends Service {

    private final IBinder mBinder = (IBinder) new LocalBinder();
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private LocationManager locationManager;
    private LocationListener ouvinteLocalizacao;
    private Context mContexto;
    private int mStartId = 0;
    String mensagem = "";

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper, Context contexto) {
            super(looper);
            mContexto = contexto;
        }

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                mensagem = "Registrando serviço de monitoramento. ID: " + msg.arg1;
                if (BuildConfig.DEBUG) Logger.log(mensagem);

                // Se o sensor for o tipo escolhido para definir o GPS
                if (msg.arg2 == Sensor.TYPE_DEVICE_PRIVATE_BASE) {
                    iniciarMonitoramentoLocalizacao();
                } else {
                    OuvinteSensor mSensor;
                    //Iniciando o sensor de acordo com o tipo de sensor passado como argumento da mensagem
                    mSensor = new OuvinteSensor(mContexto, msg.arg2);

                    try {
                        mSensor.pararLeituraSensor();
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG) Logger.log(e);
                        e.printStackTrace();
                    }
                    mSensor.iniciarLeituraSensor();
                }
            } catch (Exception e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }

    public class LocalBinder extends Binder {
        MonitoramentoSensores getService() {
            return MonitoramentoSensores.this;
        }
    }

//    protected void pararLeituraSensores() {
//        //Para a leitura de todos os sensores
//        mAcelerometro.pararLeituraSensor();
//    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        mStartId = startId;
        mContexto = this;

        try {
            Thread.sleep(1000);
        } catch (Exception e)  {

        }

        // Iniciando thread para o sensor Acelerômetro
        iniciarThreadSensor(startId, Sensor.TYPE_ACCELEROMETER);

        // Iniciando thread para sensor Giroscópio
        iniciarThreadSensor(startId, Sensor.TYPE_GYROSCOPE);

        //Iniciando thread para serviço GPS
        iniciarThreadSensor(startId, Sensor.TYPE_DEVICE_PRIVATE_BASE);

        return START_STICKY;
    }

    void iniciarThreadSensor(int startId, int tipoSensor) {
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = mStartId;
        msg.arg2 = tipoSensor;
        mServiceHandler.sendMessage(msg);
    }

    @Override
    public void onCreate() {
        //super.onCreate();

        //PendingIntent contentIntent = PendingIntent.getActivity(MonitoramentoSensores.this, 0, new Intent(MonitoramentoSensores.this,   MonitoramentoSensores.class), 0);
        Util.gerarNotificacao(this, "Monitoramento de Queda em 2 Rodas", "Serviço de monitoramento iniciado.");
        if (BuildConfig.DEBUG) Logger.log("Serviço de monitoramento iniciado - onCreate");

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper, mContexto);

    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        mensagem = "Serviço de monitoramento removido";
        if (BuildConfig.DEBUG) Logger.log(mensagem);
        Util.cancelarNotificacao(mensagem);
        // Restart service in 500 ms
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                .set(AlarmManager.RTC, System.currentTimeMillis() + 500, PendingIntent
                        .getService(this, 3, new Intent(this, MonitoramentoSensores.class), 0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mensagem = "Serviço de monitoramento finalizado - onDestroy";
        Logger.log(mensagem);
        Util.cancelarNotificacao(mensagem);
        try {
//            mAcelerometro.pararLeituraSensor();
            mServiceHandler.removeMessages(mStartId);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Logger.log(e);
            e.printStackTrace();
        }
        stopSelf();
    }

    void iniciarMonitoramentoLocalizacao() {

        try {

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = false;
            // Buscando o status da Rede celular
            //boolean isNetworkEnabled = false;

            if (ActivityCompat.checkSelfPermission(mContexto, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContexto, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                mensagem = "Monitoramento GPS não habilitado por falta de permissão de utilização do GPS";
                Logger.log(mensagem);
                return;
            } else {
                mensagem = "Permissão de utilização do GPS - Ok";
                Logger.log(mensagem);
            }

            //se o provedor de localizacao nao estiver habilitado, teremos uma excecao.
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//            isNetworkEnabled=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled) {
                mensagem = "GPS está desabilitado";
                Logger.log(mensagem);
                // TODO: Implementar na MainActivity método que faça essa checagem e solicite para usuário habilitar GPS
            } else {
                mensagem = "GPS habilitado - Ok";
                Logger.log(mensagem);

                ouvinteLocalizacao = new OuvinteLocalizacao(mContexto);

                mensagem = "Provedor instanciado - Ok";
                Logger.log(mensagem);

                long tempo = 1000; //1 segundo
                float distancia = 0; // 2 metros

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, tempo, distancia, ouvinteLocalizacao);

                mensagem = "Requisição de atualização de localização registrada - Ok";
                Logger.log(mensagem);
            }
        } catch (Exception ex) {
            mensagem = "iniciarMonitoramentoLocalizacao - Erro: " + ex.toString() + "\n." + ex.getStackTrace().toString();
            Logger.log(mensagem);
        }
    }

//    private void reRegisterSensor() {
//        mensagem = "Registrando serviço de monitoramento";
//        if (BuildConfig.DEBUG) Logger.log(mensagem);
//        Util.cancelarNotificacao(mensagem);
//        try {
//            mAcelerometro.pararLeituraSensor();
//        } catch (Exception e) {
//            if (BuildConfig.DEBUG) Logger.log(e);
//            e.printStackTrace();
//        }
//        mAcelerometro.iniciarLeituraSensor();
//    }
}
