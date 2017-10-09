/*********************************************************************************************************
 * Archivo que continen la clase que genera un hilo, mediante la clase Asynctask, el cual es el encargado
 * de emitir peticiones GET al Servidor y recibir su respuesta
 **********************************************************************************************************/

package com.example.esteban.android_http.ClienteHttp;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//Clase que genera un hiloencargado de emitir peticiones GET al Servidor y recibir su respuesta
public class ClienteHttp_GET extends AsyncTask<String, String, String>
{
    //objeto interfaz que contendra los callbacks que son utilizados para poder mostrar los resultados
    //de la ejecucion del hilo en la activity principal
    private InterfazAsyntask caller;

    //Variable utilizada para almacenar la descripcion de la excepciones que se generen durante la ejecucion
    //del thread
    private Exception mException=null;

    private HttpURLConnection httpConnection;
    private URL mUrl;

    //Constructor de la clase
    public ClienteHttp_GET(Activity a)
    {
        //se alamcena el contexto
        this.caller=(InterfazAsyntask)a;
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


    @Override
    //metodo ejecutado por medio de execute. Recibe un array de parametros
    //params[0]:direccion uri correspondiente al servicio GET del servidor para solicitar los valores
    //         sensados por el potenciometro
    protected String doInBackground(String ...params)
    {

        return GET(params[0]);
    }

    //Al finalizar la ejecucion del metodo doInBackground, se analiza el reultado devuelto
    protected void onPostExecute(String result)
    {

        try {
            super.onPostExecute(result);
            //Si se genero una excepcion durante la ejecucion del thread
            if (mException != null) {
                //Toast.makeText(this.contexto.getApplicationContext(),"Error en GET:\n"+mException.toString(),Toast.LENGTH_LONG).show();
                caller.mostrarToastMake("Error en GET:\n" + mException.toString());
                return;
            }
            //Si se recibio un mesaje NO OK como respuesta a la peticion GET
            if (result == "NO_OK") {
                caller.mostrarToastMake("Error en GET, se recibio response NO_OK");
                return;
            }

            //Si se ejecuto el Request correctamente,se llama al metodo de la activity Principal encargado
            // de actualizar el valor de texto mostrado en el TextView y actualizar el grafico del Velocimetro

            JSONObject json = new JSONObject(result);

            Float valor = Float.parseFloat(json.getString("valor"));
            String str = "Sensor: " + json.getString("sensor")+ "\n Valor: " + valor;

            caller.actualizarVelocimetro(valor);
            caller.mostrarTextViewPotenciometro(str);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }



    }


}


