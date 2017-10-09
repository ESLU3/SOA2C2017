/*********************************************************************************************************
 * Archivo que continen la clase que genera un hilo, mediante la clase Asynctask, el cual es el encargado
 * de emitir peticiones POST al Servidor y recibir su respuesta
 **********************************************************************************************************/

package com.example.esteban.android_http.ClienteHttp;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//Clase que genera un hiloencargado de emitir peticiones POST al Servidor y recibir su respuesta
public class AsynctaskHttp_POST extends AsyncTask<String , Void ,String>
{
    //objeto interfaz que contendra los callbacks que son utilizados para poder mostrar los resultados
    //de la ejecucion del hilo en la activity principal
    private InterfazAsyntask caller;

    //Variable utilizada para almacenar la descripcion de la excepciones que se generen durante la ejecucion
    //del thread
    private Exception mException=null;

    //Constructor de la clase
    public AsynctaskHttp_POST(Activity a)
    {
        //se alamcena el contexto
        this.caller=(InterfazAsyntask)a;


    }

    //Metodo que le envia una peticion POST al servidor solicitandole modificar el estado de un led
    //con un valor determinado
    private String POST (String uri, String estadoLed)
    {
        HttpURLConnection urlConnection = null;

        try
        {

            //Se alamacena la URI del request del servicio web
            URL mUrl = new URL(uri);

            //Se arma el request con el formato correcto
            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");

            //Se crea un paquete JSON que indica el estado(encendido o apagado) del led que se desea
            //modificar. Este paquete JSON se escribe en el campo body del mensaje POST
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());

            JSONObject obj = new JSONObject();
            obj.put("estado" , estadoLed);

            wr.writeBytes(obj.toString());
            Log.i("JSON Input", obj.toString());

            wr.flush();
            wr.close();

            //se envia el request al Servidor
            urlConnection.connect();

            //Se obtiene la respuesta que envio el Servidor ante el request
            int responseCode = urlConnection.getResponseCode();

            urlConnection.disconnect();

            //se analiza si la respuesta fue correcta
            if(responseCode != HttpURLConnection.HTTP_OK)
            {
                return "NO_OK";
            }



        } catch (Exception e)
        {
            mException=e;
        }
        return null;
    }

    @Override
    //metodo ejecutado por medio de execute. Recibe un array de parametros
    //params[0]:direccion uri correspondiente al servicio POST del servidor para modificar el estado del led
    //params[1]: valor del estado que se desea que tenga el led. (1=ENCENDIDO, 0=APAGADO);
    protected String doInBackground(String... params)
    {

        return POST(params[0],params[1]);
    }

    @Override
    //Al finalizar la ejecucion del metodo doInBackground, se analiza el reultado devuelto
    protected void onPostExecute(String result) {

        super.onPostExecute(result);
        //Si se genero una excepcion durante la ejecucion del thread
        if (mException != null) {
            //Toast.makeText(this.contexto.getApplicationContext(),"Error en GET:\n"+mException.toString(),Toast.LENGTH_LONG).show();
            caller.mostrarToastMake("Error en POST:\n" + mException.toString());
            return;
        }
        //Si se recibio un mesaje NO OK como respuesta a la peticion GET
        if (result == "NO_OK") {
            caller.mostrarToastMake("Error en POST, se recibio response NO_OK");
            return;
        }

    }
}
