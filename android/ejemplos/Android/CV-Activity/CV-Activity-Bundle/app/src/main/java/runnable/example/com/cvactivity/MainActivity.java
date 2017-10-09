package runnable.example.com.cvactivity;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Button btnEnviar;
    private EditText txtOrigen;

    private String texto="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtOrigen=(EditText)findViewById(R.id.txtOrigen);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);
        btnEnviar.setOnClickListener(botonesListeners);
        Log.i("Ejecuto","Ejecuto onCreate");

    }



    @Override
    protected void onStart()
    {
        Log.i("Ejecuto","Ejecuto Onstart");
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        Log.i("Ejecuto","Ejecuto OnResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i("Ejecuto","Ejecuto OnPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Ejecuto","Ejecuto OnStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.i("Ejecuto","Ejecuto OnRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Ejecuto","Ejecuto OnDestroy");
    }

    //Metodo que actua como Listener de los eventos que ocurren en los componentes graficos de la activty
    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {

        public void onClick(View v)
        {
            Intent intent;

            //Se determina que componente genero un evento
            switch (v.getId())
            {
                //Si se ocurrio un evento en el boton OK
                case R.id.btnEnviar:
                    //se genera un Intent para poder lanzar la activity principal
                    intent=new Intent(MainActivity.this,DialogActivity.class);

                    //Se le agrega al intent los parametros que se le quieren pasar a la activyt principal
                    //cuando se lanzado
                    intent.putExtra("textoOrigen",txtOrigen.getText().toString());

                    //se inicia la activity principal
                    startActivity(intent);


                    break;
                default:
                    Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_LONG).show();
            }


        }
    };

}

