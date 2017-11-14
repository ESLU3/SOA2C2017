package es.epinanab.sensores;

import java.text.DecimalFormat;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener
{
  private SensorManager mSensorManager;
  private TextView      acelerometro;
  private TextView      proximity;
  private TextView      luminosidad;
  private TextView      detecta;
  private static final int SHAKE_THRESHOLD = 800;
  private static float last_x = 0;
  private static float last_z = 0;
  private static float last_y = 0;
  DecimalFormat         dosdecimales = new DecimalFormat("###.###");

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Defino los botones
    Button sensores = (Button) findViewById(R.id.listado);
    Button limpia   = (Button) findViewById(R.id.limpia);

    // Defino los TXT para representar los datos de los sensores
    acelerometro  = (TextView) findViewById(R.id.acelerometro);
    proximity     = (TextView) findViewById(R.id.proximity);
    luminosidad   = (TextView) findViewById(R.id.luminosidad);

    // Accedemos al servicio de sensores
    mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    // Boton que muestra el listado de los sensores disponibles
    sensores.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {

        Intent i = new Intent();
        i.setClass(MainActivity.this, ListaSensoresActivity.class);

        startActivity(i);
      }
    });

    // Limpio el texto de la deteccion
    limpia.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        detecta.setText("");
        detecta.setBackgroundColor(Color.parseColor("#000000"));
      }
    });
  }

  // Metodo para iniciar el acceso a los sensores
  protected void Ini_Sensores()
  {
    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),   SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),       SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),           SensorManager.SENSOR_DELAY_NORMAL);
  }

  // Metodo para parar la escucha de los sensores
  private void Parar_Sensores()
  {

    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
  }

  // Metodo que escucha el cambio de sensibilidad de los sensores
  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy)
  {

  }

  // Metodo que escucha el cambio de los sensores
  @Override
  public void onSensorChanged(SensorEvent event)
  {

    synchronized (this)
    {
      Log.d("sensor", event.sensor.getName());

      switch(event.sensor.getType())
      {
         case Sensor.TYPE_ACCELEROMETER :
            long curTime = System.currentTimeMillis();
            long lastUpdate = 0;
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    Log.d("sensor", "shake detected w/ speed: " + speed);
                }
                 last_x = x;
                 last_y = y;
                 last_z = z;
            }
        break;

        case Sensor.TYPE_PROXIMITY :
          //txt += "proximity\n";
          //txt += event.values[0] + "\n";

         // proximity.setText(txt);

          // Si detecta 0 lo represento
          if( event.values[0] < 100 )
          {
            detecta.setBackgroundColor(Color.parseColor("#cf091c"));
            detecta.setText("Proximidad Detectada");
          }
        break;

        case Sensor.TYPE_LIGHT :
         // txt += "Luminosidad\n";
         // txt += event.values[0] + " Lux \n";

         // luminosidad.setText(txt);
        break;
      }
    }
  }

  @Override
  protected void onStop()
  {

    Parar_Sensores();

    super.onStop();
  }

  @Override
  protected void onDestroy()
  {
    Parar_Sensores();

    super.onDestroy();
  }

  @Override
  protected void onPause()
  {
    Parar_Sensores();

    super.onPause();
  }

  @Override
  protected void onRestart()
  {
    Ini_Sensores();

    super.onRestart();
  }

  @Override
  protected void onResume()
  {
    super.onResume();

    Ini_Sensores();
  }

}
