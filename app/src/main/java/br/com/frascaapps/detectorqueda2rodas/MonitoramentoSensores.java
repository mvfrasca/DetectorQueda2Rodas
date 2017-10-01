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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

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
    private Context contexto;
    private int mStartId = 0;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper, Context contexto) {
            super(looper);
            contexto = contexto;
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                OuvinteSensor mSensor;
                //Iniciando o sensor de acordo com o tipo de sensor passado como argumento da mensagem
                mSensor = new OuvinteSensor(contexto, msg.arg2);
                String mensagem = "Registrando serviço de monitoramento. ID: " + msg.arg1;
                if (BuildConfig.DEBUG) Logger.log(mensagem);
                try {
                    mSensor.pararLeituraSensor();
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) Logger.log(e);
                    e.printStackTrace();
                }
                mSensor.iniciarLeituraSensor();
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
        contexto = this;

        // Iniciando thread para o sensor Acelerômetro
        iniciarThreadSensor(startId, Sensor.TYPE_ACCELEROMETER);

        // Iniciando thread para sensor Giroscópio
        iniciarThreadSensor(startId, Sensor.TYPE_GYROSCOPE);

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

        PendingIntent contentIntent = PendingIntent.getActivity(MonitoramentoSensores.this, 0, new Intent(MonitoramentoSensores.this,   MonitoramentoSensores.class), 0);
        Util.gerarNotificacao(this, "Monitoramento de Queda em 2 Rodas", "Serviço de monitoramento iniciado.");
        if (BuildConfig.DEBUG) Logger.log("Serviço de monitoramento iniciado - onCreate");

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper, contexto);

    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        String mensagem = "Serviço de monitoramento removido";
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
        String mensagem = "Serviço de monitoramento finalizado - onDestroy";
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

//    private void reRegisterSensor() {
//        String mensagem = "Registrando serviço de monitoramento";
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
