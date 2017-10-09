/*
WEB SERVICES:
=======================================================================================================
==========   REQUEST DESARROLLADOS ============= | ============   FORMATO ALTERNATIVO     =============       
=======================================================================================================
1)POST http://192.168.0.200:8080/led/{numeroPin} | POST http://192.168.0.200:8080/led/numeroPin?estado=1
       body:{estado:1}                           |      body:---- 
                                                 | 
2)GET http://192.168.0.200:8080/potenciometro    | 
--------------------------------------------------------------------------------------------------------
*/

#include <SPI.h>
#include <Ethernet.h>

#include <ArduinoJson.h>  

//Direccion MAC de la placa que actua como Servidor
byte mac[] = {0x98, 0x4F, 0xEE, 0x01, 0x11, 0x90 };  


//Direccion IP del Servidor 
//byte ip[] = {192, 168, 0, 200 };    
IPAddress ip(192,168,0,200);

//Se connfigura el Servidor para reciba peticiones en el puerto 8080
EthernetServer server(8080);
EthernetClient client;

// maximo tamaño que permitimos que tome el cuerpo body de Post
const size_t MAX_BODY_SIZE = 200;      

int led_pin=3; //Numero de pin donde se conecto el LED
int potenciometro_pin = A0; //Numero de pin del potenciometro


String req_str = "";

void setup() 
{
  Serial.begin(9600);
  pinMode(led_pin, OUTPUT);
  
  //se inicia el servidor
  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip);
  //Ethernet.begin(mac);
  server.begin();

  delay(100);
  
  Serial.print("Servidor escuchando en la direcccion IP: ");
  Serial.println(ip);

  

}

void loop() 
{
  
  //Se empiezan a escuchar las conexiones de los clientes
  client = server.available();
  
  if (client) 
  {
    Serial.print("Cliente Conectado...");

    //un http request finaliza con una linea en blanco
    boolean currentLineIsBlank = true;

    req_str = "";
    
    while (client.connected()) 
    {
      if (client.available())
      {
        //Si se establecio una conexion con un cliente se lee caracter por caracter todo msj http
        //que este envia al servidor
        char c = client.read();
        req_str += c;
     
        //Si todo el msj del Cliente finaliza con una linea en blanco,quiere decir que el http request ha finalizado.
        //Entonces se analiza si empieza con la palabra GET
        if (c == '\n' && currentLineIsBlank && req_str.startsWith("GET")) 
        {
          analizarGet();
          break;
        }

         //Si todo el msj del Cliente finaliza con una linea en blanco,quiere decir que el http request ha finalizado.
        //Entonces se analiza si empieza con la palabra POST
        if (c == '\n' && currentLineIsBlank && req_str.startsWith("POST")) 
        {
          analizarPost();
          break;
        }
  
        if (c == '\n')
        {
           //Se esta comenzando una nueva linea
          currentLineIsBlank = true;
        } 
        else if (c != '\r')
        {
          //Se recibio un caracter de linea actual
          currentLineIsBlank = false;
        }
      }
    }

    //Serial.println(req_str);
    
    //se le da tiempo al cliente para recibir el dato
    delay(1);
    
    // se cierra la conexion
    client.stop();
    
    Serial.println("Cliente desconectado...");
  }
}

//funcion que analiza la URI del msj GET (http request) y en base a este le envia un http response 
// al cliente con los datos solicitados
bool analizarGet()
{
  //Serial.println(req_str);
 
  //si el cliente solicita el estado del potenciometro
  if(req_str.indexOf("/potenciometro")!= -1)
  {
    //Se envia un http response OK con los valores que senso el potenciomentro, en formato JSON,
    //en ese momento
    enviarHttpResponse_OK(client);
    enviarPotenciometroJson(client);
    
    Serial.println("\nSe envio request potenciometro...");
  
    return false;
  }

  //si se recibio mal la URI se envia un http response BAD
  enviarHttpResponse_BadRequest(client);
  Serial.println("\nNo se reconocio request...");

  return true;
}


//funcion que analiza la URI y el contenido del msj POST (http request) y en base a este le envia un http response 
// al cliente indicandole si pudo realizar la accion
bool analizarPost()
{
  char body_post[MAX_BODY_SIZE]="";
  char estado_led[10]="";
 
  //Si es un POST se buscan los parametros  enviados en el campo BODY del mensaje 
  
  //si el cliente solicita encender o apagar un led se alamcena, en la variable num_pin, el numero de pin 
  //indicado en la URI para utilizarlo posteriormente
  String req_str_aux = req_str.substring(req_str.indexOf("/led/"));
  int num_pin = req_str_aux.charAt(5);

  //se leen los datos JSON enviados en el body del msj, para saber si se debe encender o apagar el led
  if(leerBodyPost(req_str_aux,body_post)==false)
  {
     Serial.println("Error al leer el campo Body");
     enviarHttpResponse_BadRequest(client);
     return false;
  }

  //Se analiza si el numero de Pin enviado en la URI del POST corresponde a donde realmente 
  //esta conectado el LED en la placa. 
  if(num_pin=='3')
  {
     //Si es correcto el numero de pin se procede a parsear los datos JSON del campo body.
     //NOTA: Una vez parseado el JSON la variable estado_led contendra la accion que se deberá realizar con el led
     //Encender o apagar
     if(parserBodyPost(body_post,estado_led)==false)
     {
        Serial.println("Error al realizar el parser");
        enviarHttpResponse_BadRequest(client);
        return false;
     }
     else
     {
       //dependiendo del valor que contenga la variable estado_led se va enciender o apagar el led
       // estado_led=1 ---> Enciende Led
       // estado_led=0 ---> Apaga Led  
       digitalWrite(led_pin,atoi(estado_led));
       
       Serial.println("Prendio o Apago LED");
       enviarHttpResponse_OK(client);
       return true;
     }
     
  }

  //Si el Pin enviado en la URI no es correcto
  Serial.println("Numero de Pin Incorrecto..");
  enviarHttpResponse_BadRequest(client);

  return false;
}

//Funcion que lee el campo body del mensaje POST y lo almacena y retorna en la variable body_post 
bool leerBodyPost(String req_str,char * body_post)
{
  int data_length=-1;
  int i=0;
  char c;
    
  //Si determina el tamaño del contenido del campo BODY, leyendo el campo Content-Length del msj
  String body_tam = req_str.substring(req_str.indexOf("Content-Length:") + 15);
  body_tam.trim();
 
  data_length = body_tam.toInt();
  
  //Serial.println("----Content-Length=");
  //Serial.println(data_length);

  //Se verifica que el tamaño del campo body no supere el tamaño del String en donde sera almacenado
  if(data_length>MAX_BODY_SIZE)
  {
    Serial.println("ERROR MAX_BODY_SIZE NO ES LO SUFICIENTEMENTE GRANDE PARA ALMACENAR EL CONTENIDO DE BODY");
    return false; 
  }

  //Serial.println("----textto=");
  //Se lee el contenido del campo BODY y se lo almacena en la variable body_post
  while(i < data_length)
  {
    c = client.read();
    body_post[i] =c;
    i++;
  }

  //Serial.println("\nCAMPO BODY  =");
  //Serial.println(body_post);

  return true;
}

//Funcion que  parsea los datos Json enviados como parametros en la variable content, los analiza y alamcena y retorna en 
// la variable estado el contenido de la key estado. Su contendio indicara si se deberá encender o apagar el led
bool parserBodyPost(char * content, char * estado)
{
  
  // Calculo el tamaño del buffer JSON de acuerdo a lo que se necesita parasear
  const size_t BUFFER_SIZE = JSON_OBJECT_SIZE(1); //El elemento raíz tiene 1 elementos dentro {estado:on}
  
  DynamicJsonBuffer jsonBuffer(BUFFER_SIZE);
  
  //Se convierte el String almcenado en la variable content en una estructura JSON.
  //Aca esta instruccion realiza el parse
  JsonObject& root = jsonBuffer.parseObject(content);

  
  if (!root.success()) 
  {
    Serial.println("JSON parsing failed!");
    return false;
  }
  //Si el parser se realizo correctamente, copia el conteindo del key  estado del JSON
  // en la variable que tiene el mismo nombre 
  strcpy(estado, root["estado"]);

  Serial.println("estado:");
  Serial.println(estado);
  return true;
}


//Funcion que arma un paquete Json con los valores que senso el potenciometro en ese momento
JsonObject& armarRespuestaJson(JsonBuffer& jsonBuffer) 
{
  JsonObject& root = jsonBuffer.createObject();

  //=======================================
  // Formato JSON de respuesta para GET:
  // {"sensor":"potenciometro","valor":999}
  //=======================================
  
  root["sensor"]="potenciometro";
  root["valor"]=analogRead(potenciometro_pin);
  
  return root;
}

//Funcion que envia como parte del http response al cliente un paquete JSON con los valores que 
//senso el potenciometro
void enviarPotenciometroJson(EthernetClient& client) 
{
  StaticJsonBuffer<500> jsonBuffer;

  //Se arma el paquete JSON
  JsonObject& json = armarRespuestaJson(jsonBuffer);

  //Se envia el paquete JSON al cliente en el campo body del http response
  json.prettyPrintTo(client);

  /*Serial.println("\n Json a enviar = ");
  json.prettyPrintTo(Serial);*/
}

//Funcion que envia al cliente un Http response OK
void enviarHttpResponse_OK(EthernetClient& client) 
{
  client.println("HTTP/1.1 200 OK");
  //client.println("Content-Type: application/json");
  client.println("Connection: close");
  client.println();
}

//Funcion que envia al cliente un Http Bad Request
void enviarHttpResponse_BadRequest(EthernetClient& client) 
{
  client.println("HTTP/1.1 400 BAD REQUEST");
  client.println("Content-Type: text/html");
  client.println("Connection: close");
  client.println();
  client.println("<!DOCTYPE HTML>");
  client.println("<html> <body>");
  client.println("BAD REQUEST"); 
  client.println("</body> </html>");
  client.println();
}
