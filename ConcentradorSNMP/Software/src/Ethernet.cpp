//Ethernet
#include "Ethernet.h"
static boolean connected = false;


void EthernetInit(){
  WiFi.onEvent(EthernetEvent);
  ETH.begin(ETH_ADDR, ETH_POWER_PIN, ETH_MDC_PIN, ETH_MDIO_PIN, ETH_TYPE, ETH_CLK_MODE);
  while(!connected){
    //Serial.println("Esperando que inicialice Ethernet");
    delay(500);
  }
}




void EthernetEvent(WiFiEvent_t event){
  switch (event) {
    case ARDUINO_EVENT_ETH_START:
      Serial.println("ETH Iniciado");
      //Nombre de host
      ETH.setHostname("ESP32_HUB");
      break;
    case ARDUINO_EVENT_ETH_CONNECTED:
    connected = true;
      Serial.println("ETH Conectado");
      break;
    case ARDUINO_EVENT_ETH_GOT_IP:
      
      Serial.print("IP: ");
      Serial.println(ETH.localIP());
      Serial.print(ETH.linkSpeed());
      Serial.println("Mbps");
      break;
    case ARDUINO_EVENT_ETH_DISCONNECTED:
      Serial.println("ETH Desconectado");
      connected = false;
      break;
    case ARDUINO_EVENT_ETH_STOP:
      Serial.println("ETH Parado");
      connected = false;
      break;
    default:
      break;
  }
  
}
