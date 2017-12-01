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
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
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

    private EditText txtUmbralTemp;
    private EditText txtUmbralHum;
    private Switch switchFan;
    private Switch switchHeater;

    private TextView txtComunicacionDevice;

    private static String tempArduino;
    private static String humArduino;
    private static String luzArduino;
    private static String heaterArduino;
    private static String finArduino;
    private static String fanArduino;

    boolean mBounded;
    ComunicacionService mService;

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
        setContentView(R.layout.activity_config);

        //Se definen los componentes del layout
        txtUmbralTemp= (EditText) findViewById(R.id.txtUmbralTemp);
        txtUmbralHum= (EditText) findViewById(R.id.txtUmbralHum);
        txtComunicacionDevice= (TextView) findViewById(R.id.txtComunicacionDevice);
        switchHeater = (Switch) findViewById(R.id.switchHeater);
        switchFan = (Switch) findViewById(R.id.switchFan);

        //obtengo el adaptador del bluethoot

        txtComunicacionDevice.setText(mService.getDevice().getName());

        txtUmbralHum.setOnClickListener(btnUmbralHum);
        txtUmbralTemp.setOnClickListener(btnUmbralTemp);

        switchHeater.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    // If the switch button is on
                    switchHeater.setBackgroundColor(Color.GREEN);
                    if(mConnectedThread!=null){
                        mConnectedThread.write("h1");
                        Log.d("arduino", "envie switch heater on");
                    }
                }
                else {
                    // If the switch button is off
                    switchHeater.setBackgroundColor(Color.RED);
                    if(mConnectedThread!=null){
                        mConnectedThread.write("h0");
                        Log.d("arduino", "envie switch heater off");
                    }
                }
            }
        });
        switchFan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    // If the switch button is on
                    switchFan.setBackgroundColor(Color.GREEN);
                    if(mConnectedThread!=null){
                        mConnectedThread.write("f1");
                        Log.d("arduino", "envie switch fan on");
                    }
                }
                else {
                    // If the switch button is off
                    switchFan.setBackgroundColor(Color.RED);
                    if(mConnectedThread!=null){
                        mConnectedThread.write("f0");
                        Log.d("arduino", "envie switch fan off");
                    }
                }
            }
        });



    }
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(ConfigActivity.this, "Service is disconnected", Toast.LENGTH_LONG).show();
            mBounded = false;
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(ConfigActivity.this, "Service is connected", Toast.LENGTH_LONG).show();
            mBounded = true;
            ComunicacionService.LocalBinder mLocalBinder = (ComunicacionService.LocalBinder)service;
            mService = mLocalBinder.getServerInstance();
        }
    };
    protected void onStart() {
        super.onStart();

        Intent mIntent = new Intent(this, ComunicacionService.class);
        bindService(mIntent, mService, BIND_AUTO_CREATE);
    };
    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC05, creando un
    //socketBluethoot
    public void onResume() {
        super.onResume();
        mConnectedThread = new ConfigActivity.ConnectedThread(mService.getBtSocket());
        mConnectedThread.start();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    //Cuando se ejecuta el evento onPause se cierra el socket Bluethoot, para no recibiendo datos
    public void onPause()
    {
        super.onPause();
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

        super.onRestart();
    }
    //Metodo que crea el socket bluethoot
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private EditText.OnClickListener btnUmbralTemp = new EditText.OnClickListener() {
        @Override
        public void onClick(View b) {
            if(mConnectedThread!=null){
                String valor = txtUmbralTemp.getText().toString();
                mConnectedThread.write("t" + valor);
                Log.d("arduino", "envie umbral temp");
            }
        }
    };

    private EditText.OnClickListener btnUmbralHum = new EditText.OnClickListener() {
        @Override
        public void onClick(View b) {
            if(mConnectedThread!=null){
                String valor = txtUmbralHum.getText().toString();
                mConnectedThread.write("h" + valor);
                Log.d("arduino", "envie umbral hum");
            }
        }
    };


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
                /*try
                {
                    //se leen los datos del Bluethoot
                   // bytes = mmInStream.read(buffer);
                   // String readMessage = new String(buffer, 0, bytes);
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

                    }

                     //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                   // bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }*/
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
