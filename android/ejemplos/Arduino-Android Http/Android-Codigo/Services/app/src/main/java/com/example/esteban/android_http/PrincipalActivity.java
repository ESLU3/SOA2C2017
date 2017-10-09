/*********************************************************************************************************
 * Activity Principal de la App. Contiene el codigo del front-end de la aplicacion
 **********************************************************************************************************/
package com.example.esteban.android_http;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.esteban.android_http.ClienteHttp.AsynctaskHttp_POST;
import com.example.esteban.android_http.ClienteHttp.InterfazAsyntask;
import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.example.esteban.android_http.ClienteHttp.ServicesHttp_GET;

import java.util.Calendar;


//Clase de la Activity Principal de la Aplicación. Esta clase es el front-end
public class PrincipalActivity extends Activity implements InterfazAsyntask
{
    //variables de configuracion
    private String ipCompleta=null;

    //thread  de las clases encargadas de toda realizar la comunicacion con el Server
    private AsynctaskHttp_POST threadCliente_Post;


    //Elementos graficos de la App
    private Velocimetro velocimetro;
    private Button btnEncender, btnApagar;
    private TextView txtValorPotenciometro;

    public IntentFilter filtro;
    private ReceptorOperacion receiver =new ReceptorOperacion();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        btnApagar=(Button)findViewById(R.id.btnApagar);
        btnEncender=(Button)findViewById(R.id.btnEncender);
        txtValorPotenciometro= (TextView)findViewById(R.id.txtValorPotenciometro);

        //se establece el listener que atend    era los eventos de los botones
        btnEncender.setOnClickListener(botonesListeners);
        btnApagar.setOnClickListener(botonesListeners);

        //Se obtiene la IpCompleta del servidor recibiendo los paramteros enviados en el Intent de la activty Inicio
        configurarIpServidor();

        //se inicia el grafico del velocimetro
        iniciarVelocimetro();

        //Se crea y configurar un broadcast receiver para comunicar el servicio que recibe los mensaje del servidor
        //con la activity principal
        configurarBroadcastReciever();

        //Se inicia la recpcion de mensajes que envia el Servidor al cliente
        iniciarServicioRecepctorMsjs();;


    }

    //Metodo que obtiene la IpCompleta del servidor recibiendo los paramteros enviados en el Intent de la activty Inicio
    public void configurarIpServidor()
    {
        //se crea un objeto Bundle para poder recibir los parametros enviados por la activity Inicio
        //al momeento de ejecutar stratActivity
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();

        String ipServidor=(String)extras.get("ipServidor");
        String puertoServidor=(String)extras.get("puertoServidor");

        ipCompleta ="http://"+ipServidor+":"+puertoServidor;

    }

    //Metodo que crea y configurar un broadcast receiver para comunicar el servicio que recibe los mensaje del servidor
    //con la activity principal
    private void configurarBroadcastReciever()
    {
        //se asocia(registra) la  accion RESPUESTA_OPERACION, para que cuando el Servicio de recepcion la ejecute
        //se invoque automaticamente el OnRecive del objeto receiver
        filtro = new IntentFilter("com.example.intentservice.intent.action.RESPUESTA_OPERACION");

        filtro.addCategory(Intent.CATEGORY_DEFAULT);

        registerReceiver(receiver, filtro);


    }
    //Metodo que inicia la recpcion de mensajes que envia el Servidor al cliente
    public void   iniciarServicioRecepctorMsjs()
    {
        //se configura la uri del Servidor para que envie los datos del potenciometro atraves de un request GET
        String uri=ipCompleta+"/potenciometro";

        //***************Otra alternativa de repeticion continua de un  hilo*****************
        // Se configura una alarma para que cada determinado tiempo invioque al servicio receptor de msjs
        // atraves de otro intent

        // refresh every 60 seconds in MyService.java
        Calendar cal = Calendar.getInstance();

        //se asocia el intent al servicio
        Intent  intent = new Intent(this, ServicesHttp_GET.class);
        //se agrega el parametro uri
        intent.putExtra("uri",uri);


        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        // esta instruccion es la que realiza la repeticion del servicio cada 1.5 mseg
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 50, pintent);

        Toast.makeText(getApplicationContext(),"Cliente Activo...",Toast.LENGTH_SHORT).show();
    }



    //Metodo que es llamada cuando se cierra la APP
    protected void onDestroy()
    {
        super.onDestroy();

        unregisterReceiver(receiver);

        Intent i = new Intent(this, ServicesHttp_GET.class);
        stopService(i);

        Toast.makeText(getApplicationContext(),"eliminando Servicio...",Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(),"Cerrando Aplicacion...",Toast.LENGTH_LONG).show();



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
                    threadCliente_Post = new AsynctaskHttp_POST(PrincipalActivity.this);
                    threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,uri,estadoled);

                    Toast.makeText(getApplicationContext(),"Encendido",Toast.LENGTH_SHORT).show();
                    break;

                //Si se presiono el Boton Apagar
                case R.id.btnApagar:
                    estadoled="0";

                    //Se crea y ejecuta un Thread que envia una peticion POST al servidor para que apague el led
                    threadCliente_Post = new AsynctaskHttp_POST(PrincipalActivity.this);
                    threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,uri,estadoled);

                    Toast.makeText(getApplicationContext(),"Apagado",Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_SHORT).show();
            }


        }
    };

    //Clase BroadcastReceiver que recibira los msj que envia el servicio receptor de msj con los valores
    //del potenciometro
    public class ReceptorOperacion extends BroadcastReceiver

    {


        public void onReceive(Context context, Intent intent) {

            //Se obtiene los valores que envio el servicio atraves de un untent
            //NOtAR la utilizacion de un objeto Bundle es opcional.
            Float valorSensado = intent.getFloatExtra("valorSensado",0);
            String mensajeSensor= intent.getStringExtra("mensajeSensor");

           // Toast.makeText(getApplicationContext(),"valor n"+valorSensado,Toast.LENGTH_LONG).show();
            mostrarTextViewPotenciometro(mensajeSensor.toString());
            actualizarVelocimetro(valorSensado);


        }

    }


}
