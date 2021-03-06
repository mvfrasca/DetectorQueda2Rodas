package br.com.frascaapps.detectorqueda2rodas;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;

import java.util.Date;

import br.com.frascaapps.detectorqueda2rodas.util.Logger;
import br.com.frascaapps.detectorqueda2rodas.util.Util;

/**
 * Created by mvfra on 03/09/2017.
 */

public class OuvinteSensor implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Context contexto;

    public OuvinteSensor(Context context, int tipoSensor ) {
        contexto = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(tipoSensor);
        Logger.log("Sensor [ " + mSensor.getName() + " ]  iniciado. Detalhes do sensor: " + mSensor.toString());
    }

    protected void iniciarLeituraSensor() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void pararLeituraSensor() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Logger.log(sensor.getName() + " Acurácia do sensor alterada para: " + accuracy);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        try {
            Float x = event.values[0];
            Float y = event.values[1];
            Float z = event.values[2];

            if (event.values[0] > Integer.MAX_VALUE) {
                if (BuildConfig.DEBUG) Logger.log("Valores lidos provavelmente irreais (leitura desconsiderada): " + event.values[0]);
                return;
            } else {
                atualizarLog(x,y,z);
            }
        } catch (Exception e){
            Logger.log("Erro ao realizar gravação do sensor [ " + this.mSensor + " ]. Detalhes: " + e.toString() + "\n" + e.getStackTrace().toString());
        }
    }

    public void atualizarLog(float x, float y, float z) {

        try {
            Database db = Database.getInstance(contexto);
            Date data_hora_log = new Date();
            String info_adicional = "";
            /*
                Os valores ocilam de -10 a 10.
                Quanto maior o valor de X mais ele ta caindo para a esquerda - Positivo Esqueda
                Quanto menor o valor de X mais ele ta caindo para a direita  - Negativo Direita
                Se o valor de  X for 0 então o celular ta em pé - Nem Direita Nem Esquerda
                Se o valor de Y for 0 então o cel ta "deitado"
                Se o valor de Y for negativo então ta de cabeça pra baixo, então quanto menor y mais ele ta inclinando pra ir pra baixo
                Se o valor de Z for 0 então o dispositivo esta reto na horizontal.
                Quanto maior o valor de Z Mais ele esta inclinado para frente
                Quanto menor o valor de Z Mais ele esta inclinado para traz.
            */
            /*if (y < 0) { // O dispositivo esta de cabeça pra baixo
                if (x > 0)
                    info_adicional = "Virando para ESQUERDA ficando INVERTIDO";
                if (x < 0)
                    info_adicional = "Virando para DIREITA ficando INVERTIDO";
            } else {
                if (x > 0)
                    info_adicional = "Virando para ESQUERDA ";
                if (x < 0)
                    info_adicional = "Virando para DIREITA ";
            }*/
            data_hora_log.setTime(System.currentTimeMillis());
            db.incluirLeituraSensor(mSensor.getType(), data_hora_log,x,y,z);
        } catch (Exception e){
            Logger.log("Erro ao atualizar log do sensor [ " + this.mSensor + " ]. Detalhes: " + e.toString() + "\n" + e.getStackTrace().toString());
        }
    }
}
