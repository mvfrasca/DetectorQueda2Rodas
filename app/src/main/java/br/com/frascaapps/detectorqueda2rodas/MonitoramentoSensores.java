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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

//import br.com.frascaapps.detectorqueda2rodas.MainActivity;
import br.com.frascaapps.detectorqueda2rodas.util.Logger;
import br.com.frascaapps.detectorqueda2rodas.util.Util;
//import de.j4velin.pedometer.widget.WidgetUpdateService;

/**
 * Background service which keeps the step-sensor listener alive to always get
 * the number of steps since boot.
 * <p/>
 * This service won't be needed any more if there is a way to read the
 * step-value without waiting for a sensor event
 */
public class MonitoramentoSensores extends Service {

    private final static int NOTIFICATION_ID = 1;
    private final static long MICROSECONDS_IN_ONE_MINUTE = 60000000;
    private final static long SAVE_OFFSET_TIME = AlarmManager.INTERVAL_HOUR;
    private final static int SAVE_OFFSET_STEPS = 500;

    public final static String ACTION_PAUSE = "pause";

    private static int steps;
    private static int lastSaveSteps;
    private static long lastSaveTime;

    public final static String ACTION_UPDATE_NOTIFICATION = "updateNotificationState";

    private OuvinteSensor mAcelerometro;

    protected void pararLeituraSensores() {
        mAcelerometro.pararLeituraSensor();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        mAcelerometro.iniciarLeituraSensor();

//        if (intent != null && ACTION_PAUSE.equals(intent.getStringExtra("action"))) {
//            if (BuildConfig.DEBUG)
//                Logger.log("onStartCommand action: " + intent.getStringExtra("action"));
//            if (steps == 0) {
//                Database db = Database.getInstance(this);
//                steps = db.getCurrentSteps();
//                db.close();
//            }
//            SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
//            if (prefs.contains("pauseCount")) { // resume counting
//                int difference = steps -
//                        prefs.getInt("pauseCount", steps); // number of steps taken during the pause
//                Database db = Database.getInstance(this);
//                db.addToLastEntry(-difference);
//                db.close();
//                prefs.edit().remove("pauseCount").commit();
//                updateNotificationState();
//            } else { // pause counting
//                // cancel restart
//                ((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE))
//                        .cancel(PendingIntent.getService(getApplicationContext(), 2,
//                                new Intent(this, MonitoramentoSensores.class),
//                                PendingIntent.FLAG_UPDATE_CURRENT));
//                prefs.edit().putInt("pauseCount", steps).commit();
//                updateNotificationState();
//                stopSelf();
//                return START_NOT_STICKY;
//            }
//        }
//
//        if (intent != null && intent.getBooleanExtra(ACTION_UPDATE_NOTIFICATION, false)) {
//            updateNotificationState();
//        } else {
//            updateIfNecessary();
//        }
//
//        // restart service every hour to save the current step count
//        ((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE))
//                .set(AlarmManager.RTC, Math.min(Util.getTomorrow(),
//                        System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR), PendingIntent
//                        .getService(getApplicationContext(), 2,
//                                new Intent(this, MonitoramentoSensores.class),
//                                PendingIntent.FLAG_UPDATE_CURRENT));

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Logger.log("MonitoramentoSensores onCreate");
        reRegisterSensor();
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (BuildConfig.DEBUG) Logger.log("sensor service task removed");
        // Restart service in 500 ms
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                .set(AlarmManager.RTC, System.currentTimeMillis() + 500, PendingIntent
                        .getService(this, 3, new Intent(this, MonitoramentoSensores.class), 0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.log("MonitoramentoSensores onDestroy");
        try {
            mAcelerometro.pararLeituraSensor();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Logger.log(e);
            e.printStackTrace();
        }
    }

    private void reRegisterSensor() {
        if (BuildConfig.DEBUG) Logger.log("re-register sensor listener");
        try {
            mAcelerometro.pararLeituraSensor();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Logger.log(e);
            e.printStackTrace();
        }

//        if (BuildConfig.DEBUG) {
//            Logger.log("step sensors: " + sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size());
//            if (sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size() < 1) return; // emulator
//            Logger.log("default: " + sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER).getName());
//        }

        // enable batching with delay of max 5 min
//        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
//                SensorManager.SENSOR_DELAY_NORMAL, (int) (5 * MICROSECONDS_IN_ONE_MINUTE));

        mAcelerometro.iniciarLeituraSensor();
    }
}
