//Ethernet
#include "Ethernet.h"
static boolean connected = false;


void EthernetInit(){
  WiFi.onEvent(EthernetEvent);
  ETH.begin(ETH_ADDR, ETH_POWER_PIN, ETH_MDC_PIN, ETH_MDIO_PIN, ETH_TYPE, ETH_CLK_MODE);
  while(!connected){
    delay(500);
  }
}




void EthernetEvent(WiFiEvent_t event){
  switch (event) {
    case SYSTEM_EVENT_ETH_START:
      Serial.println("ETH Iniciado");
      //set eth hostname here
      ETH.setHostname("esphub");
      break;
    case SYSTEM_EVENT_ETH_CONNECTED:
      Serial.println("ETH Conectado");
      break;
    case SYSTEM_EVENT_ETH_GOT_IP:

      Serial.print("IP: ");
      Serial.println(ETH.localIP());
      Serial.print(ETH.linkSpeed());
      Serial.println("Mbps");
      connected = true;
      break;
    case SYSTEM_EVENT_ETH_DISCONNECTED:
      Serial.println("ETH Desconectado");
      connected = false;
      break;
    case SYSTEM_EVENT_ETH_STOP:
      Serial.println("ETH Parado");
      connected = false;
      break;
    default:
      break;
  }
  
}
