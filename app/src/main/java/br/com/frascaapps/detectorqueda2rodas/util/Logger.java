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

import android.database.Cursor;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.FileHandler;

public abstract class Logger {

    private static FileWriter fw;
    private static final Date date = new Date();
    private final static String APP = "detectorqueda2rodas";
    private final static String PATH_COMPLETO_ARQUIVO_LOG = Environment.getExternalStorageDirectory().toString() + "/" + APP + "/" + APP + ".log";

    public static void log(Throwable ex) {
        log(ex.getMessage());
        for (StackTraceElement ste : ex.getStackTrace()) {
            log(ste.toString());
        }
    }

    public static void log(final Cursor c) {
         c.moveToFirst();
        String title = "";
        for (int i = 0; i < c.getColumnCount(); i++)
            title += c.getColumnName(i) + "\t| ";
        log(title);
        while (!c.isAfterLast()) {
            title = "";
            for (int i = 0; i < c.getColumnCount(); i++)
                title += c.getString(i) + "\t| ";
            log(title);
            c.moveToNext();
        }
    }

    @SuppressWarnings("deprecation")
    public static void log(String msg) {
        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        android.util.Log.d(APP, msg);
        try {
            if (fw == null) {
                fw = new FileWriter(new File(PATH_COMPLETO_ARQUIVO_LOG), true);
            }
            date.setTime(System.currentTimeMillis());

            fw.write(dtFormat.format(date) + " - " + msg + "\n");
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // }
    }

    protected void finalize() throws Throwable {
        try {
            if (fw != null) fw.close();
        } finally {
            super.finalize();
        }
    }

    public static void expurgarLog() {
        String pathCompletoArquivoLogTmp = PATH_COMPLETO_ARQUIVO_LOG + ".tmp";
        File arquivoLog = new File(PATH_COMPLETO_ARQUIVO_LOG);
        File arquivoLogTmp = new File(pathCompletoArquivoLogTmp);

        try {

            // Se o arquivo de log ainda não existir finaliza o método sem fazer nada
            if (arquivoLog.exists() == false) {
                return;
            }
            // Se o arquivo de temporário existir o exclui
            if (arquivoLogTmp.exists()) {
                arquivoLogTmp.delete();
            }

            // Identifica a quantidade de linhas do arquivo de log
            LineNumberReader linhaLeitura = new LineNumberReader(new FileReader(arquivoLog));
            linhaLeitura.skip(arquivoLog.length());
            int qtdLinhas = linhaLeitura.getLineNumber();

            // Se o arquivo tiver menos de 100 linhas finaliza o método: expurgo não é necessário
            if (qtdLinhas < 100) {
                return;
            }

            FileWriter fwtmp;
            fwtmp = new FileWriter(new File(pathCompletoArquivoLogTmp), true);

            SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            date.setTime(System.currentTimeMillis());
            fwtmp.write(dtFormat.format(date) + " - Logs anteriores apagados pelo expurgo \n");

            // Reinstancia para recarregar desde a primeira linha
            linhaLeitura = new LineNumberReader(new FileReader(arquivoLog));
            linhaLeitura.setLineNumber(qtdLinhas - 100);
            String linha = linhaLeitura.readLine() ; // lê a primeira linha

            while (linha != null) {
                fwtmp.write(linha + "\n");
                linha = linhaLeitura.readLine(); // lê da próxima linhamarkedLastWasCR = false
            }
            date.setTime(System.currentTimeMillis());
            fwtmp.write(dtFormat.format(date) + " - Expurgo efetuado com sucesso \n");

            fwtmp.flush();
//            fwtmp.close();
//            fw.close();

            FileUtils.copyFile(new FileInputStream(arquivoLogTmp), new FileOutputStream(arquivoLog));
            arquivoLogTmp.delete();
//             try {
//                FileWriter fwtmp;
//                fwtmp = new FileWriter(new File(pathCompletoArquivoLogTmp), true);
//
//                FileReader fr = new FileReader(arquivoLog);
//                BufferedReader br = new BufferedReader(fr);
//
//                br.skip(qtdLinhas - 100)
//                String linha = br.readLine(); // lê a primeira linha
//                // a variável "linha" recebe o valor "null" quando o processo
//                // de repetição atingir o final do arquivo texto
//                while (linha != null) {
//                    System.out.printf("%s\n", linha);
//
//                    linha = lerArq.readLine(); // lê da segunda até a última linha
//                }
//
//                arq.close();
//
//
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            } finally {
//                try { if (br != null) br.close(); } catch (IOException ex) {}
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        // }
    }

}
