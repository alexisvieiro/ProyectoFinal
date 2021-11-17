#include "Variable.h"

uint32_t locUpTime              = 0;                                            // read-only (static)
char locContact[30]             = "alevieiro@frba.utn.edu.ar";                        // should be stored/read from EEPROM - read/write (not done for simplicity)
char locName[20]                = "Alexis Vieiro";                                  // should be stored/read from EEPROM - read/write (not done for simplicity)
char locLocation[20]            = "Bs As, Argentina";                             // should be stored/read from EEPROM - read/write (not done for simplicity)
int32_t locServices             = 7;    
int temperaturaC;
long int usedHeap;
byte my_IP_address[4]           = {0,0,0,0};                                    // arduino IP address
