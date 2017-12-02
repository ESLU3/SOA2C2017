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
import android.widget.Button;
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

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private InputStream mmInStream = null;
    private OutputStream mmOutStream = null;



    // SPP UUID service
    private final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // variable donde se guardará la direccion MAC del HC06 del Arduino
    private String address = "00:12:08:09:28:06";

    private final IBinder binder=new LocalBinder();

    public void onCreate()
    {
        Toast.makeText(this,"Servicio creado", Toast.LENGTH_SHORT).show();
        Log.d("arduino", "servicio iniciado");
        btAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    @Override
    //Despues de onCreate automaticamente se ejecuta a OnstartCommand,
    //idArranque: Es el id del servicio a ejecutar
    public int onStartCommand(Intent intenc, int flags, int idArranque)
    {
        Toast.makeText(this,"Servicio arrancado "+ idArranque,Toast.LENGTH_SHORT).show();

        return flags;
    }

    @Override
    //On destroy se invoca cuando se ejcuta stopService
    public void onDestroy()
    {
        Toast.makeText(this,"Servicio detenido",Toast.LENGTH_SHORT).show();
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
                            Log.d("arduino", "service conectado al arduino");
                        }

                        } catch(InterruptedException e){
                            e.printStackTrace();
                        }
                            InputStream tmpIn = null;
                            OutputStream tmpOut = null;

                        try
                        {
                            //Create I/O streams for connection
                            tmpIn = btSocket.getInputStream();
                            tmpOut = btSocket.getOutputStream();
                        } catch (IOException e) { }

                        mmInStream = tmpIn;
                        mmOutStream = tmpOut;
                    }
            }


        }).start();

    }

    public String read() {
        byte[] msgBuffer = new byte[256];           //converts entered String into bytes
        int bytes;
        String readMessage = null;
        try {
            bytes = mmInStream.read(msgBuffer);
            readMessage = new String(msgBuffer, 0, bytes);
            msgBuffer = new byte[256];

        } catch (IOException e) {   }
        return readMessage;
    }

    //write method
    public void write(String input) {
        byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
        try {
            mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
        } catch (IOException e) {  }
    }



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
    /*public BluetoothDevice getDevice(){
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
    }*/


}
