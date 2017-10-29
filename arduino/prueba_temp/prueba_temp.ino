#include "DHT.h"
#define DHTPIN 7
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);

//float temp; //variable de temperatura
int valorLDR = 0;  //valor para LDR
int pinLed2 = 3;

//int fanControl = A0;
int fanSense = 12;
unsigned long pulseDuration;

void setup() {
  // put your setup code here, to run once:
  //  pinMode(A0, INPUT); // Tell Arduino to make its Analog A0 pin as Input reading pin
   // pinMode(A1, INPUT); //pin para LDR (sensor luminico)
    //pinMode(pinLed2, OUTPUT); //led salida
  // start the serial connection at 9600 bps:
   Serial.begin(9600);
   //pinMode(fanControl, OUTPUT);
   pinMode(fanSense, INPUT);
  digitalWrite(fanSense,LOW);
   //dht.begin();
}

void readPulse() {
pulseDuration = pulseIn(fanSense, LOW);
double frequency = 1000000/pulseDuration;

Serial.print("pulse duration:");
Serial.println(pulseDuration);

Serial.print("time for full rev. (microsec.):");
Serial.println(pulseDuration*2);
Serial.print("freq. (Hz):");
Serial.println(frequency/2);
Serial.print("RPM:");
Serial.println(frequency/2*60);

}

void loop() {
  // put your main code here, to run repeatedly:
 //  temp = (5.0 * analogRead(A0) * 100.0) / 1024; 
   //se multiplica por 5 y divide por 1024 por la precision del sensor, y se multiplica por 100 para pasar de voltaje a grados C (1°C son 0,01V)
    /*digitalWrite(pinLed2, LOW); //apaga el led al comienzo del loop
    valorLDR= analogRead(A1);
    int h = dht.readHumidity();// Lee la humedad
    int t = dht.readTemperature();//Lee la temperatura
    //////////////////////////////////////////////////Humedad
    Serial.print("Humedad Relativa: ");                
    Serial.print(h);//Escribe la humedad
    Serial.println(" %");                    
    ///////////////////////////////////////////////////Temperatura              
    Serial.print("Temperatura: ");                  
    Serial.print(t);//Escribe la temperatura
    Serial.println(" C'");                  

    Serial.print("luz is: "); //println prints next thing on a new line
    Serial.println(valorLDR);// Prints current luz on Monitor
    Serial.println(" "); //Break space // Start reading on new line
    if (t > 25) {
      Serial.println("la temperatura esta alta papá");
      digitalWrite(pinLed2, HIGH);
      
    }
     if (t < 10) {
      //se prende ventilador
    }
    if(valorLDR < 100)
    {
      Serial.println("es de noche");
    }
    /*if(valorLDR > 512)
    {
      Serial.println("valor de LDR es mayor a 512");
    }
    if(valorLDR > 768)
    {
      Serial.println("valor de LDR es mayor a 768");
    }*/
  //  digitalWrite(fanSense, LOW); 
    analogWrite(fanSense, 255); 
    readPulse();
     delay(5000); // 3 seconds delay before taking the new reading
     analogWrite(fanSense, 1000); 
     readPulse();
     delay(5000); // 3 seconds delay before taking the new reading
    analogWrite(fanSense, 255);
    readPulse();
    delay(5000);
    analogWrite(fanSense, 100);
    readPulse();
    delay(5000);

}
