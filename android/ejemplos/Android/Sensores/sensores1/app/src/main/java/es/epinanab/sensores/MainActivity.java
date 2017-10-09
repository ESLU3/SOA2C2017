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
  private TextView      giroscopo;
  private TextView      orientacion;
  private TextView      magnetic;
  private TextView      proximity;
  private TextView      luminosidad;
  private TextView      temperatura;
  private TextView      gravedad;
  private TextView      detecta;
  private TextView      giro;
  private TextView      presion;

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
    giroscopo     = (TextView) findViewById(R.id.giroscopo);
    orientacion   = (TextView) findViewById(R.id.orientacion);
    magnetic      = (TextView) findViewById(R.id.magnetic);
    proximity     = (TextView) findViewById(R.id.proximity);
    luminosidad   = (TextView) findViewById(R.id.luminosidad);
    temperatura   = (TextView) findViewById(R.id.temperatura);
    gravedad      = (TextView) findViewById(R.id.gravedad);
    detecta       = (TextView) findViewById(R.id.detecta);
    giro          = (TextView) findViewById(R.id.giro);
    presion       = (TextView) findViewById(R.id.presion);

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
    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),       SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),     SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),  SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),       SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),           SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE),     SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),         SensorManager.SENSOR_DELAY_NORMAL);
    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),        SensorManager.SENSOR_DELAY_NORMAL);
  }

  // Metodo para parar la escucha de los sensores
  private void Parar_Sensores()
  {

    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE));
    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE));
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
    String txt = "";

    // Cada sensor puede lanzar un thread que pase por aqui
    // Para asegurarnos ante los accesos simult�neos sincronizamos esto

    synchronized (this)
    {
      Log.d("sensor", event.sensor.getName());

      switch(event.sensor.getType())
      {
        case Sensor.TYPE_ORIENTATION :
          txt += "orientation\n";
          txt += "azimut: " + getDireccion(event.values[0]) + "\n";
          txt += "y: " + event.values[1] + "\n";
          txt += "z: " + event.values[2] + "\n";
          orientacion.setText(txt);
        break;

        case Sensor.TYPE_ACCELEROMETER :
          txt += "acelerometro\n";
          txt += "x: " + dosdecimales.format(event.values[0]) + " m/seg2 \n";
          txt += "y: " + dosdecimales.format(event.values[1]) + " m/seg2 \n";
          txt += "z: " + dosdecimales.format(event.values[2]) + " m/seg2 \n";
          acelerometro.setText(txt);

          if ((event.values[0] > 15) || (event.values[1] > 15) || (event.values[2] > 15))
          {
            detecta.setBackgroundColor(Color.parseColor("#cf091c"));
            detecta.setText("Vibracion Detectada");
          }
        break;

        case Sensor.TYPE_GYROSCOPE:
          txt += "giroscopo\n";
          txt += "x: " + dosdecimales.format(event.values[0]) + " deg/s \n";
          txt += "y: " + dosdecimales.format(event.values[1]) + " deg/s \n";
          txt += "z: " + dosdecimales.format(event.values[2]) + " deg/s \n";
          giroscopo.setText(txt);
        break;

        case Sensor.TYPE_ROTATION_VECTOR :
          txt += "rotation vector\n";
          txt += "x: " + event.values[0] + "\n";
          txt += "y: " + event.values[1] + "\n";
          txt += "z: " + event.values[2] + "\n";

          // Creo objeto para saber como esta la pantalla
          Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
          int rotation = display.getRotation();

          // El objeto devuelve 3 estados 0, 1 y 3
          if( rotation == 0 )
          {
            txt += "Pos: Vertical \n";

          }
          else if( rotation == 1 )
          {
            txt += "Pos: Horizontal Izq. \n";

          }
          else if (rotation == 3)
          {
            txt += "Pos: Horizontal Der \n";
          }

          txt += "display: " + rotation + "\n";

          giro.setText(txt);

        break;

        case Sensor.TYPE_GRAVITY :
          txt += "Gravedad\n";
          txt += "x: " + event.values[0] + "\n";
          txt += "y: " + event.values[1] + "\n";
          txt += "z: " + event.values[2] + "\n";

          gravedad.setText(txt);
        break;

        case Sensor.TYPE_MAGNETIC_FIELD :
          txt += "magnetic field\n";
          txt += event.values[0] + " uT" + "\n";

          magnetic.setText(txt);
        break;

        case Sensor.TYPE_PROXIMITY :
          txt += "proximity\n";
          txt += event.values[0] + "\n";

          proximity.setText(txt);

          // Si detecta 0 lo represento
          if( event.values[0] == 0 )
          {
            detecta.setBackgroundColor(Color.parseColor("#cf091c"));
            detecta.setText("Proximidad Detectada");
          }
        break;

        case Sensor.TYPE_LIGHT :
          txt += "Luminosidad\n";
          txt += event.values[0] + " Lux \n";

          luminosidad.setText(txt);
        break;

        case Sensor.TYPE_PRESSURE :
          txt += "Presion\n";
          txt += event.values[0] + " mBar \n";

          presion.setText(txt);
        break;

        case Sensor.TYPE_TEMPERATURE :
          txt += "Temperatura\n";
          txt += event.values[0] + " C \n";

          temperatura.setText(txt);
        break;
      }
    }
  }

  private String getDireccion(float values)
  {
    String txtDirection = "";
    if (values < 22)
      txtDirection = "N";
    else if (values >= 22 && values < 67)
      txtDirection = "NE";
    else if (values >= 67 && values < 112)
      txtDirection = "E";
    else if (values >= 112 && values < 157)
      txtDirection = "SE";
    else if (values >= 157 && values < 202)
      txtDirection = "S";
    else if (values >= 202 && values < 247)
      txtDirection = "SO";
    else if (values >= 247 && values < 292)
      txtDirection = "O";
    else if (values >= 292 && values < 337)
      txtDirection = "NO";
    else if (values >= 337)
      txtDirection = "N";

    return txtDirection;
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
