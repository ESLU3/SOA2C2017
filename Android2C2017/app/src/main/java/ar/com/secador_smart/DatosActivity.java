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
public class DatosActivity extends Activity
{

    //variables txt para el layout de los datos obtenidos de arduino
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

    Notification notificacionFin = null;

    NotificationManager nManager = null;
    boolean mBounded;
    private ComunicacionService mService;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunicacion);

        // Accedemos al servicio de sensores

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
        btnObtener= (Button) findViewById(R.id.btnObtener);
        btnObtener.setOnClickListener(btnObtenerListener);



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
            if(datos != null && datos.length()>3) {
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
