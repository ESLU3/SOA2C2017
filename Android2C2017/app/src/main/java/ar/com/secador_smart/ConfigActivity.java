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
import android.widget.Button;
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
public class ConfigActivity extends Activity {

    private EditText txtUmbralTemp;
    private EditText txtUmbralHum;
    private Switch switchFan;
    private Switch switchHeater;

    private Button btnOriginal;
    private Button btnObtener;

    private static String heaterArduino;
    private static String fanArduino;

    boolean mBounded;
    ComunicacionService mService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        Intent mIntent = new Intent(this, ComunicacionService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);

        //Se definen los componentes del layout
        txtUmbralTemp = (EditText) findViewById(R.id.txtUmbralTemp);
        txtUmbralHum = (EditText) findViewById(R.id.txtUmbralHum);
        switchHeater = (Switch) findViewById(R.id.switchHeater);
        switchFan = (Switch) findViewById(R.id.switchFan);
        btnOriginal= (Button) findViewById(R.id.btnOriginal);
        btnObtener= (Button) findViewById(R.id.btnObtener);

        txtUmbralHum.setOnClickListener(btnUmbralHum);
        txtUmbralTemp.setOnClickListener(btnUmbralTemp);

        switchHeater.setOnCheckedChangeListener(switchHeaterListener);

        switchFan.setOnCheckedChangeListener(switchFanListener);

        btnOriginal.setOnClickListener(btnOriginalListener);

        btnObtener.setOnClickListener(btnObtenerListener);

    }

    private EditText.OnClickListener btnUmbralTemp = new EditText.OnClickListener() {
        @Override
        public void onClick(View b) {
            if (mService != null) {
                String valor = txtUmbralTemp.getText().toString();
                mService.write("t" + valor);
                Log.d("arduino", "envie umbral temp");
            }
        }
    };

    private EditText.OnClickListener btnUmbralHum = new EditText.OnClickListener() {
        @Override
        public void onClick(View b) {
            if (mService != null) {
                String valor = txtUmbralHum.getText().toString();
                mService.write("h" + valor);
                Log.d("arduino", "envie umbral hum");
            }
        }
    };

    private Button.OnClickListener btnOriginalListener = new EditText.OnClickListener() {
        @Override
        public void onClick(View b) {
            if (mService != null) {
                mService.write("o");
                Log.d("arduino", "envie restaurar");
                btnObtener.performClick();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener switchHeaterListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d("arduino", "detecte cambio en switch heater");
            if (isChecked) {
                // If the switch button is on
                switchHeater.setBackgroundColor(Color.GREEN);
                if (mService != null) {
                    mService.write("5");
                    Log.d("arduino", "envie switch heater on");
                }
            } else {
                // If the switch button is off
                switchHeater.setBackgroundColor(Color.RED);
                if (mService != null) {
                    mService.write("6");
                    Log.d("arduino", "envie switch heater off");
                }
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener switchFanListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d("arduino", "detecte cambio en switch fan");
            if (isChecked) {
                // If the switch button is on
                switchFan.setBackgroundColor(Color.GREEN);
                if (mService != null) {
                    mService.write("7");
                    Log.d("arduino", "envie switch fan on");
                }
            } else {
                // If the switch button is off
                switchFan.setBackgroundColor(Color.RED);
                if (mService != null) {
                    mService.write("8");
                    Log.d("arduino", "envie switch fan off");
                }
            }
        }
    };

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
            ComunicacionService.LocalBinder mLocalBinder = (ComunicacionService.LocalBinder) service;
            mService = mLocalBinder.getService();
        }
    };

    protected void onStart() {
        super.onStart();

    }



    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC05, creando un
    //socketBluethoot
    public void onResume() {
        super.onResume();
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
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {

        super.onRestart();
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Button.OnClickListener btnObtenerListener = new View.OnClickListener() {
        @Override
        public void onClick(View b) {
            String datos;

            datos = mService.read();
            if (datos != null && datos.length() > 3) {
                if (!datos.contains("fin")) { //si es fin del proceso
                    String[] datosArduino = datos.split("\\|"); //obtengo string enviado desde HC06 y hago split segun regex
                    heaterArduino = datosArduino[3]; //obtengo estado calentador
                    fanArduino = datosArduino[5]; //obtengo estado fan
                    if (heaterArduino.equals("ON")) {
                        switchHeater.setChecked(true);
                    } else {
                        switchHeater.setChecked(false);
                    }
                    if (fanArduino.equals("ON")) {
                        switchFan.setChecked(true);
                    } else {
                        switchFan.setChecked(false);
                    }

                }


            }
        }
    };
}
