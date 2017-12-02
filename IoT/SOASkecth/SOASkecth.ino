#include "DHT.h"
#define DHTTYPE DHT22
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <SoftwareSerial.h>
SoftwareSerial bluetooth(10, 11); // RX, TX (contrario)
//                    addr, en,rw,rs,d4,d5,d6,d7,bl,blpol
LiquidCrystal_I2C lcd(0x27, 2, 1, 0, 4, 5, 6, 7, 3, POSITIVE);  // 0x27 es la direccion del i2c del lcd

const int pinLuz = A1;                //Pin del LDR
const int pinTemperatura = A0;        //Pin del Lm35
const int pinLed = 3;                 //Pin del Led
const int pinFan = 12;                //Pin del Fan
const int pinHeater = 8;              //Pin del Heater
const int pinDHT = 5;                 //Pin del DHT
DHT sensorDeHumedad(pinDHT, DHTTYPE); //Inicializar el sensor con el pin y el tipo(Dht22)

const long intervaloPantalla = 5000;
unsigned long tiempoPantallaViejo = 0;
int iluminacion;

bool procesoTerminado = false;
bool heaterOn = false;
bool fanOn = false;
bool fanConfig = false;
bool heaterConfig = false;
bool humConfig = false;
bool tempConfig = false;
float temperatura;
float humedad;

float humedadConfMin;
float tempConfMin;
float tempConf;
float ilumConf;

void setup() {

  humedadConfMin = 20;
  tempConfMin = 20;
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

  humedad = leerHumedad();
  temperatura = (5.0 * analogRead(A0) * 100.0) / 1024;
  iluminacion = analogRead(pinLuz);
  unsigned long tiempoPantallaActual = millis();

  if (procesoTerminado == false && tiempoPantallaActual - tiempoPantallaViejo >= intervaloPantalla) {
    tiempoPantallaViejo = tiempoPantallaActual;
    float temperaturaDHT = leerTemperatura();
    lcd.clear();
    lcd.setCursor(1, 0);
    lcd.print("HUMEDAD: " );
    lcd.setCursor(14, 0);
    lcd.print(humedad);
    lcd.setCursor(1, 1);
    lcd.print("TEMPERATURA: " );
    lcd.setCursor(14, 1);
    lcd.print(temperaturaDHT);
    lcd.setCursor(1, 2);
    lcd.print("ILUMINACION: " );
    lcd.setCursor(14, 2);


    String ilu;
    if (iluminacion < 150) {
      lcd.print("BAJA");
      ilu = "Baja";
    } else {
      if (iluminacion >= 150 && iluminacion <= 400) {
        lcd.print("MEDIA");
        ilu = "Media";
      } else
      {
        lcd.print("ALTA");
        ilu = "Alta";
      }
    }
    String heater;
    String proceso;
    String fan;
    if (heaterOn == true) {
      heater = "ON";
      lcd.setCursor(9, 3);
      lcd.print("HEATER ON ");
    } else {
      heater = "OFF";
      lcd.setCursor(9, 3);
      lcd.print("HEATER OFF");
    }
    if (fanOn == true) {
      fan = "ON";
      lcd.setCursor(1, 3);
      lcd.print("FAN ON ");
    } else {
      fan = "OFF";
      lcd.setCursor(1, 3);
      lcd.print("FAN OFF");
    } if (procesoTerminado == true) {
      proceso = "ON";
    } else {
      proceso = "OFF";
    }

    String enviar = String(temperaturaDHT) + "|" + String(humedad) + "|" + ilu + "|" + heater + "|" + proceso + "|" + fan + "|";
    bluetooth.println(enviar);

  }

  if ( humedad >= 40) {
    procesoTerminado = false;
    digitalWrite(pinLed, LOW);                        // Apagar LED que indica que no finalizo el proceso

    if (!fanConfig) {
      analogWrite(pinFan, 255);                         // Encender el Ventilador a velocidad maxima
      fanOn = true;
      lcd.setCursor(1, 3);
      lcd.print("FAN ON ");
    }
    if (!heaterConfig) {
      digitalWrite(pinHeater, HIGH);                    // Encender el Calentador
      heaterOn = true;
      lcd.setCursor(9, 3);
      lcd.print("HEATER ON ");
    }

  } else {
    if (temperatura < tempConfMin) {
      digitalWrite(pinLed, LOW);                        // Apagar LED que indica que no finalizo el proceso
      procesoTerminado = false;
      if (!fanConfig) {
        analogWrite(pinFan, 255);                         // Encender el Ventilador a velocidad maxima
        fanOn = true;
        lcd.setCursor(1, 3);
        lcd.print("FAN ON ");
      }
    }
  }
  if (humedad < 40 && temperatura >= 20 && iluminacion >= 150) {   // Falta terminar el secado pero no es necesario continuar con el calentador encendido
    digitalWrite(pinLed, LOW);                        // Apagar LED que indica que no finalizo el proceso
    procesoTerminado = false;

    if (!fanConfig) {
      analogWrite(pinFan, 0);
      fanOn = false;
      lcd.setCursor(1, 3);
      lcd.print("FAN OFF");
    }
    if (!heaterConfig) {
      digitalWrite(pinHeater, LOW);                 // Detener el Calentador
      heaterOn = false;
      lcd.setCursor(9, 3);
      lcd.print("HEATER OFF");
    }
  }
  if (humedad <= humedadConfMin) {                            // Se termina el secado
    analogWrite(pinFan, 0);                       // Detener el ventilador
    fanOn = false;
    lcd.setCursor(1, 3);
    lcd.print("FAN OFF");
    digitalWrite(pinHeater, LOW);                 // Detener el Calentador
    lcd.setCursor(9, 3);
    lcd.print("HEATER OFF");
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
    lcd.print("PROCESO DE SECADO");
    lcd.setCursor(4, 2);
    lcd.print("COMPLETADO");
    procesoTerminado = true;
    bluetooth.println("fin");
  }

  if (bluetooth.available()) {

    String solicitud = bluetooth.readString();
    bool shake = solicitud.startsWith("1");
    bool proximidad = solicitud.startsWith("2");
    bool luz = solicitud.startsWith("3");
    bool eCalentador = solicitud.startsWith("5");
    bool aCalentador = solicitud.startsWith("6");
    bool eFan = solicitud.startsWith("7");
    bool aFan = solicitud.startsWith("8");
    bool hum = solicitud.startsWith("h");
    bool temp = solicitud.startsWith("t");
    bool original = solicitud.startsWith("o");

    if (shake) {                         //SHAKE
      lcd.clear();
      lcd.setCursor(0, 3);
      lcd.print("                     ");
      lcd.print(" SHAKE RECONOCIDO");
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
    if (proximidad) {                        //PROXI
      digitalWrite(pinHeater, HIGH);
      delay(200);
      digitalWrite(pinHeater, LOW);
      delay(200);
      digitalWrite(pinHeater, HIGH);
      delay(200);
      digitalWrite(pinHeater, LOW);
      delay(200);
      digitalWrite(pinHeater, HIGH);
      delay(200);
      digitalWrite(pinHeater, LOW);
      delay(200);
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
    if (luz) {                               //LUZ
      digitalWrite(pinLed, LOW);
      delay(200);
      digitalWrite(pinLed, HIGH);
      delay(200);
      digitalWrite(pinLed, LOW);
      delay(200);
      digitalWrite(pinLed, HIGH);
      delay(200);
      digitalWrite(pinLed, LOW);
      delay(200);
      digitalWrite(pinLed, HIGH);
      delay(200);
      digitalWrite(pinLed, LOW);
      delay(200);
      digitalWrite(pinLed, HIGH);
      delay(200);
      digitalWrite(pinLed, LOW);
    }
    if (eFan) {
      analogWrite(pinFan, 255);                         // Encender el Ventilador a velocidad maxima
      fanOn = true;
      fanConfig = true;
      lcd.setCursor(1, 3);
      lcd.print("FAN ON ");
    }
    if (eCalentador) {
      digitalWrite(pinHeater, HIGH);                    // Encender el Calentador
      heaterOn = true;
      heaterConfig = true;
      lcd.setCursor(9, 3);
      lcd.print("HEATER ON ");
    }
    if (aFan) {
      analogWrite(pinFan, 0);                         // Encender el Ventilador a velocidad maxima
      fanOn = false;
      fanConfig = true;
      lcd.setCursor(1, 3);
      lcd.print("FAN OFF");
    }
    if (aCalentador) {
      digitalWrite(pinHeater, LOW);                    // Encender el Calentador
      heaterOn = false;
      heaterConfig = true;
      lcd.setCursor(9, 3);
      lcd.print("HEATER OFF");
    }
    if (hum) {
      solicitud.remove(0, 1);
      humedadConfMin = solicitud.toFloat();
      humConfig = true;
    }
    if (temp) {
      solicitud.remove(0, 1);
      Serial.println(solicitud);
      tempConfMin = solicitud.toFloat();
      tempConfig = true;
    }

    if (original) {
      fanConfig = false;
      heaterConfig = false;
      humConfig = false;
      tempConfig = false;
      humedadConfMin = 20;
      tempConfMin = 20;
    }
  }
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
}


