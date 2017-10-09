/*********************************************************************************************************
 * Activity Inicial de la App que permite al usuario ingresar la IP y puerto del Servidor.
 * Contiene el codigo del front-end de la aplicacion
 **********************************************************************************************************/

package com.example.esteban.android_http;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//Clase de la activty Inicial
public class InicioActivity extends AppCompatActivity {

    //Elementos graficos de la Activity
    Button btnOk;
    TextView txtIpServidor,txtPuertoServidor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        //Se asoscia cada componente de la activity a una variable
        btnOk =(Button)findViewById(R.id.btnOK);
        txtIpServidor=(TextView)findViewById(R.id.txtIpServidor);
        txtPuertoServidor=(TextView)findViewById(R.id.txtPuertoServidor);

        //Se establece el handlers para el boton OK
        btnOk.setOnClickListener(botonesListeners);
    }


    //Metodo que es llamada cuando se cierra la activity
    protected void onDestroy()
    {
        super.onDestroy();
        //Toast.makeText(getApplicationContext(),"Cerrando Inicio",Toast.LENGTH_LONG).show();
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
               case R.id.btnOK:
                   //se genera un Intent para poder lanzar la activity principal
                   intent=new Intent(InicioActivity.this,PrincipalActivity.class);

                   //Se le agrega al intent los parametros que se le quieren pasar a la activyt principal
                   //cuando se lanzado
                   intent.putExtra("ipServidor",txtIpServidor.getText().toString());
                   intent.putExtra("puertoServidor",txtPuertoServidor.getText().toString());

                   //se inicia la activity principal
                   startActivity(intent);

                   //se cierra la acitvity de Inicio
                   finish();

                   break;
               default:
                Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_LONG).show();
            }


        }
    };



}

