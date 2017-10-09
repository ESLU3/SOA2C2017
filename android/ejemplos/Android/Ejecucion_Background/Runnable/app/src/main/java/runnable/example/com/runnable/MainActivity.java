package runnable.example.com.runnable;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements  View.OnClickListener{

    private Button btnBoton;
    private TextView txtTextView;
    private Integer idThread=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBoton = (Button) findViewById(R.id.btnBoton);
        txtTextView= (TextView) findViewById(R.id.txttextView);

        btnBoton.setOnClickListener(this);

    }
    public void onClick(View v)
    {
        if(v==btnBoton)
        {
            ejecutarThread();
        }
    }

    private void  ejecutarThread()
    {
        final Handler mHandler = new Handler();

        //Se crea un Thread implementado la interfaz Runnable para poder interactuar con este
        new Thread(new Runnable() {
            @Override
            //El metodo run se utiliza debido a que se debe implentar al utilizar la interfaz Runnable
            public void run () {
                // Perform long-running task here
                // (like audio buffering).
                // you may want to update some progress
                // bar every second, so use handler:

                //Aca, en esta seccion, se ejecuta el thread secundario creado
                idThread++;

                //Se utiliza un Handler que permite comunicar al thread creado con la cola de mensjes del hilo
                //principal de la interfaz grafica, internamente mediante un looper.
                //En otras palabras mediante un Handler el Thread seundario puede modifcar la interfaz grafica
                Runnable refresh = new Runnable() {
                    @Override
                    public void run()
                    {
                        // make operation on UI - on example
                        // on progress bar.

                        Toast.makeText(getApplicationContext(),"Ejecutando Thread Numero:"+idThread.toString(),Toast.LENGTH_LONG).show();
                        txtTextView.setText("Id Thread:"+idThread.toString());
                    }
                };
                //Al utilizar desde el Thread secundario el post del Handler , se invocara automaticamente el
                //  metodo run de la intefaz Runnable reefresh. El cual permite modificar la Interfaz GUI desde dicho metodo.
                //NOTA: Lo que se ejecuta en el ambito de post, No se ejecuta en el thread secundario,
                //      sino en la interfaz grafica
                mHandler.post(refresh);

                Log.i("INFO","EjecutaNDO THREAD!!!!!!!!!!!!!!!");
                //Ejecutando una espera activa por cada thread creado para comprobar que la Interfaz grafica no se congela,
                //sigue activa. Continua respondiendo a los eventos de los usuarios
                while(true);
            }
        }).start();
    }


}



