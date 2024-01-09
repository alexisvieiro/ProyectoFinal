#include "Ethernet.h" //Ethernet
#include "Sensors.h" //Sensores
#include "SNMP.h" //SNMP
#include "ServerHandler.h" //Webserver


void setup(){

    Serial.begin(115200);
    Serial.print("Pin Clock: ");
    Serial.println(ETH_CLK_MODE);
    SensorsInit(); //Inicialización de Sensores
    EthernetInit(); //Inicialización de Ethernet
    ServerInit(); //Inicialización de Servidor HTTPS
    SNMPInit(); //Inicialización SNMP

    }


void loop() {
    //Serial.println("Entra al loop");
    SNMPLoop();
    SensadoPuertas();
    SensadoTemperatura();
    SensadoInterno();
    ServerLoop();
    delay(1);
}





