/*********************************************************************************************************
 * Archivo que continen la clase que genera un hilo, mediante la clase Asynctask, el cual es el encargado
 * de enviar mensajes al Servidor para modificar el estado del led
 **********************************************************************************************************/

package com.example.esteban.android_code.ClienteUDP;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


//Clase del hilo encargado de enviar los mensajes al Servidor
public class ClienteUDP_Emisor extends AsyncTask<String, Void, String>
{

    private InterfazAsyntask caller;
    public InetAddress ipServidor;
    public int puertoServidor;
    private DatagramSocket dtSocket;

    //Variable utilizada para almacenar la descripcion de la excepciones que se generen durante la ejecucion
    //del thread
    private Exception mException=null;

    //Constructor de la clase
    public ClienteUDP_Emisor(Activity a,DatagramSocket dtSocket, String ipServidor, int puertoServidor) {
        try
        {
            this.caller= (InterfazAsyntask) a;

            //Se convierte el String que contiene la direccion IP del Servidor en el formato InetAddress
            this.ipServidor= InetAddress.getByName(ipServidor);
            this.puertoServidor=puertoServidor;
            this.dtSocket=dtSocket;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            caller.mostrarToastMake("Error en constructor:"+e.toString());
        }
    }

    @Override
    //Este metodo envia los mensajes al Servidor.
    //metodo ejecutado por medio de execute. Recibe un array de parametros.
    //params[0]: valor del estado que se desea que tenga el led. (1=ENCENDIDO, 2=APAGADO);
    protected String doInBackground(String... params)
    {
        try
        {
            DatagramPacket paqueteEnviar;

            //array de bytes utilizado para alamacenar temporalmente los msj a enviar al Servidor
            byte[] datosAEnviar=params[0].getBytes();

            //Se genera un paquete de datos (datagrama) para ser enviado por el socket UDP
            paqueteEnviar = new DatagramPacket(datosAEnviar,datosAEnviar.length, ipServidor, puertoServidor);
            //se envia el datagrama
            dtSocket.send(paqueteEnviar);


            Log.i("Mensaje UDP enviado:","OK");
        }
        catch(Exception e)
        {
            Log.e("Error al enviar msj:", e.toString());
            mException=e;

        }
        return null;
    }

    //Al finalizar la ejecucion del metodo doInBackground, se analiza el reultado devuelto
    protected void onPostExecute(String result)
    {
        super.onPostExecute(result);
        //Si se genero una excepcion durante la ejecucion del thread
        if (mException != null) {
            //Toast.makeText(this.contexto.getApplicationContext(),"Error en GET:\n"+mException.toString(),Toast.LENGTH_LONG).show();
            caller.mostrarToastMake("Error al enviar por UDP:\n" + mException.toString());
            return;
        }


    }


}
