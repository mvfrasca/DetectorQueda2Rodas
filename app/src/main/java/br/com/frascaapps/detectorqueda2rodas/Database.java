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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.frascaapps.detectorqueda2rodas.util.FileUtils;
import br.com.frascaapps.detectorqueda2rodas.util.Logger;
import br.com.frascaapps.detectorqueda2rodas.util.Util;

public class Database extends SQLiteOpenHelper {

    private final static String APP = "detectorqueda2rodas";
    private final static String DB_NAME = "DB_Log";
    private final static int DB_VERSION = 1;
    private final static String TABELA_LOG_ACELEROMETRO = "tb_log_acelerometro";
    private final static String TABELA_LOG = "tb_log";

    private static Database instance;
    private static final AtomicInteger openCounter = new AtomicInteger();

    private Database(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized Database getInstance(final Context c) {
        if (instance == null) {
            instance = new Database(c.getApplicationContext());
        }
        openCounter.incrementAndGet();
        return instance;
    }

    @Override
    public void close() {
        if (openCounter.decrementAndGet() == 0) {
            super.close();
        }
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABELA_LOG + " (data_hora_log INTEGER, mensagem VARCHAR(255) )");
        db.execSQL("CREATE TABLE " + TABELA_LOG_ACELEROMETRO + " (data_hora_log INTEGER, x FLOAT, y FLOAT, z FLOAT, info_adicional VARCHAR(80))");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        /*if (oldVersion == 1) {
            // drop PRIMARY KEY constraint
            db.execSQL("CREATE TABLE " + DB_NAME + "2 (date INTEGER, steps INTEGER)");
            db.execSQL("INSERT INTO " + DB_NAME + "2 (date, steps) SELECT date, steps FROM " +
                    DB_NAME);
            db.execSQL("DROP TABLE " + DB_NAME);
            db.execSQL("ALTER TABLE " + DB_NAME + "2 RENAME TO " + DB_NAME + "");
        }*/
    }

    /**
     * Query the 'steps' table. Remember to close the cursor!
     *
     * @param columns       the colums
     * @param selection     the selection
     * @param selectionArgs the selction arguments
     * @param groupBy       the group by statement
     * @param having        the having statement
     * @param orderBy       the order by statement
     * @return the cursor
     */
    public Cursor query(final String[] columns, final String selection,
                        final String[] selectionArgs, final String groupBy, final String having,
                        final String orderBy, final String limit) {
        return getReadableDatabase()
                .query(TABELA_LOG_ACELEROMETRO, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * Inserts a new entry in the database, if there is no entry for the given
     * date yet. Steps should be the current number of steps and it's negative
     * value will be used as offset for the new date. Also adds 'steps' steps to
     * the previous day, if there is an entry for that date.
     * <p/>
     * This method does nothing if there is already an entry for 'date' - use
     * {@link //#updateSteps} in this case.
     * <p/>
     * To restore data from a backup, use {@link //#insertDayFromBackup}
     *
     * //@param date  the date in ms since 1970
     * //@param steps the current step value to be used as negative offset for the
     *              new day; must be >= 0
     */
    public void incluirLeituraSensor(Date data_hora_log, float x, float y, float z, String info_adicional) {
        getWritableDatabase().beginTransaction();
        try {
            // add today
            ContentValues values = new ContentValues();
            SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            values.put("data_hora_log", dtFormat.format(data_hora_log));
            values.put("x", x);
            values.put("y", y);
            values.put("z", z);
            values.put("info_adicional", info_adicional);

            getWritableDatabase().insert(TABELA_LOG_ACELEROMETRO, null, values);

            Logger.log("Log acelerômetro: " + data_hora_log.toString() + " -  X: " + x + "; Y: " + y + "; Z: " + z);
            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
    }

    public void gerarBackupBD() {
        try {
            close();
            File newDb = new File(Environment.getExternalStorageDirectory().toString() + "/" + APP + "/" + DB_NAME + ".db");
            File oldDb = new File(this.getReadableDatabase().getPath());
            //if (newDb.exists()) {
                FileUtils.copyFile(new FileInputStream(oldDb), new FileOutputStream(newDb));
                // Access the copied database so SQLiteHelper will cache it and mark
                // it as created.
                getWritableDatabase().close();
            //}
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /*public void gerarBackupBD() {
        File f = new File("/data/data/br.com.frascaapps.detectorqueda2rodas/databases/" + DB_NAME);
//        FileInputStream fis = null;
//        FileOutputStream fos = null;
        FileReader fis = null;
        FileWriter writer = null;
        FileUtils
        try
        {
//            fis=new FileInputStream(f);
//            fos=new FileOutputStream("/sdcard/detectorqueda2rodas/" + DB_NAME + ".db");
//            while(true)
//            {
//                int i=fis.read();
//                if(i!=-1)
//                {fos.write(i);}
//                else
//                {break;}
//            }
//            fos.flush();
//            Toast.makeText(this, "DB dump OK", Toast.LENGTH_LONG).show();

            fis = new FileReader(f);
            BufferedReader bufferedReader = new BufferedReader(fis);
            StringBuilder buffer = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            bufferedReader.close();
            File arquivoDestino = new File(Environment.getExternalStorageDirectory().toString() + "/" + APP + "/" + DB_NAME + ".db");
            writer = new FileWriter(arquivoDestino);
            writer.write(buffer.toString());
            writer.flush();
        }
        catch(Exception e)
        {
            e.printStackTrace();
//            Toast.makeText(this, "DB dump ERROR", Toast.LENGTH_LONG).show();
        }
        finally
        {
            try
            {
//                fos.close();
//                fis.close();
                writer.close();
                fis.close();
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }*/

}
