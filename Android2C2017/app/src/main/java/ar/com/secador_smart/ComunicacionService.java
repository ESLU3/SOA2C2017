package ar.com.secador_smart;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
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
public class ComunicacionService extends Service implements SensorEventListener
{
    public Handler handler;
    private SensorManager mSensorManager;
    private final int ACC = 20; //variable para umbral del Shake
    private float last_x = 0;
    private float last_z = 0;
    private float last_y = 0;
    private TextView txtComunicacionPaired;
    private TextView txtComunicacionDevice;
    private TextView txtSensorDetected;
    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    //Variable para el hilo que manejará la comunicacion
    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // variable donde se guardará la direccion MAC del HC06 del Arduino
    private String address = null;

    public ComunicacionService() {

       /* new Thread(new Runnable() {
            public void run() {


                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String mensajeArduino = null;


                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mensajeArduino = Cliente.comunicarse(Menu.ip, Menu.puerto, "z");
                    //System.out.println("Hola " + modifiedSentence);
                    if (modifiedSentence != null) {
                        String[] separated = modifiedSentence.split("\\|");

                        Menu.temperaturaActual = separated[0];
                        Menu.estadoTemperatura = separated[1];
                        Menu.flama = separated[2];
                        Menu.buzzerEstado = Integer.parseInt(separated[3]);
                        Menu.temperaturaCrit = separated[4];


                    } else {

                        Menu.temperaturaActual = "-";
                        Menu.temperaturaCrit = "-";
                        Menu.flama = "-";

                    }

                    handler.sendEmptyMessage(0);
                }

            }
        }).start();*/
    }
        public IBinder onBind(Intent intent) {
            return null;
        }
        // Metodo que escucha el cambio de sensibilidad de los sensores

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
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
    // Metodo que escucha el cambio de los sensores

    public void onSensorChanged(SensorEvent event) {

        synchronized (this) { //esto implica que solo escuchará de a un sensor a la vez

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    if ((Math.abs(event.values[0]) > ACC || Math.abs(event.values[1]) > ACC || Math.abs(event.values[2]) > ACC)) {
                        if (mConnectedThread != null) {
                            mConnectedThread.write("1");
                            Log.d("sensor", "shake detected");
                            txtSensorDetected.setText("Shake detectado");
                        }
                    }

                    break;


                case Sensor.TYPE_PROXIMITY:
                    if (event.values[0] < 20) {
                        if (mConnectedThread != null) {
                            mConnectedThread.write("2");
                            txtSensorDetected.setText("Proximidad Detectada");
                            Log.d("sensor", "Proximidad Detectada");
                        }

                    }
                    break;

                case Sensor.TYPE_LIGHT:
                    if (event.values[0] < 5) {
                        if (mConnectedThread != null) {
                            mConnectedThread.write("3");
                            txtSensorDetected.setText("Poca luz detectada");
                            Log.d("sensor", "Poca luz detectada");
                        }
                    }

                    break;
            }
        }
    }
        //Metodo que crea el socket bluethoot
        private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

            return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        }


        private void showToast(String message) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }

        //******************************************** Hilo secundario del Activity**************************************
        //*************************************** recibe los datos enviados por el HC05**********************************

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

            //metodo run del hilo, que va a entrar en una espera activa para recibir los msjs del HC05
            public void run()
            {
                byte[] buffer = new byte[256];
                int bytes;

                //el hilo secundario se queda esperando mensajes del HC05
                while (true)
                {
                    try
                    {
                        //se leen los datos del Bluethoot
                        bytes = mmInStream.read(buffer);
                        String readMessage = new String(buffer, 0, bytes);

                         //se muestran en el layout de la activity, utilizando el handler del hilo
                        // principal antes mencionado
                        bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
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

                }
            }
        }

    }
