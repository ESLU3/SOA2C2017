package ar.com.secador_smart;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Button btnDatos;
    private Button btnConfig;
    private TextView txtSensorDetected;
    private static final int ACC = 15; //variable para umbral del Shake


    boolean mBounded;

    private ComunicacionService mService;


    /**
     * Called when the activity is first created.
     */
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service is disconnected", Toast.LENGTH_LONG).show();
            mBounded = false;
            mService = null;

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "Service is connected", Toast.LENGTH_LONG).show();
            mBounded = true;
            ComunicacionService.LocalBinder mLocalBinder = (ComunicacionService.LocalBinder)service;
            mService = mLocalBinder.getService();
        }
    };
    protected void onStart() {
        super.onStart();
        Intent mIntent = new Intent(getApplicationContext(), ComunicacionService.class);
        bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        btnDatos = (Button) findViewById(R.id.btnDatos);
        btnConfig = (Button) findViewById(R.id.btnConfig);

        btnDatos.setOnClickListener(btnDatosListener);
        btnConfig.setOnClickListener(btnConfigListener);

        txtSensorDetected= (TextView) findViewById(R.id.txtSensorDetected);


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
                        if (mService != null) {
                            mService.write("1");
                            Log.d("arduino", "shake detected");
                            txtSensorDetected.setText("Shake detectado");
                        }
                    }

                    break;


                case Sensor.TYPE_PROXIMITY:
                    if (event.values[0] < 20) {
                        if (mService != null) {
                            mService.write("2");
                             txtSensorDetected.setText("Proximidad Detectada");
                            Log.d("arduino", "Proximidad Detectada");
                        }

                    }
                    break;

                case Sensor.TYPE_LIGHT:
                    if (event.values[0] < 100){
                        if (mService != null) {
                            mService.write("3");
                             txtSensorDetected.setText("Poca luz detectada");
                            Log.d("arduino", "Poca luz detectada");
                        }
                    }

                    break;
            }
        }
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
    protected void onPause() {
        Parar_Sensores();
        super.onPause();
    }

    @Override
    protected void onRestart() {
        Ini_Sensores();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Ini_Sensores();

    }


    private View.OnClickListener btnDatosListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, DatosActivity.class);
            startActivity(intent);

        }
    };


    private View.OnClickListener btnConfigListener = new View.OnClickListener() {
        @Override
        public void onClick(View b) {
            Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
            startActivity(intent);
        }
    };



}
