package br.com.frascaapps.detectorqueda2rodas;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;

import java.util.Date;

import br.com.frascaapps.detectorqueda2rodas.util.Logger;

/**
 * Created by mvfra on 10/10/2017.
 */

public class OuvinteLocalizacao implements LocationListener {

    private Context contexto;
    private String mensagem = "";

    public OuvinteLocalizacao(Context context) {
        contexto = context;
        Logger.log("Provedor de Localização iniciado.");
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            Logger.log("onLocationChanged - iniciado");
            Database db = Database.getInstance(contexto);
            Date data_hora_log = new Date();

            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            Double altitude = location.getAltitude();
            Float velocidade = location.getSpeed();
            Float acuracia = location.getAccuracy();

            data_hora_log.setTime(System.currentTimeMillis());
            db.incluirLocalizacao(data_hora_log, latitude, longitude, altitude, velocidade, acuracia);
        } catch (Exception ex) {
            mensagem = "iniciarMonitoramentoLocalizacao - Erro: " + ex.toString() + "\n." + ex.getStackTrace().toString();
            Logger.log(mensagem);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        String descricaoStatus = "";

        switch (status) {
            case LocationProvider.AVAILABLE:
                descricaoStatus = "Disponível";
                break;
            case LocationProvider.OUT_OF_SERVICE:
                descricaoStatus = "Fora de serviço";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                descricaoStatus = "Temporariamente indisponível";
                break;
            default:
                descricaoStatus = "Status desconhecido";
        }
        Logger.log("Status do provedor de localização [ " + provider.toString() + " ] alterado para [ " + status + " - " + descricaoStatus + " ]");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Logger.log("Provedor de localização [ " + provider.toString() + " ] habilitado");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Logger.log("Provedor de localização [ " + provider.toString() + " ] desabilitado");
    }
}
