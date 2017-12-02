package ar.com.secador_smart;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;

/*********************************************************************************************************
 * Activity que muestra realiza la comunicacion con Arduino
 **********************************************************************************************************/

//******************************************** Hilo principal del Activity**************************************
public class DatosActivity extends Activity implements SensorEventListener
{
    private SensorManager mSensorManager;
    private static final int ACC = 15; //variable para umbral del Shake
    //variables txt para el layout de los datos obtenidos de arduino
    //private TextView txtComunicacionPaired;
    private TextView txtComunicacionDevice;
    private TextView txtTempArduino;
    private TextView txtHumArduino;
    private TextView txtLuzArduino;
    private TextView txtFanArduino;
    private TextView txtHeaterArduino;
    private TextView txtTiempoEstimado;

    private Button btnObtener;

    private static String tempArduino;
    private static String humArduino;
    private static String luzArduino;
    private static String heaterArduino;
    private static String finArduino;
    private static String fanArduino;
    //manager para mostrar notificaciones

    Notification notificacionFin = null;

    NotificationManager nManager = null;
    boolean mBounded;
    private ComunicacionService mService;



    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    //Variable para el hilo que manejará la comunicacion
    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // variable donde se guardará la direccion MAC del HC06 del Arduino
    private static String address = null;

    //Metodo para registrar los sensosres en el Manager
    protected void Ini_Sensores() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Metodo para parar la escucha de los sensores
    private void Parar_Sensores() {

        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
    }

    // Metodo que escucha el cambio de sensibilidad de los sensores
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Metodo que escucha el cambio de los sensores
    @Override
    public void onSensorChanged(SensorEvent event) {

        synchronized (this) { //esto implica que solo escuchará de a un sensor a la vez

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    if ((Math.abs(event.values[0]) > ACC || Math.abs(event.values[1]) > ACC || Math.abs(event.values[2]) > ACC)){
                        if (mConnectedThread != null) {
                            mConnectedThread.write("1");
                            Log.d("sensor", "shake detected");
                           // txtSensorDetected.setText("Shake detectado");
                        }
                    }

                    break;


                case Sensor.TYPE_PROXIMITY:
                    if (event.values[0] < 20) {
                        if (mConnectedThread != null) {
                            mConnectedThread.write("2");
                           // txtSensorDetected.setText("Proximidad Detectada");
                            Log.d("sensor", "Proximidad Detectada");
                        }

                    }
                    break;

                case Sensor.TYPE_LIGHT:
                    if (event.values[0] < 5){
                        if (mConnectedThread != null) {
                            mConnectedThread.write("3");
                           // txtSensorDetected.setText("Poca luz detectada");
                            Log.d("sensor", "Poca luz detectada");
                        }
                    }

                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunicacion);



        // Accedemos al servicio de sensores
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, DatosActivity.class);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        notificacionFin  = new Notification.Builder(this)
                .setContentTitle("Secado finalizado!!")
                .setContentText("Secador SMART")
                .setSmallIcon(R.drawable.reloj)
                .setContentIntent(pIntent)
                .setAutoCancel(true).build();

        //Se definen los componentes del layout
        txtTempArduino= (TextView) findViewById(R.id.txtTempArduino);
        txtHumArduino= (TextView) findViewById(R.id.txtHumArduino);
        txtLuzArduino= (TextView) findViewById(R.id.txtLuzArduino);
        txtFanArduino= (TextView) findViewById(R.id.txtFanArduino);
        txtHeaterArduino= (TextView) findViewById(R.id.txtHeaterArduino);
        txtTiempoEstimado= (TextView) findViewById(R.id.txtTiempoEstimado);
       // txtComunicacionPaired= (TextView) findViewById(R.id.txtComunicacionPaired);
        txtComunicacionDevice= (TextView) findViewById(R.id.txtComunicacionDevice);
        //txtSensorDetected= (TextView) findViewById(R.id.txtSensorDetected);
        btnObtener= (Button) findViewById(R.id.btnObtener);
        btnObtener.setOnClickListener(btnObtenerListener);
        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();



    }
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(DatosActivity.this, "Service is disconnected", Toast.LENGTH_LONG).show();
            mBounded = false;
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(DatosActivity.this, "Service is connected", Toast.LENGTH_LONG).show();
            mBounded = true;
            ComunicacionService.LocalBinder mLocalBinder = (ComunicacionService.LocalBinder)service;
            mService = mLocalBinder.getService();
        }
    };

    protected void onStart() {
        super.onStart();

        Intent mIntent = new Intent(this, ComunicacionService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);

    };





    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC06, creando un
    //socketBluethoot
    public void onResume() {
        super.onResume();

        Ini_Sensores();


    }

    @Override
    protected void onStop() {

        Parar_Sensores();
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        Parar_Sensores();
        super.onDestroy();
    }
    @Override
    //Cuando se ejecuta el evento onPause se cierra el socket Bluethoot, para no recibiendo datos
    public void onPause()
    {
        super.onPause();
        Parar_Sensores();
        /*try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }*/
    }
    @Override
    protected void onRestart() {
        Ini_Sensores();

        super.onRestart();
    }
    //Metodo que crea el socket bluethoot
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //******************************************** Hilo secundario del Activity**************************************

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //Constructor de la clase del hilo secundario
        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //metodo run del hilo, que va a entrar en una espera activa para recibir los msjs del HC06
        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            //el hilo secundario se queda esperando mensajes del HC06
            while (true)
            {
                try
                {
                    Thread.sleep(5000);
                    //se leen los datos del Bluethoot



                     //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                   // bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //write method
        public String read() {
            byte[] msgBuffer = new byte[256];           //converts entered String into bytes
            int bytes;
            String readMessage = null;
            try {
                bytes = mmInStream.read(msgBuffer);
                readMessage = new String(msgBuffer, 0, bytes);
                msgBuffer[bytes] = '\0';

            } catch (IOException e) {
                //if you cannot write, close the application
                showToast("La conexion fallo");
                finish();

            }
            return readMessage;
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                showToast("La conexion fallo");
                finish();

            }
        }
    }

    private Button.OnClickListener btnObtenerListener = new View.OnClickListener() {
        @Override
        public void onClick(View b) {
            /*dialog.dismiss();

            mBluetoothAdapter.cancelDiscovery();*/

            //Una establecida la conexion con el Hc05 se crea el hilo secundario, el cual va a recibir
            // los datos de Arduino atraves del bluethoot
            String datos;
            mConnectedThread = new ConnectedThread(mService.getBtSocket());
            mConnectedThread.start();

            //I send a character when resuming.beginning transmission to check device is connected
            //If it is not an exception will be thrown in the write method and finish() will be called
            mConnectedThread.write("x");
            datos = mConnectedThread.read();
            if(datos != null) {
                if (!datos.contains("fin")) { //si es fin del proceso
                    String[] datosArduino = datos.split("\\|"); //obtengo string enviado desde HC06 y hago split segun regex
                    Log.d("arduino", "datos: " + datos);
                    tempArduino = datosArduino[0]; //obtengo temperatura desde arduino
                    humArduino = datosArduino[1]; //obtengo humedad desde arduino
                    luzArduino = datosArduino[2]; //obtengo luz desde arduino
                    heaterArduino = datosArduino[3]; //obtengo estado calentador
                    finArduino = datosArduino[4]; //obtengo estado calentador
                    fanArduino = datosArduino[5]; //obtengo estado fan
                    txtTempArduino.setText(tempArduino);
                    txtHumArduino.setText(humArduino);
                    txtLuzArduino.setText(luzArduino);
                    txtFanArduino.setText(fanArduino);
                    txtHeaterArduino.setText(heaterArduino);

                    if (Float.parseFloat(humArduino) > 30) {
                        if (Float.parseFloat(tempArduino) < 20) {
                            txtTiempoEstimado.setText("2 horas");
                        } else {
                            txtTiempoEstimado.setText("1 hora");
                        }
                    } else if (Float.parseFloat(humArduino) > 20) {
                        if (Float.parseFloat(tempArduino) < 20) {
                            txtTiempoEstimado.setText("1 hora");
                        } else {
                            txtTiempoEstimado.setText("Media Hora");
                        }
                    } else {
                        txtTiempoEstimado.setText("Secado terminado en breve");
                    }

                }else {
                    nManager.notify(12345, notificacionFin);
                    txtTiempoEstimado.setText("Secado finalizado!!");
                }
            }



        }
    };
}
