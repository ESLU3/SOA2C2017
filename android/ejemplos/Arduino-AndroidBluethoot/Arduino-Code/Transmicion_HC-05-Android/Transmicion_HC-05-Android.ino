#include <SoftwareSerial.h>

//Al utilizar la biblioteca SoftwareSerial los pines RX y TX para la transmicion serie de Bluethoot se pueden cambiar mapear a otros pines.
//Sino se utiliza esta bibioteca esto no se puede realizar y se debera conectar al pin 0 y 1, conexion Serie no pudiendo imprmir por el monitor serie
//Al estar estos ocupados.
SoftwareSerial BTserial(10,11); // RX | TX

char c = ' ';

int pinLed= 3;

void setup()
{
    //Se configura la velocidad del puerto serie para poder imprimir en el puerto Serie
    Serial.begin(9600);
    Serial.println("Inicializando configuracion del HC-05...");
  
    //Se configura la velocidad de transferencia de datos entre el Bluethoot  HC05 y el de Android.
    BTserial.begin(9600); 
    Serial.println("Esperando Comandos AT...");

    pinMode(pinLed, OUTPUT);
}
 
void loop()
{
   //sI reciben datos del HC05 
    if (BTserial.available())
    { 
        //se los lee y se los muestra en el monitor serie
        c = BTserial.read();
        //Serial.write("leyendoAA:");
        Serial.write(c);
        analizarDato(c);
    }

    //Si se ingresa datos por teclado en el monitor serie 
    if (Serial.available())
    {
        //se los lee y se los envia al HC05
        c =  Serial.read();
        Serial.write(c);
        BTserial.write(c); 
    }
}

void analizarDato(char c)
{
  //Serial.write("leyendo");
   if(c=='1')
   {
      Serial.println("Encender Led");
      digitalWrite(pinLed, HIGH);  
   }
   else if(c=='2')
   {
      Serial.println("Apagar Led");
      digitalWrite(pinLed, LOW);   // turn the LED on (HIGH is the voltage level)
   }
}



