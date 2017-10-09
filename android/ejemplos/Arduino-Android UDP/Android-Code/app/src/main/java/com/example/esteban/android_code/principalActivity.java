/*********************************************************************************************************
 * Activity Principal de la App. Contiene el codigo del front-end de la aplicacion
 **********************************************************************************************************/
package com.example.esteban.android_code;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.esteban.android_code.ClienteUDP.ClienteUDP_Emisor;
import com.example.esteban.android_code.ClienteUDP.ClienteUDP_Receptor;
import com.example.esteban.android_code.ClienteUDP.InterfazAsyntask;

import java.net.DatagramSocket;

//Clase de la Activity Principal de la Aplicación. Esta clase es el front-end
public class principalActivity extends Activity implements InterfazAsyntask
{
    //variables de configuracion
    final String IP_SERVIDOR ="192.168.0.200";
    final int PUERTO_SERVIDOR = 8032;
    final int PUERTO_CLIENTE = 1234;

    //variable con almacenara una instancia del socket que se va a utilizar para
    //la comunicacion entre el Cliente y el Servidor
    private DatagramSocket dtSocket;

    //thread  de las clases encargadas de toda realizar la comunicacion con el Server
    private ClienteUDP_Emisor threadEmisor_UDP;
    private ClienteUDP_Receptor threadRecepetor_UDP;


    //Handler utilizado para poder ejecutar repetidamente perticiones GET en un hilo Asynctask
    private Handler handler;

    //Elementos graficos de la App
    Button btnEncender, btnApagar;
    TextView txtValorPotenciometro;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_principal);

            btnApagar=(Button)findViewById(R.id.btnApagar);
            btnEncender=(Button)findViewById(R.id.btnEncender);
            txtValorPotenciometro= (TextView)findViewById(R.id.txtValorPotenciometro);

            //se establece el listener que atendera los eventos de los botones
            btnEncender.setOnClickListener(botonesListeners);
            btnApagar.setOnClickListener(botonesListeners);

            //Se crea un socket para efectuar la comunicacion con el Servidor
            dtSocket=new DatagramSocket(PUERTO_CLIENTE);

            //Se inicia la recpcion de mensajes que enviar el Servidor al cliente
            iniciarRecepcionMsjs();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Error:"+e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    //Metodo que incia la recpcion de mensajes que envia el Servidor al cliente. Enviandole continaumente
    //peticiones  al Servidor para que le envie los valores sensador por el potenciometro.
    public void iniciarRecepcionMsjs()
    {
        //Se genera un objeto Handler dado que es una forma segura de ejecutar reprtidamente Treads Asynctask sin problemas.
        //No se utiliza un bucle en el metodo doInBackroung, ni un TimeTask dado que no son formas seguras de ejecucion. Ya que
        //pueden fallar
        handler = new Handler();

        Runnable refresh = new Runnable()
        {
            @Override
            public void run()
            {
                //Se crea y ejecuta un Thread que envia una peticion  al servidor que le solicita los valores del potenciometro
                threadRecepetor_UDP= new ClienteUDP_Receptor(principalActivity.this,dtSocket,IP_SERVIDOR,PUERTO_SERVIDOR);
                threadRecepetor_UDP.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                //Se ejecuta cada 1.5 segundos el codigo del metodo run de la interfaz Runnable refresh
                handler.postDelayed(this, 1500);
            }
        };
        //Se ejecuta una vez el codigo del metodo run de la interfaz Runnable refresh
        handler.postDelayed(refresh, 1500);

    }

    //Metodo que es llamada cuando se cierra la APP
    protected void onDestroy()
    {
        try {

            super.onDestroy();

            //Cuando se cierra la APP se detiene la recepcion de los msjs del Servidor
            handler.removeCallbacksAndMessages(null);
            Thread.sleep(2000);
            dtSocket.close();

            Log.i("LOG","Recepcion de msjs finalizada");
            Toast.makeText(getApplicationContext(),"Cerrando Aplicacion...",Toast.LENGTH_SHORT).show();

        } catch (InterruptedException e) {
            Toast.makeText(getApplicationContext(),"Error"+e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    //Metodo utilizado para mostrar una msj de notificacion al usuario desde la activity principal
    //Este metodo es utilizado principalmente por la clase ClienteUDP
    @Override
    public void mostrarToastMake(String msg)
    {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    //Metodo utilizado para cambiar el contenido que muesta el TextView txtValorPotenciometro en la Activity Principal
    //Este metodo es utilizado principalmente por la clase ClienteUDP
    @Override
    public void mostrarTextView(String msg)
    {
        Log.i("Valor ",msg);
        txtValorPotenciometro.setText("");
        txtValorPotenciometro.setText(msg);
    }

    //Metodo que actua como Listener de los eventos que ocurren en los componentes graficos de la activty principal
    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {

        public void onClick(View v)
        {
            String estadoLed;
            //Se determina que componente genero un evento
            switch (v.getId())
            {
                //Si se presiono el Boton Encender
                case R.id.btnEncender:
                    estadoLed="1";

                    threadEmisor_UDP=new ClienteUDP_Emisor(principalActivity.this,dtSocket,IP_SERVIDOR,PUERTO_SERVIDOR);
                    threadEmisor_UDP.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,estadoLed);

                    Toast.makeText(getApplicationContext(),"Encendido",Toast.LENGTH_SHORT).show();
                    break;

                //Si se presiono el Boton Apagar
                case R.id.btnApagar:
                    estadoLed="2";

                    threadEmisor_UDP=new ClienteUDP_Emisor(principalActivity.this,dtSocket,IP_SERVIDOR,PUERTO_SERVIDOR);
                    threadEmisor_UDP.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,estadoLed);

                    Toast.makeText(getApplicationContext(),"Apagado",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_LONG).show();
            }

        }
    };


}
