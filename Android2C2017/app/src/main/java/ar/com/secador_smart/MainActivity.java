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
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity  {
    //variables Bluetooth
    private TextView txtEstado;
   /* private Button btnActivar;
    private Button btnEmparejar;
    private Button btnBuscar;
    private ProgressDialog mProgressDlg;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private BluetoothAdapter mBluetoothAdapter;*/
    private Button btnDatos;
    private Button btnConfig;
    private TextView txtComunicacionDevice;

    boolean mBounded;
    ComunicacionService mService;

    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();


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
            mService = mLocalBinder.getServerInstance();
        }
    };
    protected void onStart() {
        super.onStart();

        Intent mIntent = new Intent(this, ComunicacionService.class);
        bindService(mIntent, mService, BIND_AUTO_CREATE);
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Defino los botones
        //txtEstado = (TextView) findViewById(R.id.txtEstado);
        /*btnActivar = (Button) findViewById(R.id.btnActivar);
        btnEmparejar = (Button) findViewById(R.id.btnEmparejar);
        btnBuscar = (Button) findViewById(R.id.btnBuscar);*/
        btnDatos = (Button) findViewById(R.id.btnDatos);
        btnConfig = (Button) findViewById(R.id.btnConfig);

        // txtComunicacionPaired= (TextView) findViewById(R.id.txtComunicacionPaired);
        //
        //
        txtComunicacionDevice= (TextView) findViewById(R.id.txtComunicacionDevice);

        //Se crea un adaptador para podermanejar el bluethoot del celular
        /*mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Se Crea la ventana de dialogo que indica que se esta buscando dispositivos bluethoot
        mProgressDlg = new ProgressDialog(this);

        mProgressDlg.setMessage("Buscando dispositivos...");
        mProgressDlg.setCancelable(false);

        //se asocia un listener al boton cancelar para la ventana de dialogo ue busca los dispositivos bluethoot
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", btnCancelarDialogListener);

        //se determina si existe bluethoot en el celular
        if (mBluetoothAdapter == null) {
            //si el celular no soporta bluethoot
            showUnsupported();
        } else {
            //si el celular soporta bluethoot, se definen los listener para los botones de la activity
            btnEmparejar.setOnClickListener(btnEmparejarListener);

            btnBuscar.setOnClickListener(btnBuscarListener);*/

        btnDatos.setOnClickListener(btnDatosListener);
        btnConfig.setOnClickListener(btnConfigListener);

            //se determina si esta activado el bluethoot
           /* if (mBluetoothAdapter.isEnabled()) {
                //se informa si esta habilitado
                showEnabled();
            } else {
                //se informa si esta deshabilitado
                showDisabled();
            }
        }*/

        //se definen un broadcastReceiver que captura el broadcast del SO cuando captura los siguientes eventos:
       /* IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //Cambia el estado del Bluethoot (Acrtivado /Desactivado)
        filter.addAction(BluetoothDevice.ACTION_FOUND); //Se encuentra un dispositivo bluethoot al realizar una busqueda
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //Cuando se comienza una busqueda de bluethoot
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //cuando la busqueda de bluethoot finaliza

        //se define (registra) el handler que captura los broadcast anteriormente mencionados.
        registerReceiver(mReceiver, filter);

        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();*/
        txtComunicacionDevice.setText(mService.getDevice().getName());

    }


    @Override
    protected void onStop() {

//        unregisterReceiver(mReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
       /* if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }*/
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
/*

    private void showDisabled() {
        txtEstado.setText("Bluetooth Deshabilitado");
        txtEstado.setTextColor(Color.RED);

        btnActivar.setText("Activar");
        btnActivar.setEnabled(true);

        btnEmparejar.setEnabled(false);
        btnBuscar.setEnabled(false);
    }

    private void showUnsupported() {
        txtEstado.setText("Bluetooth no es soportado por el dispositivo movil");

        btnActivar.setText("Activar");
        btnActivar.setEnabled(false);

        btnEmparejar.setEnabled(false);
        btnBuscar.setEnabled(false);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


*/

    //Metodo que actua como Listener de los eventos que ocurren en los componentes graficos de la activty
    /*private View.OnClickListener btnEmparejarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices == null || pairedDevices.size() == 0) {
                showToast("No se encontraron dispositivos emparejados");
            } else {
                ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

                list.addAll(pairedDevices);

                Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);

                intent.putParcelableArrayListExtra("device.list", list);

                startActivity(intent);
            }
        }
    };

    private View.OnClickListener btnBuscarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mBluetoothAdapter.startDiscovery();
        }
    };*/


    private View.OnClickListener btnDatosListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /*if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();

                showDisabled();
            } else {*/
                Intent intent = new Intent(MainActivity.this, DatosActivity.class);

                startActivity(intent);
            //}
        }
    };


    private View.OnClickListener btnConfigListener = new View.OnClickListener() {
        @Override
        public void onClick(View b) {
            /*dialog.dismiss();

            mBluetoothAdapter.cancelDiscovery();*/
            Intent intent = new Intent(MainActivity.this, ConfigActivity.class);

            startActivity(intent);
        }
    };
}
