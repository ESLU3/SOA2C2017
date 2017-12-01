package ar.com.secador_smart;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/*********************************************************************************************************
 * Activity que muestra realiza la comunicacion con Arduino
 **********************************************************************************************************/

//******************************************** Hilo principal del Activity**************************************
public class ConfigActivity extends Activity
{

    private TextView txtTempArduino;
    private TextView txtHumArduino;
    private TextView txtLuzArduino;
    private TextView txtFanArduino;
    private TextView txtHeaterArduino;
    private TextView txtTiempoEstimado;

    private static String tempArduino;
    private static String humArduino;
    private static String luzArduino;
    private static String heaterArduino;
    private static String finArduino;
    private static String fanArduino;

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


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunicacion);

        //Se definen los componentes del layout
        txtTempArduino= (TextView) findViewById(R.id.txtTempArduino);
        txtHumArduino= (TextView) findViewById(R.id.txtHumArduino);
        txtLuzArduino= (TextView) findViewById(R.id.txtLuzArduino);
        txtFanArduino= (TextView) findViewById(R.id.txtFanArduino);
        txtHeaterArduino= (TextView) findViewById(R.id.txtHeaterArduino);
        txtTiempoEstimado= (TextView) findViewById(R.id.txtTiempoEstimado);
       // txtComunicacionPaired= (TextView) findViewById(R.id.txtComunicacionPaired);
       // txtComunicacionPaired= (TextView) findViewById(R.id.txtComunicacionPaired);
          //txtSensorDetected= (TextView) findViewById(R.id.txtSensorDetected);

        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent=getIntent();
        Bundle extras=intent.getExtras();

        address= extras.getString("Dispositivo conectado");

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        //txtComunicacionDevice.setText(device.getName());

    }

    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC05, creando un
    //socketBluethoot
    public void onResume() {
        super.onResume();

        Ini_Sensores();
        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();

        address= extras.getString("Dispositivo conectado");

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
        try
        {
            btSocket = createBluetoothSocket(device);
        }
        catch (IOException e)
        {
            showToast( "La creacción del Socket fallo");
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        }
        catch (IOException e)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e2)
            {
                //insert code to deal with this
            }
        }

        //Una establecida la conexion con el Hc05 se crea el hilo secundario, el cual va a recibir
        // los datos de Arduino atraves del bluethoot
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
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
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
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
                    //se leen los datos del Bluethoot
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    if(!readMessage.isEmpty()) {
                        String[] datosArduino = readMessage.split("\\||"); //obtengo string enviado desde HC06 y hago split segun regex
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
                        if (finArduino.equals("1")) { //si es fin del proceso
                            nManager.notify(12345, notificacionFin);
                            txtTiempoEstimado.setText("Secado finalizado!!");
                        }
                    }

                     //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                   // bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
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

}
