#include <OneWire.h>
#include <DallasTemperature.h>
#include "Variable.h"
#include "Sensors.h"
#include "Agentuino.h"


//Sensor Temperatura
static uint32_t periodoLecturaTemperatura=0;
OneWire oneWire(oneWireBus);
DallasTemperature sensors(&oneWire);

//Puerta
static uint32_t periodoLecturaPuertas=0;
int PuertaEstado[4],UltimoEstado[4];

//SNMP
extern byte RemoteIP[4];

void SensorsInit(){

    //Inicialización Pullups Puertas
    pinMode(PinPuerta1, INPUT_PULLUP);
    pinMode(PinPuerta2, INPUT_PULLUP);
    pinMode(PinPuerta3, INPUT_PULLUP);
    pinMode(PinPuerta4, INPUT_PULLUP);

    //Puerta1
    PuertaEstado[0]=digitalRead(PinPuerta1);
    UltimoEstado[0]=PuertaEstado[0];

    //Puerta2
    PuertaEstado[1]=digitalRead(PinPuerta2);
    UltimoEstado[1]=PuertaEstado[1];

    //Puerta3
    PuertaEstado[2]=digitalRead(PinPuerta3);
    UltimoEstado[2]=PuertaEstado[2];

    //Puerta4
    PuertaEstado[3]=digitalRead(PinPuerta4);
    UltimoEstado[3]=PuertaEstado[3];   

    /*
    //Para la lectura datos
    periodoLecturaPuertas=millis();
    periodoLecturaTemperatura=periodoLecturaPuertas;
    periodoLecturaRAM=periodoLecturaPuertas;*/
}




void SensadoInterno(){

   //Lectura de system up time
    if (millis() - prevMillis > 1000) {
        prevMillis += 1000;
        locUpTime += 100;
    }

    //Lectura RAM
    if(millis()  - periodoLecturaRAM > 10000){
      periodoLecturaRAM = millis();
      freeHeap=(ESP.getFreeHeap());
    }
}

void SensadoTemperatura(){

      //Lectura DS18B20
      if(millis()>(periodoLecturaTemperatura+10000)){
      periodoLecturaTemperatura=millis();
      sensors.requestTemperatures(); 
      temperaturaC = round(sensors.getTempCByIndex(0));
      if(temperaturaC==-127){
        temperaturaC=0;
      }

    }
}


void SensadoPuertas(){

    if(millis()-periodoLecturaPuertas>1000){
      periodoLecturaPuertas=millis();

        //Puerta1
        PuertaEstado[0]=digitalRead(PinPuerta1);
        Serial.print("Puerta1: ");
        Serial.println(PuertaEstado[0]);
        if(PuertaEstado[0]==ABIERTA && UltimoEstado[0]==CERRADA){
            Serial.println("Send TRAP: Puerta1 Abierta");
            Agentuino.Trap("Puerta1 Abierta", RemoteIP, locUpTime);
            UltimoEstado[0]=PuertaEstado[0];
        }else if(PuertaEstado[0]==CERRADA && UltimoEstado[0]==ABIERTA){
            Serial.println("Send TRAP: Puerta1 Cerrada");
            Agentuino.Trap("Puerta1 Cerrada", RemoteIP, locUpTime);
            UltimoEstado[0]=PuertaEstado[0];
        }
  
        //Puerta2
        PuertaEstado[1]=digitalRead(PinPuerta2);
        Serial.print("Puerta2: ");
        Serial.println(PuertaEstado[1]);
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
        Serial.print("Puerta3: ");
        Serial.println(PuertaEstado[2]);
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
        Serial.print("Puerta4: ");
        Serial.println(PuertaEstado[3]);
        if(PuertaEstado[3]==ABIERTA && UltimoEstado[3]==CERRADA){
            Serial.println("Send TRAP: Puerta4 Abierta");
            Agentuino.Trap("Puerta4 Abierta", RemoteIP, locUpTime);
            UltimoEstado[3]=PuertaEstado[3];
        }else if(PuertaEstado[3]==CERRADA && UltimoEstado[3]==ABIERTA){
            Serial.println("Send TRAP: Puerta4 Cerrada");
            Agentuino.Trap("Puerta4 Cerrada", RemoteIP, locUpTime);
            UltimoEstado[3]=PuertaEstado[3];
        }
    }
}