/*********************************************************************************************************
 * Archvio que contiene el Servicio recpetor de mensajes que envia el Servidor con los valores sensados
 * por el potenciometro
 **********************************************************************************************************/

package com.example.esteban.android_http.ClienteHttp;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Esteban on 28/02/2017.
 */


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

//Clase del Servicio ecpetor de mensajes que envia el Servidor con los valores sensados
//por el potenciometro
public class ServicesHttp_GET extends IntentService {


    //Variable utilizada para almacenar la descripcion de la excepciones que se generen durante la ejecucion
    //del thread
    private Exception mException=null;

    private HttpURLConnection httpConnection;

    private URL mUrl;

    //Constructor de la clase
    public ServicesHttp_GET() {
        super("ServicesHttp_GET");
    }

    @Override

    //Metodo que se invoca cuando se ejecuta  la instrcuccion stratService por en la activity principal
    protected void onHandleIntent(Intent intent) {
        try {

            //Se obtiene la uri que envia la activityPrincipal atraves de un intent

            String uri = intent.getExtras().getString("uri");

            ejecutarGet(uri);

        }
        catch(Exception e)
        {
            Log.e("****ERRROR",e.toString());
        }

    }

    private StringBuilder convertInputStreamToString(InputStreamReader inputStream) throws IOException {
        BufferedReader br = new BufferedReader(inputStream);
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            result.append(line + "\n");
        }
        br.close();
        return result;
    }

    //Metodo que le envia una peticion GET al servidor solicitandole los valores sensados por el potenciometro,
    //los cuales son retornados al metodo llamador una vez recibida la respuesta emitida por el Servidor
    private String GET(String uri)
    {
        try
        {
            String result = null;


            //Se alamacena la URI del request del servicio web
            this.mUrl = new URL(uri);

            //Se arma el request con el formato correcto
            httpConnection = (HttpURLConnection) mUrl.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Content-length", "0");
            httpConnection.setUseCaches(false);
            httpConnection.setAllowUserInteraction(false);
            httpConnection.setConnectTimeout(100000);
            httpConnection.setReadTimeout(100000);

            //se envia el request al Servidor
            httpConnection.connect();

            //Se obtiene la espuesta que envio el Servidor con los valores sensados
            int responseCode = httpConnection.getResponseCode();

            //se analiza si la respuesta fue correcta
            if (responseCode == HttpURLConnection.HTTP_OK)
                result = convertInputStreamToString(new InputStreamReader(httpConnection.getInputStream())).toString();
            else
                result = "NO_OK";

            httpConnection.disconnect();
            return result;
        }
        catch (Exception e)
        {
            mException=e;
            return null;
        }
    }


    //Metod que ejecuta un request Get al servidor. El resultado del request, es enviado
    //a la activty principal con sendbraodcast, para que los reciba la clase ReceptorOperacion de dicha activty
    protected void ejecutarGet(String uri) {

        try {
            String result= GET(uri);

            //Si se genero una excepcion durante la ejecucion del thread
            if (mException != null) {
                //Toast.makeText(this.contexto.getApplicationContext(),"Error en GET:\n"+mException.toString(),Toast.LENGTH_LONG).show();
                Log.e("Error","Error en GET:\n" + mException.toString());
                return;
            }
            //Si se recibio un mesaje NO OK como respuesta a la peticion GET
            if (result == "NO_OK") {
                Log.e("Error en GET"," se recibio response NO_OK");
                return;
            }

            //Si se ejecuto el Request correctamente,se llama al metodo de la activity Principal encargado
            // de actualizar el valor de texto mostrado en el TextView y actualizar el grafico del Velocimetro

            JSONObject json = new JSONObject(result);

            Float valor = Float.parseFloat(json.getString("valor"));
            String str = "Sensor: " + json.getString("sensor")+ "\n Valor: " + valor;


            // send Intent to BroadcastReceiver in MainActivity.class to know the service  finished its task once

            Intent i = new Intent("com.example.intentservice.intent.action.RESPUESTA_OPERACION" );
            i.putExtra("valorSensado", valor);
            i.putExtra("mensajeSensor",str);
            //Se envian los valores sensados por el potenciometro, al bradcast reciever de la activity principal
            sendBroadcast(i);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }


    }

}



