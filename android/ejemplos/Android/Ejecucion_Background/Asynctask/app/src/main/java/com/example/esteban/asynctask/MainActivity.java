package com.example.esteban.asynctask;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnBoton;
    private EditText txtTextView;
    private Integer idThread=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBoton = (Button) findViewById(R.id.btnBoton);
        txtTextView= (EditText) findViewById(R.id.txttTextView);

        btnBoton.setOnClickListener(this);

    }
    public void onClick(View v)
    {
        if(v==btnBoton)
        {
            ejecutarThread();
        }
    }

    private void  ejecutarThread() {
        ThreadAsynctask hilo=new ThreadAsynctask();

        //Si se ejecuta con execute, se ejecuta solo un Thread secundario a la vez.
        //O sea si se crean varios Thread secundarios, no se van a ejecutar en paralelo,
        //Sino secuencialmente.
        //
        //hilo.execute("id Thread");

        //Si se ejecuta con executeOnExecutor, se ejecuta varios Threads secundarios a la vez.
        //O sea si se crean varios Thread secundarios, se van a poder ejecutar en paralelo,
        //
        //NOTA: La cantidad maxima de Thread secundarios que se podrn ejecutar en paralelo depende
        //      de la version del S.O Androdi instalada
        hilo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"id Thread");

    }
    private class ThreadAsynctask extends AsyncTask<String, Integer, String>{

        @Override
        //Metodo que se ejecuta antes de ejecutar el Thread secundario, ejecutandose en
        //en Thread correspondiente a la interfaz grafica. Por eso puede modificar la Interfaz grafica
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(),"Antes de Ejcutar Thread",Toast.LENGTH_SHORT).show();
        }


        @Override
        //Metodo que se ejecuta en el Thred secundario. Aca se ejecuta el Thread creado.
        //Por eso en este metodo no se perimite modificar DIRECTAMENTE a la Interfaz GUI, dado que puede fallar el programa
        // si se lo realiza desde este lugar.
        protected String doInBackground(String... params) {
            Integer i=0;

            idThread++;

            //Con publishProgress se invoca al metodo onProgressUpdate para modificar INDIRECTAMENTE  a la interfaz GUI
            publishProgress(idThread);

            for (i=0;i<10000000;i++);

            String mensaje =params[0]+idThread;
            return mensaje;
        }

        @Override
        //Metodo que se ejecuta al finalizar la ejecucion del Thread secundario, ejecutandose en
        //en Thread correspondiente a la interfaz grafica. Por eso puede modificar la Interfaz grafica
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(),"Finalizando"+result.toString(),Toast.LENGTH_SHORT).show();
            txtTextView.setText(result);
        }


        @Override
        //Metodo que se permite modificar Interfaz GUI durante la ejecucion del metodo doInBackground,
        //O sea durante la ejecucion del Thread secundario.
        //Este metodo se ejecuta en el Thread correspondiente a la interfaz grafica. Por eso puede modificar la Interfaz grafica
        protected void onProgressUpdate(Integer ...values) {
            Toast.makeText(getApplicationContext(),"Ejecutando Thread Numero:"+values[0].toString(),Toast.LENGTH_SHORT).show();
        }

    }

}



