package runnable.example.com.cv_activity_estado;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    String texto = null;
    EditText txtEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtEditText= (EditText) findViewById(R.id.txtEditText);

        Toast.makeText(this, "onCreate - Texto: " + texto, Toast.LENGTH_LONG).show();

    }


    @Override
    public void  onSaveInstanceState(Bundle savedInstanceState)
    {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);

        // Save the user's current game state
        texto= txtEditText.getText().toString();
        savedInstanceState.putString("valorTxtEditText", texto);

        Toast.makeText(this, "Texto guardado " + texto, Toast.LENGTH_LONG).show();

    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        texto = savedInstanceState.getString("valorTxtEditText");

        Toast.makeText(this, "Texto recuperado " + texto, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Toast.makeText(this, "onStart - Texto:" + texto, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Toast.makeText(this, "onResume - Texto:" + texto, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        Toast.makeText(this, "onPause - Texto:" + texto, Toast.LENGTH_LONG).show();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(this, "onStop- Texto:" + texto, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Toast.makeText(this, "onRestart- Texto:" + texto, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "onDestroy - Texto:" + texto, Toast.LENGTH_LONG).show();
    }


}

