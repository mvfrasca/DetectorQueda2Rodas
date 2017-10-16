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

package br.com.frascaapps.detectorqueda2rodas.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.content.Context;
import java.util.Calendar;

import br.com.frascaapps.detectorqueda2rodas.MainActivity;
import br.com.frascaapps.detectorqueda2rodas.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public abstract class Util {

    private static NotificationManager mNotifyMgr;
    private static int mNotificationId = 1;

    /**
     * @return milliseconds since 1.1.1970 for today 0:00:00 local timezone
     */
    public static long getToday() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /**
     * @return milliseconds since 1.1.1970 for tomorrow 0:00:01 local timezone
     */
    public static long getTomorrow() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 1);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DATE, 1);
        return c.getTimeInMillis();
    }

    public static void gerarNotificacao(Context contexto, String tituloMensagem, String mensagem) {

        Intent resultIntent = new Intent(contexto, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(contexto,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(contexto)
                        .setSmallIcon(R.drawable.moto_queda_notify_icon)
                        .setContentTitle(tituloMensagem)
                        .setContentText(mensagem)
                        .setVibrate(new long[] { 200, 200 })
                        .setContentIntent(resultPendingIntent);
//                        .setOngoing(true);

        mNotifyMgr = (NotificationManager) contexto.getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    public static void cancelarNotificacao(String mensagem) {
        try {
            mNotifyMgr.cancel(mensagem, mNotificationId);
        }
        catch (Exception e) {}
    }
}
