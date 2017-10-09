package serviciomusica.example.com.serviciomusica;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    Button btnArrancar;
    Button btnDetener;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnArrancar = (Button) findViewById(R.id.boton_arrancar);
        btnDetener = (Button) findViewById(R.id.boton_detener);

        btnArrancar.setOnClickListener(botonesListeners);
        btnDetener.setOnClickListener(botonesListeners);

    }

    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {

        public void onClick(View v)
        {

            Intent intent=new Intent(MainActivity.this, ServicioMusica.class);

            //Se determina que componente genero un evento
            switch (v.getId())
            {
                //Si se presiono el Boton Encender
                case R.id.boton_arrancar:
                    //Inicio el servicio
                    startService(intent);
                    break;

                //Si se presiono el Boton Apagar
                case R.id.boton_detener:
                    //detengo el servicio
                    stopService(intent);
                    break;

                default:
                    Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_SHORT).show();
            }


        }
    };

}
