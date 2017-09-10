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
import android.os.IBinder;
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

    private OuvinteSensor mAcelerometro;
    private final IBinder mBinder = (IBinder) new LocalBinder();

    public class LocalBinder extends Binder {
        MonitoramentoSensores getService() {
            return MonitoramentoSensores.this;
        }
    }

    protected void pararLeituraSensores() {
        mAcelerometro.pararLeituraSensor();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        mAcelerometro.iniciarLeituraSensor();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        //super.onCreate();
        PendingIntent contentIntent = PendingIntent.getActivity(MonitoramentoSensores.this, 0, new Intent(MonitoramentoSensores.this,   MonitoramentoSensores.class), 0);
        Util.gerarNotificacao(this, "Monitoramento de Queda em 2 Rodas", "Serviço de monitoramento iniciado.");
        if (BuildConfig.DEBUG) Logger.log("Serviço de monitoramento iniciado - onCreate");
        //Iniciando o sensor Acelerômetro
        mAcelerometro = new OuvinteSensor(this, Sensor.TYPE_ACCELEROMETER);
        reRegisterSensor();
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
            mAcelerometro.pararLeituraSensor();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Logger.log(e);
            e.printStackTrace();
        }
        stopSelf();
    }

    private void reRegisterSensor() {
        String mensagem = "Registrando serviço de monitoramento";
        if (BuildConfig.DEBUG) Logger.log(mensagem);
        Util.cancelarNotificacao(mensagem);
        try {
            mAcelerometro.pararLeituraSensor();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Logger.log(e);
            e.printStackTrace();
        }
        mAcelerometro.iniciarLeituraSensor();
    }
}
