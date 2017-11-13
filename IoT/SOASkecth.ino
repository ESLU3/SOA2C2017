#include "DHT.h"
#define DHTTYPE DHT22
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
//                    addr, en,rw,rs,d4,d5,d6,d7,bl,blpol
LiquidCrystal_I2C lcd(0x27, 2, 1, 0, 4, 5, 6, 7, 3, POSITIVE);  // 0x27 es la direccion del i2c del lcd

const int pinLuz = A1;          //Pin del LDR
const int pinLed = 3;           //Pin del Led
const int pinDHT = 5;           //Pin del DHT
DHT sensorDeHumedad(pinDHT, DHTTYPE);
const int pinTemperatura = A0;  //Pin del lm35
const int pinFan = 12;


int iluminacion;
bool procesoTerminado = false;
int contador = 0;

void setup() {
  Serial.begin(9600);
  lcd.begin(20, 4);        // 4 lineas con 20 caracteres cada una
  sensorDeHumedad.begin();
  pinMode(pinLed , OUTPUT);
  pinMode(pinLuz, INPUT);
  lcd.clear();
}

void loop() {

  //delay(3000);
  float humedad = leerHumedad();
  float temperatura = (5.0 * analogRead(A0) * 100.0) / 1024;
  iluminacion = analogRead(pinLuz);

  if (procesoTerminado == false && contador != 1000) {
    lcd.clear();
    lcd.setCursor(1, 0); // 3 caracter, linea 0
    lcd.print("Humedad: " );
    lcd.setCursor(14, 0);
    lcd.print(humedad);
    lcd.setCursor(1, 1); // 3 caracter, linea 0
    lcd.print("Temperatura: " );
    lcd.setCursor(14, 1);
    lcd.print(temperatura);
    lcd.setCursor(1, 2); // 3 caracter, linea 0
    lcd.print("Iluminacion: " );
    lcd.setCursor(14, 2);
    lcd.print(iluminacion);
  }
  if ( humedad >= 75 ) {
    procesoTerminado = false;
    analogWrite(pinFan, 255);                    // Encender el Ventilador a velocidad maxima
    Serial.print("falta el cal");                // Encender el Calentador
  } else {
    if (temperatura < 20) {
      procesoTerminado = false;
      analogWrite(pinFan, 255);                  // Encender el Ventilador a velocidad maxima
    }
    else {
      if (temperatura >= 20 && humedad <= 75) {  // Esta para describir bien el proceso
        procesoTerminado = false;
        analogWrite(pinFan, 150);                // Encender el Ventilador a velocidad media
      }
    }
  }
  if (humedad <= 30 && temperatura >= 22 && iluminacion >= 120) {   // Falta terminar el secado pero no es necesario continuar con el calentador encendido
    procesoTerminado = false;
    Serial.print("falta el cal");                // Detener el calentador
  }

  if (humedad <= 20) {                            // Se termina el secado
    analogWrite(pinFan, 0);                      // Detener el ventilador
    Serial.print("falta el cal");                // Detener el Calentador
    digitalWrite(pinLed, HIGH);                  // Encender LED indicando que finalizo el proceso


    for (int i = 0; i < 3; i++)                  // Blink al lcd
    {
      lcd.backlight();
      delay(250);
      lcd.noBacklight();
      delay(250);
    }
    lcd.backlight();
    lcd.clear();
    lcd.setCursor(2, 1); // 3 caracter, linea 0
    lcd.print("Proceso de Secado");
    lcd.setCursor(4, 2); // 3 caracter, linea 0
    lcd.print("COMPLETADO");
    procesoTerminado = true;
  }

  if (contador != 1000) {
    contador++;
  } else
  {
    contador = 0;
  }
  
  
  //probarVentilador();
  //probarHumedad();
  //probarTemperatura();
 // probarLed();
  //probarLuz();

}

void probarLuz() {
  delay(2000);
  iluminacion = analogRead(pinLuz);
  Serial.print("El valor de la iluminacion es: ");
  Serial.print(iluminacion);
  Serial.println();
}

void probarLed() {
  digitalWrite(pinLed  , HIGH);   // poner el Pin en HIGH
  delay(1000);                   // esperar un segundo
  digitalWrite(pinLed , LOW);    // poner el Pin en LOW
  delay(1000);

}

void probarHumedad() {
  delay(2000);
  float humedad = leerHumedad();
  float temperatura = leerTemperatura();
  mostrarHumedadYTemperatura(humedad, temperatura);
}

void probarTemperatura() {
  float  temperatura = (5.0 * analogRead(A0) * 100.0) / 1024;
  //se multiplica por 5 y divide por 1024 por la precision del sensor, y se multiplica por 100 para pasar de voltaje a grados C (1°C son 0,01V)
  Serial.print(temperatura);
  Serial.println();
  delay(2000);
}

float leerHumedad() {
  return sensorDeHumedad.readHumidity();// Lee la humedad desde el sensor DHT22
}

float leerTemperatura() {
  return sensorDeHumedad.readTemperature();//Lee la temperatura desde el sensor DHT22
}
void mostrarHumedadYTemperatura(float humedad, float temperatura) {
  //////////////////////////////////////////////////Humedad
  Serial.print("Humedad Relativa: ");
  Serial.print(humedad);//Escribe la humedad
  Serial.println(" %");
  ///////////////////////////////////////////////////Temperatura
  Serial.print("Temperatura: ");
  Serial.print(temperatura);//Escribe la temperatura
  Serial.println(" C'");
}

void encenderVentilador(int pinFan, int velocidad) {
  analogWrite(pinFan, velocidad);
}

void probarVentilador() {
  encenderVentilador(pinFan, 255);
  delay(5000);
  encenderVentilador(pinFan, 0);
  delay(5000);
}


