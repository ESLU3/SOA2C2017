#include <SPI.h>        
#include <Ethernet.h>
#include <EthernetUdp.h>    

byte macServidor[] = {0x98,0x4F,0xEE,0x01,0x11,0x90}; //MAC de la placa donde este ejecutando el Servidor

IPAddress ipServidor(192, 168, 0, 200); //Direccion IP del Servidor
IPAddress ipCliente(192, 168, 0, 101); //Direccion IP del Cliente

EthernetServer server(8032);

unsigned int puertoServidor = 8032; //Puerto del servidor en donde recibiran los datos del cliente
unsigned int puertoCliente = 1234; //Puerto del cliente en donde recibiran los datos del servidor

const int pinAnalog = A0;  //Pin analogico donde se encuentra conectado el potenciometro
const int pinDigital = 3; //Pin digital donde donde se encuentra conectado el LED

//La instancia de EthernetUDP nos permite enviar y recibir paquetes a traves de UDP
EthernetUDP Udp;

void setup()
{
  // Se arranca el Ethernet y UDP en el servidor
  Ethernet.begin(macServidor,ipServidor);
  Udp.begin(puertoServidor);  

  //Se configura el pin digital como salida
  pinMode(pinDigital,OUTPUT);

  //se configura la velocidad del puerto serie
  Serial.begin(9600);
}

void loop()
{

  //buffers que se utilizan para recibir y enviar datos por el socket
  char stringRecibido[UDP_TX_PACKET_MAX_SIZE]={}; 
  String stringAEnviar="";


  //Se analiza si se recibieron datos por el socket UDP
  int packetSize = Udp.parsePacket();
  
  EthernetClient client = server.available();
  
  Serial.println("bytes recibido:");
  Serial.print(packetSize);
  
  if(packetSize)
  {
      // 1) === LECTURA SOCKET UDP ====
      // Si se recibieron datos por el socket, entonces se lee el paquete recibido      
      leerSocketUdp(stringRecibido,UDP_TX_PACKET_MAX_SIZE);
      
      Serial.println("Contenido:");
      Serial.println(stringRecibido);

      // 2) == ANALISIS ==
      // Se analiza la orden enviada en el paquete de datos desde el cliente
      switch (stringRecibido[0])
      {
          case '1':
          {
            //si se recibio un 1, se prende el LED
            digitalWrite(pinDigital,HIGH);
            break;
          }
          case '2':      
          {
            //si se recibio un 2, se apaga el LED
            digitalWrite(pinDigital,LOW);
            break;
          }
          default:
             break;
      }
      
      //enviarSocketUdp(ReplyBuffer,ipCliente,puertoCliente); 
    }
    
     //3) ==== ENVIO DE VALORES DE SENSORES POR SOCKET UDP===
     // Se lee el pin Analogico y se guarda su valor en un String
     stringAEnviar= String(analogRead(pinAnalog), DEC)+ "\n";
    
    Serial.print("Valor a enviar = " );
    Serial.print(stringAEnviar);

    //Se envia el valor sensado del pin Analogico al cliente a traves del socket UDP
    enviarSocketUdp(stringAEnviar,ipCliente,puertoCliente); 

    //se aplica un sleep
    delay(2000);
    
    client.stop();
}

/**
  * Funcion que lee los datos recibidos en el Socket UDP del cliente
  * y los guarda en un buffer
  * @param bufferLectura: buffer en donde se guardaran los datos recibidos del cliente.
  *        tamanoBuffer: tamano del buffer en donde se guardaran los datos recibidos del cliente.
  * @return --
 */
void leerSocketUdp(char * bufferLectura, unsigned int tamanoBuffer)
{
   // read the packet into packetBufffer
   Udp.read(bufferLectura,tamanoBuffer);
}


/**
  * Funcion que envia un mensaje al cliente a traves del socket UDP
  * @param ipCliente: Direccion IP del cliente al que se desea enviar los datos
  *        puertoCliente: puerto del cliente en donde se van a enviar los datos.
  * @return --
 */
void enviarSocketUdp(String mensaje, IPAddress ipCliente,unsigned int puertoCliente)
{
    Udp.beginPacket(ipCliente,puertoCliente);
    Udp.print(mensaje);
    Udp.endPacket();
  
}

