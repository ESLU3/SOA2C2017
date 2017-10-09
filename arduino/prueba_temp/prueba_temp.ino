float temp; //variable de temperatura
int valorLDR = 0;  //valor para LDR
void setup() {
  // put your setup code here, to run once:
    pinMode(A0, INPUT); // Tell Arduino to make its Analog A0 pin as Input reading pin
    pinMode(A1, INPUT); //pin para LDR (sensor luminico)
  // start the serial connection at 9600 bps:
   Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
   temp = (5.0 * analogRead(A0) * 100.0) / 1024; 
    valorLDR= analogRead(A1);
    Serial.print("Temperature is: "); //println prints next thing on a new line
    Serial.print((float)temp); // Prints current temperature on Monitor
    Serial.println(" *C");
    Serial.println(" "); //Break space // Start reading on new line
    Serial.print("luz is: "); //println prints next thing on a new line
    Serial.print(valorLDR); // Prints current luz on Monitor
    Serial.println(valorLDR);
    Serial.println(" "); //Break space // Start reading on new line
    delay(3000); // 3 seconds delay before taking the new reading
    if (temp > 30) {
      Serial.println("la temperatura esta alta papá");
    }
    if(valorLDR > 256)
    {
      Serial.println("valor de LDR es mayor a 256");
    }
    if(valorLDR > 512)
    {
      Serial.println("valor de LDR es mayor a 256");
    }
    if(valorLDR > 768)
    {
      Serial.println("valor de LDR es mayor a 256");
    }
    
}
