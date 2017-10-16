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
import android.hardware.Sensor;
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
    //private final static String TB_TIPO_MOVIMENTO = "tb_tipo_movimento";
    private final static String TB_LOG_ACELEROMETRO = "tb_log_acelerometro";
    private final static String TB_LOG_ACELEROMETRO_LINEAR = "tb_log_acelerometro_linear";
    private final static String TB_LOG_GIROSCOPIO = "tb_log_giroscopio";
    private final static String TB_LOG_GRAVIDADE = "tb_log_gravidade";
    private final static String TB_LOG_LOCALIZACAO = "tb_log_localizacao";
//    private final static String TB_LOG = "tb_log";

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
//        db.execSQL("CREATE TABLE " + TB_LOG + " (data_hora_log INTEGER, mensagem VARCHAR(255) )");
//        db.execSQL("CREATE TABLE " + TB_TIPO_MOVIMENTO + " (id_movimento INTEGER PRIMARY KEY, descricao_movimento VARCHAR(80))");
        db.execSQL("CREATE TABLE " + TB_LOG_ACELEROMETRO + " (data_hora_log INTEGER, x FLOAT, y FLOAT, z FLOAT)");
        db.execSQL("CREATE TABLE " + TB_LOG_ACELEROMETRO_LINEAR + " (data_hora_log INTEGER, x FLOAT, y FLOAT, z FLOAT)");
        db.execSQL("CREATE TABLE " + TB_LOG_GIROSCOPIO + " (data_hora_log INTEGER, x FLOAT, y FLOAT, z FLOAT)");
        db.execSQL("CREATE TABLE " + TB_LOG_GRAVIDADE + " (data_hora_log INTEGER, x FLOAT, y FLOAT, z FLOAT)");
        db.execSQL("CREATE TABLE " + TB_LOG_LOCALIZACAO + " (data_hora_log INTEGER, latitude DOUBLE, longitude DOUBLE, altitude DOUBLE, velocidade FLOAT, acuracia FLOAT, provedor VARCHAR(255))");
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
     * @param table         the table
     * @param columns       the colums
     * @param selection     the selection
     * @param selectionArgs the selction arguments
     * @param groupBy       the group by statement
     * @param having        the having statement
     * @param orderBy       the order by statement
     * @return the cursor
     */
    public Cursor query(final String table, final String[] columns, final String selection,
                        final String[] selectionArgs, final String groupBy, final String having,
                        final String orderBy, final String limit) {
        return getReadableDatabase()
                .query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
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
    public void incluirLeituraSensor(int tipoSensor, Date data_hora_log, float x, float y, float z) {
        String tabelaSensor = "";
        getWritableDatabase().beginTransaction();
        try {
            // Definição da tabela de acordo com o tipo de sensor
            switch (tipoSensor) {
                case Sensor.TYPE_ACCELEROMETER:
                    tabelaSensor = TB_LOG_ACELEROMETRO;
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    tabelaSensor = TB_LOG_ACELEROMETRO_LINEAR;
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    tabelaSensor = TB_LOG_GIROSCOPIO;
                    break;
                case Sensor.TYPE_GRAVITY:
                    tabelaSensor = TB_LOG_GRAVIDADE;
                    break;
                default:
                    Logger.log("Tipo de Sensor [ " + tipoSensor + " ] não esperado.");
                    //TODO: disparar exceção
            }

            // add today
            ContentValues values = new ContentValues();
            SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

            values.put("data_hora_log", dtFormat.format(data_hora_log));
            values.put("x", x);
            values.put("y", y);
            values.put("z", z);

            getWritableDatabase().insert(tabelaSensor, null, values);

            getWritableDatabase().setTransactionSuccessful();
//            Logger.log("Log Acelerômetro: X: " + x + "; Y: " + y + "; Z: " + z + "; " + info_adicional );
        }
        catch (Exception e) {
            Logger.log("Erro ao gravar leitura sensor em [ " + tabelaSensor + " ]: " + e.toString() + "\n" + e.getStackTrace().toString());
        }
        finally {
            getWritableDatabase().endTransaction();
        }
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
    public void incluirLocalizacao(Date data_hora_log, Double latitude, Double longitude, Double altitude, Float velocidade, Float acuracia, String provedor) {

        getWritableDatabase().beginTransaction();
        try {

            // add today
            ContentValues values = new ContentValues();
            SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

            values.put("data_hora_log", dtFormat.format(data_hora_log));
            values.put("latitude", latitude);
            values.put("longitude", longitude);
            values.put("altitude", altitude);
            values.put("velocidade", velocidade);
            values.put("acuracia", acuracia);
            values.put("provedor", provedor);

            getWritableDatabase().insert(TB_LOG_LOCALIZACAO, null, values);

            getWritableDatabase().setTransactionSuccessful();
        }
        catch (Exception e) {
            Logger.log("Erro ao gravar localização: " + e.toString() + ". \n" + e.getStackTrace().toString());
        }
        finally {
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
            Logger.log("Backup do banco gerado em " + newDb.getPath() + "; tamanho: " + newDb.length() / 1000 + " Kb");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Logger.log("Erro ao gravar backup: " + e.toString() + ". \n" + e.getStackTrace().toString());
        }
    }

    public void expurgarBD() {

        String condicao = "data_hora_log < date('now','-1 day')";
        try {
            getWritableDatabase().delete(TB_LOG_ACELEROMETRO, condicao, null);
            getWritableDatabase().delete(TB_LOG_ACELEROMETRO_LINEAR, condicao, null);
            getWritableDatabase().delete(TB_LOG_GIROSCOPIO, condicao, null);
            getWritableDatabase().delete(TB_LOG_GRAVIDADE, condicao, null);
            getWritableDatabase().delete(TB_LOG_LOCALIZACAO, condicao, null);
            Logger.log("Expurgo do banco de dados realizado com sucesso");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Logger.log("Erro ao realiza expurgo BD: " + e.toString() + ". \n" + e.getStackTrace().toString());
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
