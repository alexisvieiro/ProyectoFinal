#include "SNMP.h"

void SNMPInit(){
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
}



void SNMPLoop(){
  Agentuino.listen();
}