package ar.com.secador_smart;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Binder;
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
import java.lang.reflect.Method;
import java.util.UUID;

/*********************************************************************************************************
 * Activity que muestra realiza la comunicacion con Arduino
 **********************************************************************************************************/

//******************************************** Hilo principal del Activity**************************************
public class ComunicacionService extends Service implements ServiceConnection {
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
    //private ConnectedThread mConnectedThread;

    // SPP UUID service
    private final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // variable donde se guardará la direccion MAC del HC06 del Arduino
    private String address = "00:12:08:09:28:06";

    private final IBinder binder=new LocalBinder();

    public void onCreate()
    {
        Toast.makeText(this,"Servicio creado", Toast.LENGTH_SHORT).show();
        //reproductor.setLooping(true);
        Log.d("arduino", "servicio iniciado");
        btAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    @Override
    //Despues de onCreate automaticamente se ejecuta a OnstartCommand,
    //idArranque: Es el id del servicio a ejecutar
    public int onStartCommand(Intent intenc, int flags, int idArranque)
    {
        Toast.makeText(this,"Servicio arrancado "+ idArranque,Toast.LENGTH_SHORT).show();

       /* new Thread(new Runnable() {
            public void run() {

                while (true) {
                    try {
                        Thread.sleep(500);
                        if (btSocket == null) {
                            BluetoothDevice device = btAdapter.getRemoteDevice(address);

                            //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
                            try {
                                btSocket = createBluetoothSocket(device);
                            } catch (IOException e) {
                                //showToast("La creacción del Socket fallo");
                            }
                            // Establish the Bluetooth socket connection.
                            try {
                                btSocket.connect();
                            } catch (IOException e) {
                                try {
                                    btSocket.close();
                                } catch (IOException e2) {
                                    //insert code to deal with this
                                }
                            }
                            // showToast("service conectado al arduino");
                            Log.d("arduino", "service conectado al arduino");
                        }

                    } catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }


        }).start();*/

        return flags;
    }

    @Override
    //On destroy se invoca cuando se ejcuta stopService
    public void onDestroy()
    {
        Toast.makeText(this,"Servicio detenido",Toast.LENGTH_SHORT).show();
        //reproductor.stop();
        try {
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    public ComunicacionService() {
        //hilo para la conexion existente

        new Thread(new Runnable() {
            public void run() {

                while (true) {
                    try {
                        Thread.sleep(500);
                        if (btSocket == null) {
                            BluetoothDevice device = btAdapter.getRemoteDevice(address);

                            //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
                            try {
                                btSocket = createBluetoothSocket(device);
                            } catch (IOException e) {
                                //showToast("La creacción del Socket fallo");
                            }
                            // Establish the Bluetooth socket connection.
                            try {
                                btSocket.connect();
                            } catch (IOException e) {
                                try {
                                    btSocket.close();
                                } catch (IOException e2) {
                                    //insert code to deal with this
                                }
                            }
                           // showToast("service conectado al arduino");
                            Log.d("arduino", "service conectado al arduino");
                        }

                        } catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
            }


        }).start();
    }
    //Handler que captura los brodacast que emite el SO al ocurrir los eventos del bluethoot
   /* private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            //Atraves del Intent obtengo el evento de Bluethoot que informo el broadcast del SO
            String action = intent.getAction();

            //Si cambio de estado el Bluethoot(Activado/desactivado)
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                //Obtengo el parametro, aplicando un Bundle, que me indica el estado del Bluethoot
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                //Si esta activado
                if (state == BluetoothAdapter.STATE_ON) {
                    showToast("Activar");
                    Log.d("blut", "active blut");

                    showEnabled();
                }
            }
            //Si se inicio la busqueda de dispositivos bluethoot
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //Creo la lista donde voy a mostrar los dispositivos encontrados
                mDeviceList = new ArrayList<BluetoothDevice>();
                Log.d("blut", "empiezo busqueda");
                //muestro el cuadro de dialogo de busqueda
                mProgressDlg.show();
            }
            //Si finalizo la busqueda de dispositivos bluethoot
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //se cierra el cuadro de dialogo de busqueda
                mProgressDlg.dismiss();

                //se inicia el activity DeviceListActivity pasandole como parametros, por intent,
                //el listado de dispositivos encontrados
                Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);

                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
                Log.d("blut", "termine busqueda");

                startActivity(newIntent);
            }
            //si se encontro un dispositivo bluethoot
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Se lo agregan sus datos a una lista de dispositivos encontrados
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("blut", "encontré un dispositivo" + device.getName());
                mDeviceList.add(device);
                showToast("Dispositivo Encontrado:" + device.getName());
            }
        }
    };*/


        public IBinder onBind(Intent intent) {

            return binder;
        }
        // Metodo que escucha el cambio de sensibilidad de los sensores


        public class LocalBinder extends Binder {
            public ComunicacionService getService() {
                return ComunicacionService.this;
            }
        }


        //Metodo que crea el socket bluethoot
        private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

            return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        }




    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
    //metodo que llamaran los activities para enviar info
    public BluetoothDevice getDevice(){
        return btAdapter.getRemoteDevice(address);
    }

    public BluetoothSocket getBtSocket(){
        return btSocket;
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
