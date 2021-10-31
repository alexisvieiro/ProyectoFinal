#include <ETH.h>

//Parametros del puerto Ethernet
//Pin del ESP32 que hace de block al LAN8710
#define ETH_CLK_MODE ETH_CLOCK_GPIO17_OUT
// Pin del cristal externo, en este caso no hay.
#define ETH_POWER_PIN -1
// Tipo de PHY Ethernet
#define ETH_TYPE ETH_PHY_LAN8720
// Direccion I2C del LAN8710
#define ETH_ADDR 0
// Numero del Pin donde conecto el MDC
#define ETH_MDC_PIN 23
// Numero del Pin donde conecto el MDIO
#define ETH_MDIO_PIN 18


void EthernetInit();
void EthernetEvent(WiFiEvent_t event);