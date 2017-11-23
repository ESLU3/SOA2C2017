#include "DHT.h"
#define DHTTYPE DHT22
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <SoftwareSerial.h>
SoftwareSerial bluetooth(10, 11); // RX, TX
//                    addr, en,rw,rs,d4,d5,d6,d7,bl,blpol
LiquidCrystal_I2C lcd(0x27, 2, 1, 0, 4, 5, 6, 7, 3, POSITIVE);  // 0x27 es la direccion del i2c del lcd

const int pinLuz = A1;          //Pin del LDR
const int pinLed = 3;           //Pin del Led
const int pinDHT = 5;           //Pin del DHT
DHT sensorDeHumedad(pinDHT, DHTTYPE);
const int pinTemperatura = A0;  //Pin del lm35
const int pinFan = 12;
const int pinHeater = 7;

const long intervaloPantalla = 2000;
unsigned long tiempoPantallaViejo = 0;

int iluminacion;
bool procesoTerminado = false;
bool heaterOn = false;
int contador = 0;


void setup() {
  Serial.begin(9600);
  lcd.begin(20, 4);        // 4 lineas con 20 caracteres cada una
  sensorDeHumedad.begin();
  bluetooth.begin(9600);
  pinMode(pinLed , OUTPUT);
  pinMode(pinHeater, OUTPUT);
  pinMode(pinLuz, INPUT);
  lcd.clear();
}

void loop() {

  //delay(3000);
  float humedad = leerHumedad();
  float temperatura = (5.0 * analogRead(A0) * 100.0) / 1024;
  iluminacion = analogRead(pinLuz);

  unsigned long tiempoPantallaActual = millis();

  if (procesoTerminado == false && tiempoPantallaActual - tiempoPantallaViejo >= intervaloPantalla) {
    tiempoPantallaViejo = tiempoPantallaActual;
    lcd.clear();
    lcd.setCursor(1, 0);
    lcd.print("Humedad: " );
    lcd.setCursor(14, 0);
    lcd.print(humedad);
    lcd.setCursor(1, 1);
    lcd.print("Temperatura: " );
    lcd.setCursor(14, 1);
    lcd.print(temperatura);
    lcd.setCursor(1, 2);
    lcd.print("Iluminacion: " );
    lcd.setCursor(14, 2);
    lcd.print(iluminacion);
  }
  if ( humedad >= 75) {
    procesoTerminado = false;
    digitalWrite(pinLed, LOW);                        // Apagar LED indicando que no finalizo el proceso
    analogWrite(pinFan, 255);                         // Encender el Ventilador a velocidad maxima
    digitalWrite(pinHeater, HIGH);                    // Detener el Calentador
    heaterOn = true;
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
    digitalWrite(pinHeater, LOW);                 // Detener el Calentador
    heaterOn = false;
  }

  if (humedad <= 20) {                            // Se termina el secado
    analogWrite(pinFan, 0);                       // Detener el ventilador
    digitalWrite(pinHeater, LOW);                 // Detener el Calentador
    heaterOn = false;
    digitalWrite(pinLed, HIGH);                   // Encender LED indicando que finalizo el proceso

    for (int i = 0; i < 3; i++)                   // Blink al lcd
    {
      lcd.backlight();
      delay(250);
      lcd.noBacklight();
      delay(250);
    }
    lcd.backlight();
    lcd.clear();
    lcd.setCursor(2, 1);
    lcd.print("Proceso de Secado");
    lcd.setCursor(4, 2);
    lcd.print("COMPLETADO");
    procesoTerminado = true;
  }

  if (bluetooth.available()) {
    String solicitud = bluetooth.readString();

    if (solicitud == "shake") {
      lcd.setCursor(0, 3);
      lcd.print("shake reconocido");
      for (int i = 0; i < 3; i++)                  // Blink al lcd
      {
        lcd.backlight();
        delay(250);
        lcd.noBacklight();
        delay(250);
      }
      lcd.backlight();
      lcd.setCursor(0, 3);
      lcd.print("                     ");
    }
    if (solicitud == "proximidad") {
      digitalWrite(pinHeater, HIGH);
      delay(200);
      digitalWrite(pinHeater, LOW);
      delay(200);
      digitalWrite(pinHeater, HIGH);
      delay(200);
      if (heaterOn == false) {
        digitalWrite(pinHeater, LOW);
      }
    }
    if (solicitud == "luminosidad") {
      digitalWrite(pinLed, LOW);
      delay(200);
      digitalWrite(pinLed, HIGH);
      delay(200);
      digitalWrite(pinLed, LOW);
    }

  }

  //probarVentilador();
  //probarHumedad();
  //probarTemperatura();
  //probarLed();
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
  Serial.println(" % ");
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
  //delay(5000);
  //encenderVentilador(pinFan, 0);
  //delay(5000);
}


