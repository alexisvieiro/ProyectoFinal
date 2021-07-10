#include <ETH.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include "Agentuino.h"
#include "MIB.h"
#include "Variable.h"

//Botón
#define UserButton 34
#define ABIERTA 0
#define CERRADA 1
static int PuertaEstado=1;
static int UltimoEstado=1;
static int reboteTiempo = 0;

//Ethernet
static boolean connected = false;

//SNMP
static byte RemoteIP[4] = {192, 168, 0, 90}; // The IP address of the host that will receive the trap
static int tiempoSensor;
const int oneWireBus = 4; 
OneWire oneWire(oneWireBus);
DallasTemperature sensors(&oneWire);

//Multitarea
TaskHandle_t TaskWifi;
//, TaskEthernet;
SemaphoreHandle_t semaforoMutex;


void TaskWifiCode( void * parameter )
{

  for (;;) {

  }
}

void setup(){
    //Inicialización Serie
    Serial.begin(115200);
        
    //Inicialización Botón
    pinMode(UserButton, INPUT);
    PuertaEstado=digitalRead(UserButton);
    UltimoEstado=digitalRead(UserButton);
    
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
    tiempoSensor=millis();

    //semaforoMutex = xSemaphoreCreateMutex();
    //xSemaphoreGive(semaforoMutex);
    /*
    xTaskCreatePinnedToCore(
    TaskWifiCode,
    "WifiCode",
    1000,
    NULL,
    1,
    &TaskWifi,
    0);
  delay(500);  // needed to start-up taskWifi
  */
    
}



void loop() {
////INICIO SNMP
    Agentuino.listen();

////INICIO Botón
    PuertaEstado=digitalRead(UserButton);
    if(PuertaEstado!=UltimoEstado){
        if((millis()-reboteTiempo)>500){
          UltimoEstado=PuertaEstado;

          if(PuertaEstado==ABIERTA){
            Serial.println("Send TRAP");
            Agentuino.Trap("Puerta1", RemoteIP, locUpTime);
          }
          //delay(1000);
          //locUpTime = locUpTime + 100;      
          }
    }else{
          reboteTiempo= millis();
    }
////FIN Botón

    if (millis() - prevMillis > 1000) {
        prevMillis += 1000;
        locUpTime += 100;
    }

   //SENSOR
   if(millis()>(tiempoSensor+1000)){
      tiempoSensor=millis();
      sensors.requestTemperatures(); 
      temperaturaC = (int) sensors.getTempCByIndex(0);
      Serial.print("Temperatura sobrescrita: ");
      Serial.println(temperaturaC);
      
    }
    
////FIN SNMP

}



void WiFiEvent(WiFiEvent_t event){
  switch (event) {
    case SYSTEM_EVENT_ETH_START:
      Serial.println("ETH Started");
      //set eth hostname here
      ETH.setHostname("esp32-ethernet");
      break;
    case SYSTEM_EVENT_ETH_CONNECTED:
      Serial.println("ETH Connected");
      break;
    case SYSTEM_EVENT_ETH_GOT_IP:
      Serial.print("ETH MAC: ");
      Serial.print(ETH.macAddress());
      Serial.print(", IPv4: ");
      Serial.print(ETH.localIP());
      if (ETH.fullDuplex()) {
        Serial.print(", FULL_DUPLEX");
      }
      Serial.print(", ");
      Serial.print(ETH.linkSpeed());
      Serial.println("Mbps");
      //eth_connected = true;
      connected = true;
      break;
    case SYSTEM_EVENT_ETH_DISCONNECTED:
      Serial.println("ETH Disconnected");
      //eth_connected = false;
      connected = false;
      break;
    case SYSTEM_EVENT_ETH_STOP:
      Serial.println("ETH Stopped");
      //eth_connected = false;
      connected = false;
      break;
    default:
      break;
  }
  
}
