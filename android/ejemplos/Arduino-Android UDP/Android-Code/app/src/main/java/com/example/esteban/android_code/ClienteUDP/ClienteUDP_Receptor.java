/*********************************************************************************************************
 * Archivo que continen la clase que genera un hilo, mediante la clase Asynctask, el cual es el encargado
 * de recibir los mensajes que envia el Servidor con los valores que son sensados por el potenciometro
 **********************************************************************************************************/

package com.example.esteban.android_code.ClienteUDP;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

//Clase del hilo encargado de recibir los mensajes dels Servidor
public class ClienteUDP_Receptor extends AsyncTask <Void, Void, String>
{

    private InterfazAsyntask caller;
    public InetAddress ipServidor;
    public int puertoServidor;
    private DatagramSocket dtSocket;

    //Variable utilizada para almacenar la descripcion de la excepciones que se generen durante la ejecucion
    //del thread
    private Exception mException=null;

    //Constructor de la clase
    public ClienteUDP_Receptor(Activity a, DatagramSocket dtSocket, String ipServidor, int puertoServidor)
    {

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
    //metodo ejecutado por medio de execute. Recibe un array de parametros.
    //En este metodo se reciben los mensajes de Servidor
    protected String doInBackground(Void... params) {

        try
        {

            DatagramPacket paqueteRecibido;
            String mensajeRecibido = null;
            byte[] datosRecibidos = new byte[1024];

            //Se genera un paquete de datos vacio (datagrama) para almacenar el datagrama que se va recibir
            //del Servidor
            paqueteRecibido = new DatagramPacket(datosRecibidos, datosRecibidos.length);

            //El metodo doInBackground recibe como parametro una instancia de la claseUDP, cuando se invoca a executeOnExecutor
            //Por ese motivo se pueden acceder a los atributos de esa clase
            Log.i("LOG : ","Iniciando rececpcion de msjs UDP del Servidor...");

            //Se lee el Socket UDP para recibir los datos del Servidor.
            //Notar que receive es bloqueante, y se lo configura para el bloqueo de la recepcion
            //de datos es por 1 seg.
            dtSocket.setSoTimeout(10000);
            dtSocket.receive(paqueteRecibido);

            //Se lee el msj del datagrama recibido
            mensajeRecibido = new String(paqueteRecibido.getData(), 0, paqueteRecibido.getLength());


            return mensajeRecibido;
        }
        catch(Exception e)
        {
            Log.e("Error al recibir msj:", e.toString());
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
            caller.mostrarToastMake("Error al Recibir mensaje por UDP:\n" + mException.toString());
            return;
        }

        //Si se ejecuto el Request correctamente,se llama al metodo de la activity Principal encargado
        // de actualizar el valor de texto mostrado en el TextView

        caller.mostrarTextView(result);

    }

}
