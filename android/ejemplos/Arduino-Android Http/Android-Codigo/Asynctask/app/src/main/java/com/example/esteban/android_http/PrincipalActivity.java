/*********************************************************************************************************
 * Activity Principal de la App. Contiene el codigo del front-end de la aplicacion
 **********************************************************************************************************/
package com.example.esteban.android_http;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.esteban.android_http.ClienteHttp.ClienteHttp_GET;
import com.example.esteban.android_http.ClienteHttp.ClienteHttp_POST;
import com.example.esteban.android_http.ClienteHttp.InterfazAsyntask;
import com.cardiomood.android.controls.gauge.SpeedometerGauge;

import java.net.MalformedURLException;
import java.util.TimerTask;


//Clase de la Activity Principal de la Aplicación. Esta clase es el front-end
public class PrincipalActivity extends Activity implements InterfazAsyntask
{
    //variables de configuracion
    private String ipCompleta=null;

    //thread  de las clases encargadas de toda realizar la comunicacion con el Server
    private ClienteHttp_GET threadCliente_Get;
    private ClienteHttp_POST threadCliente_Post;

    //Handler utilizado para poder ejecutar repetidamente perticiones GET en un hilo Asynctask
    private Handler handler;

    //Elementos graficos de la App
    private Velocimetro velocimetro;
    private Button btnEncender, btnApagar;
    private TextView txtValorPotenciometro;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        btnApagar=(Button)findViewById(R.id.btnApagar);
        btnEncender=(Button)findViewById(R.id.btnEncender);
        txtValorPotenciometro= (TextView)findViewById(R.id.txtValorPotenciometro);

        //se establece el listener que atendera los eventos de los botones
        btnEncender.setOnClickListener(botonesListeners);
        btnApagar.setOnClickListener(botonesListeners);

        //se inicia el grafico del velocimetro
        iniciarVelocimetro();

        //se crea un objeto Bundle para poder recibir los parametros enviados por la activity Inicio
        //al momeento de ejecutar stratActivity
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();

        String ipServidor=(String)extras.get("ipServidor");
        String puertoServidor=(String)extras.get("puertoServidor");

        ipCompleta ="http://"+ipServidor+":"+puertoServidor;

        Toast.makeText(getApplicationContext(),"Cliente Activo...",Toast.LENGTH_SHORT).show();

        //Se inicia la recpcion de mensajes que enviar el Servidor al cliente
        iniciarRecepcionMsjs();

    }

    //Metodo que incia la recpcion de mensajes que envia el Servidor al cliente. Enviandole continaumente
    //peticiones Get al Servidor para que le envie los valores sensador por el potenciometro.
    public void iniciarRecepcionMsjs()
    {
        //Se genera un objeto Handler dado que es una forma segura de ejecutar reprtidamente Treads Asynctask sin problemas.
        //No se utiliza un bucle en el metodo doInBackroung, ni un TimeTask dado que no son formas seguras de ejecucion. Ya que
        //pueden fallar
        handler = new Handler();

        //uri con que responde el Servidor a las peticiones GET que devuelven el valor sensado por el pootenciometro
        final String uri=ipCompleta+"/potenciometro";

        Runnable refresh = new Runnable()
        {
            @Override
            public void run()
            {
                //Se crea y ejecuta un Thread que envia una peticion GET al servidor que le solicita los valores del potenciometro
                threadCliente_Get = new ClienteHttp_GET(PrincipalActivity.this);
                threadCliente_Get.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,uri);

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
        super.onDestroy();

        //Cuando se cierra la APP se detiene la recepcion de los msjs del Servidor
        handler.removeCallbacksAndMessages(null);


        Log.i("LOG","Recepcion de msjs finalizada");
        Toast.makeText(getApplicationContext(),"Cerrando Aplicacion...",Toast.LENGTH_SHORT).show();
    }

    //Metodo utilizado para mostrar una msj de notificacion al usuario desde la activity principal
    //Este metodo es utilizado principalmente por las clases ClienteHttp
    @Override
    public void mostrarToastMake(String msg)
    {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    //Metodo utilizado para cambiar el contenido que muesta el TextView txtValorPotenciometro en la Activity Principal
    //Este metodo es utilizado principalmente por las clases ClienteHttp
    @Override
    public void mostrarTextViewPotenciometro(String msg)
    {
        Log.i("Valor ",msg);
        txtValorPotenciometro.setText("");
        txtValorPotenciometro.setText(msg);
    }

    //Metodo utilizado para cambiar el grafico del Velocimetro en la Activity Principal
    //Este metodo es utilizado principalmente por las clases ClienteHttp
    public void actualizarVelocimetro(float valor)
    {
        velocimetro.modificarVelocimetro(valor);
    }


    public void iniciarVelocimetro(){
        SpeedometerGauge speedometer;
        speedometer = (SpeedometerGauge) findViewById(R.id.speedometer);
        velocimetro = new Velocimetro(speedometer);
    }

    //Metodo que actua como Listener de los eventos que ocurren en los componentes graficos de la activty principal
    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {

        public void onClick(View v)
        {
            String uri=ipCompleta+"/led/3";
            String estadoled;

            //Se determina que componente genero un evento
            switch (v.getId())
            {
                //Si se presiono el Boton Encender
                case R.id.btnEncender:
                    estadoled="1";
                    //Se crea y ejecuta un Thread que envia una peticion POST al servidor para que encienda el led
                    threadCliente_Post = new ClienteHttp_POST(PrincipalActivity.this);
                    threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,uri,estadoled);

                    Toast.makeText(getApplicationContext(),"Encendido",Toast.LENGTH_SHORT).show();
                    break;

                //Si se presiono el Boton Apagar
                case R.id.btnApagar:
                    estadoled="0";

                    //Se crea y ejecuta un Thread que envia una peticion POST al servidor para que apague el led
                    threadCliente_Post = new ClienteHttp_POST(PrincipalActivity.this);
                    threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,uri,estadoled);

                    Toast.makeText(getApplicationContext(),"Apagado",Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_SHORT).show();
            }


        }
    };


}
