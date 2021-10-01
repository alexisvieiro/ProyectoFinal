#include <ETH.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include "Agentuino.h"
#include "MIB.h"
#include "Variable.h"


//Ethernet y SNMP
static boolean connected = false;
static byte RemoteIP[4] = {192, 168, 0, 90}; // The IP address of the host that will receive the trap\

//Sensor Puertas
static uint32_t periodoLecturaPuertas=0;
#define ABIERTA 0
#define CERRADA 1
#define PinPuerta1 34
//#define PinPuerta1 32
//#define PinPuerta2 33
//#define PinPuerta3 14
//#define PinPuerta4 16
static int PuertaEstado[4];
static int UltimoEstado[4];


//Sensor Temperatura
static uint32_t periodoLecturaTemperatura=0;
const int oneWireBus = 13; 
OneWire oneWire(oneWireBus);
DallasTemperature sensors(&oneWire);


void setup(){
    //Inicialización Serie
    Serial.begin(115200);
        
    //Inicialización Pullups Puertas
    pinMode(PinPuerta1, INPUT);
    //pinMode(PinPuerta1, INPUT_PULLUP);
    //pinMode(PinPuerta2, INPUT_PULLUP);
    //pinMode(PinPuerta3, INPUT_PULLUP);
    //pinMode(PinPuerta41, INPUT_PULLUP);

    //Puerta1
    PuertaEstado[0]=digitalRead(PinPuerta1);
    UltimoEstado[0]=PuertaEstado[0];

    //Puerta2
    //PuertaEstado[1]=digitalRead(PinPuerta2);
    //UltimoEstado[1]=PuertaEstado[1];

    //Puerta3
    //PuertaEstado[2]=digitalRead(PinPuerta3);
    //UltimoEstado[2]=PuertaEstado[2];

    //Puerta4
    //PuertaEstado[3]=digitalRead(PinPuerta4);
    //UltimoEstado[3]=PuertaEstado[3];   


     
    //Inicialización Ethernet
    WiFi.onEvent(WiFiEvent);
    ETH.begin();
    while(!connected){
      delay(500);
    }

    //Inicialización SNMP
    api_status = Agentuino.begin();
    if (api_status == SNMP_API_STAT_SUCCESS) {
        Agentuino.onPduReceive(pduReceived);
        delay(10);
        Serial.println("Agente SNMP inicializado");
        return;
    }else{
        delay(10);
        Serial.println("Agente SNMP no inicializado");
    }

    //Para la lectura datos
    periodoLecturaPuertas=millis();
    periodoLecturaTemperatura=periodoLecturaPuertas;
    periodoLecturaRAM=periodoLecturaPuertas;
   
}



void loop() {

    Agentuino.listen();
    SensadoPuertas();
    SensadoTemperatura();
    SensadoInterno();
}

void SensadoInterno(){
   //Lectura de system up time
    if (millis() - prevMillis > 1000) {
        prevMillis += 1000;
        locUpTime += 100;
    }

    //Lectura RAM
    if(millis()  - periodoLecturaRAM > 60000){
      periodoLecturaRAM = millis();
      freeHeap=(ESP.getFreeHeap());
      Serial.print("Free Heap: ");
      Serial.println(freeHeap);
    }

}

void SensadoTemperatura(){

      //Lectura ds18b20
      if(millis()>(periodoLecturaTemperatura+30000)){
      periodoLecturaTemperatura=millis();
      sensors.requestTemperatures(); 
      temperaturaC = round(sensors.getTempCByIndex(0));
      if(temperaturaC==-127){
        temperaturaC=0;
      }
      //Serial.print("Temperatura sobrescrita: ");
      //Serial.println(temperaturaC);

    }
}


void SensadoPuertas(){


    if(millis()-periodoLecturaPuertas>1000){
      periodoLecturaPuertas=millis();

      
        //Puerta1
        PuertaEstado[0]=digitalRead(PinPuerta1);
        if(PuertaEstado[0]==ABIERTA && UltimoEstado[0]==CERRADA){
            Serial.println("Send TRAP: Puerta1 Abierta");
            Agentuino.Trap("Puerta1 Abierta", RemoteIP, locUpTime);
            UltimoEstado[0]=PuertaEstado[0];
        }else if(PuertaEstado[0]==CERRADA && UltimoEstado[0]==ABIERTA){
            Serial.println("Send TRAP: Puerta1 Cerrada");
            Agentuino.Trap("Puerta1 Cerrada", RemoteIP, locUpTime);
            UltimoEstado[0]=PuertaEstado[0];
        }
  
      /*
        //Puerta2
        PuertaEstado[1]=digitalRead(PinPuerta2);
        if(PuertaEstado[1]==ABIERTA && UltimoEstado[1]==CERRADA){
            Serial.println("Send TRAP: Puerta2 Abierta");
            Agentuino.Trap("Puerta2 Abierta", RemoteIP, locUpTime);
            UltimoEstado[1]=PuertaEstado[1];
        }else if(PuertaEstado[1]==CERRADA && UltimoEstado[1]==ABIERTA){
            Serial.println("Send TRAP: Puerta2 Cerrada");
            Agentuino.Trap("Puerta2 Cerrada", RemoteIP, locUpTime);
            UltimoEstado[1]=PuertaEstado[1];
        }


        //Puerta3
        PuertaEstado[2]=digitalRead(PinPuerta3);
        if(PuertaEstado[2]==ABIERTA && UltimoEstado[2]==CERRADA){
            Serial.println("Send TRAP: Puerta3 Abierta");
            Agentuino.Trap("Puerta3 Abierta", RemoteIP, locUpTime);
            UltimoEstado[2]=PuertaEstado[2];
        }else if(PuertaEstado[2]==CERRADA && UltimoEstado[2]==ABIERTA){
            Serial.println("Send TRAP: Puerta3 Cerrada");
            Agentuino.Trap("Puerta3 Cerrada", RemoteIP, locUpTime);
            UltimoEstado[2]=PuertaEstado[2];
        }


        //Puerta4
        PuertaEstado[3]=digitalRead(PinPuerta4);
        if(PuertaEstado[3]==ABIERTA && UltimoEstado[3]==CERRADA){
            Serial.println("Send TRAP: Puerta4 Abierta");
            Agentuino.Trap("Puerta4 Abierta", RemoteIP, locUpTime);
            UltimoEstado[3]=PuertaEstado[3];
        }else if(PuertaEstado[3]==CERRADA && UltimoEstado[3]==ABIERTA){
            Serial.println("Send TRAP: Puerta4 Cerrada");
            Agentuino.Trap("Puerta4 Cerrada", RemoteIP, locUpTime);
            UltimoEstado[3]=PuertaEstado[3];
        }
        */
    }
}

void WiFiEvent(WiFiEvent_t event){
  switch (event) {
    case SYSTEM_EVENT_ETH_START:
      Serial.println("ETH Iniciado");
      //set eth hostname here
      ETH.setHostname("esp32-ethernet");
      break;
    case SYSTEM_EVENT_ETH_CONNECTED:
      Serial.println("ETH Conectado");
      break;
    case SYSTEM_EVENT_ETH_GOT_IP:
      Serial.print("MAC: ");
      Serial.println(ETH.macAddress());
      Serial.print("IP: ");
      Serial.println(ETH.localIP());
      if (ETH.fullDuplex()) {
        Serial.print("FULL_DUPLEX");
      }
      Serial.print(":");
      Serial.print(ETH.linkSpeed());
      Serial.println("Mbps");
      //eth_connected = true;
      connected = true;
      break;
    case SYSTEM_EVENT_ETH_DISCONNECTED:
      Serial.println("ETH Desconectado");
      //eth_connected = false;
      connected = false;
      break;
    case SYSTEM_EVENT_ETH_STOP:
      Serial.println("ETH Parado");
      //eth_connected = false;
      connected = false;
      break;
    default:
      break;
  }
  
}
