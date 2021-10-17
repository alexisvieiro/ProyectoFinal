//Ethernet
#include <ETH.h>

//Sensores
#include <OneWire.h>
#include <DallasTemperature.h>

//SNMP
#include "Agentuino.h"
#include "MIB.h"
#include "Variable.h"

//Webserver
#include <functional>
#include <HTTPSServer.hpp>
#include <SSLCert.hpp>
#include <HTTPRequest.hpp>
#include <HTTPResponse.hpp>
#include <Preferences.h>

//Ethernet y SNMP
static boolean connected = false;
static byte RemoteIP[4] = {192, 168, 0, 90}; // The IP address of the host that will receive the trap

//Sensor Puertas
static uint32_t periodoLecturaPuertas=0;
#define ABIERTA 0
#define CERRADA 1
#define PinPuerta1 34
#define PinPuerta1 32
#define PinPuerta2 33
#define PinPuerta3 14
#define PinPuerta4 16
static int PuertaEstado[4];
static int UltimoEstado[4];

//Sensor Temperatura
static uint32_t periodoLecturaTemperatura=0;
const int oneWireBus = 13; 
OneWire oneWire(oneWireBus);
DallasTemperature sensors(&oneWire);


//WebServer
#define HEADER_USERNAME "X-USERNAME"
#define HEADER_GROUP    "X-GROUP"
#define IP_FIJA 1
#define IP_DHCP 0
using namespace httpsserver;
HTTPSServer * secureServer;
SSLCert * cert;

//Guarda en flash:
//Configuración de ESP32
//Certificado y Key para HTTPs
Preferences preferences;


//Funciones de URLs
void handleRoot(HTTPRequest * req, HTTPResponse * res);
void handleInternalPage(HTTPRequest * req, HTTPResponse * res);
void handleAdminPage(HTTPRequest * req, HTTPResponse * res);
void handlePublicPage(HTTPRequest * req, HTTPResponse * res);
void handle404(HTTPRequest * req, HTTPResponse * res);
void handleConf(HTTPRequest * req, HTTPResponse * res);
void handleConfIPFija(HTTPRequest * req, HTTPResponse * res);
void handleConfIPDHCP(HTTPRequest * req, HTTPResponse * res);

//Middlewares (verifican que estes logueado y tengas permisos de navegar en distintas URLs)
void middlewareAuthentication(HTTPRequest * req, HTTPResponse * res, std::function<void()> next);
void middlewareAuthorization(HTTPRequest * req, HTTPResponse * res, std::function<void()> next);

void setup(){
    //Inicialización Serie
    Serial.begin(115200);

    preferences.begin("ip", false);
    //Certificado y Key
    size_t pkLen = preferences.getBytesLength("PK");
    size_t certLen = preferences.getBytesLength("cert");

     if (pkLen && certLen) {
      uint8_t *pkBuffer = new uint8_t[pkLen];
      preferences.getBytes("PK", pkBuffer, pkLen);
      
      uint8_t *certBuffer = new uint8_t[certLen];
      preferences.getBytes("cert", certBuffer, certLen);
  
      cert = new SSLCert(certBuffer, certLen, pkBuffer, pkLen);
      
    } else {
    Serial.println("Creating certificate.");
    cert = new SSLCert();
    int createCertResult = createSelfSignedCert(*cert,KEYSIZE_2048,"CN=esp32hub.local,O=Ericnet,C=DE","20190101000000","20300101000000");
  
    if (createCertResult != 0) {
      Serial.printf("Cerating certificate failed. Error Code = 0x%02X, check SSLCert.hpp for details", createCertResult);
      while(true) delay(500);
    }
    Serial.println("Creating the certificate was successful");
    preferences.putBytes("PK", (uint8_t *)cert->getPKData(), cert->getPKLength());
    preferences.putBytes("cert", (uint8_t *)cert->getCertData(), cert->getCertLength());  
  
    }    
    //HTTPSServer::HTTPSServer(SSLCert * cert, const uint16_t port, const uint8_t maxConnections, const in_addr_t bindAddress)
    secureServer = new HTTPSServer(cert);



  //IP fija o por DHCP
  char ip[4],ipm[4],ipg[4];
  char modo;


  ip[0]=preferences.getChar("ip1",0);
  ip[1]=preferences.getChar("ip2",0);
  ip[2]=preferences.getChar("ip3",0);
  ip[3]=preferences.getChar("ip4",0);
  
  ipg[0]=preferences.getChar("ipg1",0);
  ipg[1]=preferences.getChar("ipg2",0);
  ipg[2]=preferences.getChar("ipg3",0);
  ipg[3]=preferences.getChar("ipg4",0);
  
  ipm[0]=preferences.getChar("ipm1",0);
  ipm[1]=preferences.getChar("ipm2",0);
  ipm[2]=preferences.getChar("ipm3",0);
  ipm[3]=preferences.getChar("ipm4",0);
  
  modo=preferences.getChar("modo",0);

  Serial.println("");
  Serial.println("");
  Serial.println("-------------------------------");
  Serial.print("IP leida de las preferences: ");
  Serial.print((uint8_t) ip[0]);
  Serial.print(".");
  Serial.print((uint8_t) ip[1]);
  Serial.print(".");
  Serial.print((uint8_t) ip[2]);
  Serial.print(".");
  Serial.print((uint8_t) ip[3]);
  Serial.println("");  

  Serial.print("IP Gateway leida de las preferences: ");
  Serial.print((uint8_t) ipg[0]);
  Serial.print(".");
  Serial.print((uint8_t) ipg[1]);
  Serial.print(".");
  Serial.print((uint8_t) ipg[2]);
  Serial.print(".");
  Serial.println  ((uint8_t) ipg[3]);


  Serial.print("IP Mask leida de las preferences: ");
  Serial.print((uint8_t) ipm[0]);
  Serial.print(".");
  Serial.print((uint8_t) ipm[1]);
  Serial.print(".");
  Serial.print((uint8_t) ipm[2]);
  Serial.print(".");
  Serial.print((uint8_t) ipm[3]);
  Serial.println("");  
  
  Serial.println("-------------------------------");
  Serial.println("");
  Serial.println("");
 
  
  
  
  if (!ip[0] && !ip[1] && !ip[2] && !ip[3] && !modo){


    //Modo 0= dhcp, Modo 1= IP fija
    Serial.println("IP por DHCP, primer inicio.");
    preferences.putChar("ip1",0);
    preferences.putChar("ip2",0);
    preferences.putChar("ip3",0);
    preferences.putChar("ip4",0);
    preferences.putChar("modo",IP_DHCP);
    
  }



    
    
    //Inicialización Pullups Puertas
    //pinMode(PinPuerta1, INPUT);
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

    //Inicialización Ethernet
    WiFi.onEvent(WiFiEvent);
    ETH.begin();
    while(!connected){
      delay(500);
    }


  if(modo== IP_FIJA){
    
    Serial.print("Modo: ");
    Serial.println("IP Fija");
    IPAddress local_IP(ip[0], ip[1], ip[2], ip[3]);
    IPAddress gateway(ipg[0], ipg[1], ipg[2], ipg[3]);
    IPAddress subnet(ipm[0], ipm[1], ipm[2], ipm[3]);
    IPAddress dns(ipg[0], ipg[1], ipg[2], ipg[3]);
    if (!ETH.config(local_IP, gateway, subnet,dns)) {
    Serial.println("STA Fallo al configurar");
    }
    //Modo 1 == FIJA
  }else{
    Serial.println("IP por DHCP");
    //Modo 1 == DHCP
  }
     






  

    // The ResourceNode links URL and HTTP method to a handler function
    ResourceNode * nodeRoot     = new ResourceNode("/", "GET", &handleRoot);
    ResourceNode * nodeInternal = new ResourceNode("/internal", "GET", &handleInternalPage);
    ResourceNode * nodeAdmin    = new ResourceNode("/internal/admin", "GET", &handleAdminPage);
    ResourceNode * nodePublic   = new ResourceNode("/public", "GET", &handlePublicPage);
    ResourceNode * node404      = new ResourceNode("", "GET", &handle404);
    ResourceNode * nodeConf     = new ResourceNode("/conf", "GET", &handleConf);
    ResourceNode * nodeConfIPFija = new ResourceNode("/conf_ipfija", "POST", &handleConfIPFija);
    ResourceNode * nodeConfIPDHCP = new ResourceNode("/conf_ipdhcp", "GET", &handleConfIPDHCP);
    // Add the nodes to the server
    secureServer->registerNode(nodeRoot);
    secureServer->registerNode(nodeInternal);
    secureServer->registerNode(nodeAdmin);
    secureServer->registerNode(nodePublic);
    secureServer->registerNode(nodeConf);
    secureServer->registerNode(nodeConfIPFija);
    secureServer->registerNode(nodeConfIPDHCP);
    // The path is ignored for the default node.
    secureServer->setDefaultNode(node404);
    // middleware (First we check the identity, then we see what the user is allowed to do)
    secureServer->addMiddleware(&middlewareAuthentication);
    secureServer->addMiddleware(&middlewareAuthorization);

    Serial.println("Starting server...");
    secureServer->start();
    if (secureServer->isRunning()) {
    Serial.println("Server ready.");
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
/*
    //Para la lectura datos
    periodoLecturaPuertas=millis();
    periodoLecturaTemperatura=periodoLecturaPuertas;
    periodoLecturaRAM=periodoLecturaPuertas;*/
   
}



void loop() {

    //Agentuino.listen();
    //SensadoPuertas();
    //SensadoTemperatura();
    //SensadoInterno();
    secureServer->loop();
    delay(1);
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
      if(millis()>(periodoLecturaTemperatura+3000)){
      periodoLecturaTemperatura=millis();
      sensors.requestTemperatures(); 
      temperaturaC = round(sensors.getTempCByIndex(0));
      if(temperaturaC==-127){
        temperaturaC=0;
      }
      Serial.print("Temperatura sobrescrita: ");
      Serial.println(temperaturaC);

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
      //Serial.print("MAC: ");
      //Serial.println(ETH.macAddress());
      Serial.print("IP: ");
      Serial.println(ETH.localIP());
      //if (ETH.fullDuplex()) {
      //  Serial.print("FULL_DUPLEX");
      //}
      //Serial.print(":");
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






void middlewareAuthentication(HTTPRequest * req, HTTPResponse * res, std::function<void()> next) {
  Serial.print("Request: ");
  for(int i=0;i<req->getRequestString().length();i++){
    Serial.print(req->getRequestString()[i]);
  }
  Serial.println("");
  Serial.println("Entro a la middlewareAuthentication");
  // Unset both headers to discard any value from the client
  // This prevents authentication bypass by a client that just sets X-USERNAME
  req->setHeader(HEADER_USERNAME, "");
  req->setHeader(HEADER_GROUP, "");

  // Get login information from request
  // If you use HTTP Basic Auth, you can retrieve the values from the request.
  // The return values will be empty strings if the user did not provide any data,
  // or if the format of the Authorization header is invalid (eg. no Basic Method
  // for Authorization, or an invalid Base64 token)
  std::string reqUsername = req->getBasicAuthUser();
  std::string reqPassword = req->getBasicAuthPassword();

  // If the user entered login information, we will check it
  if (reqUsername.length() > 0 && reqPassword.length() > 0) {

    // _Very_ simple hardcoded user database to check credentials and assign the group
    bool authValid = true;
    std::string group = "";
    if (reqUsername == "Admin" && reqPassword == "secret") {
      group = "ADMIN";
    } else if (reqUsername == "user" && reqPassword == "test") {
      group = "USER";
    } else {
      authValid = false;
    }

    // If authentication was successful
    if (authValid) {
      // set custom headers and delegate control
      req->setHeader(HEADER_USERNAME, reqUsername);
      req->setHeader(HEADER_GROUP, group);

      // The user tried to authenticate and was successful
      // -> We proceed with this request.
      next();
    } else {
      // Display error page
      res->setStatusCode(401);
      res->setStatusText("Unauthorized");
      res->setHeader("Content-Type", "text/plain");

      // This should trigger the browser user/password dialog, and it will tell
      // the client how it can authenticate
      res->setHeader("WWW-Authenticate", "Basic realm=\"ESP32 privileged area\"");

      // Small error text on the response document. In a real-world scenario, you
      // shouldn't display the login information on this page, of course ;-)
      res->println("401. Unauthorized (try admin/secret or user/test)");

      // NO CALL TO next() here, as the authentication failed.
      // -> The code above did handle the request already.
    }
  } else {
    // No attempt to authenticate
    // -> Let the request pass through by calling next()
    next();
  }
}

/**
 * This function plays together with the middlewareAuthentication(). While the first function checks the
 * username/password combination and stores it in the request, this function makes use of this information
 * to allow or deny access.
 *
 * This example only prevents unauthorized access to every ResourceNode stored under an /internal/... path.
 */
void middlewareAuthorization(HTTPRequest * req, HTTPResponse * res, std::function<void()> next) {
  Serial.println("Entro a la middlewareAuthorization");
  // Get the username (if any)
  std::string username = req->getHeader(HEADER_USERNAME);

  // Check that only logged-in users may get to the internal area (All URLs starting with /internal)
  // Only a simple example, more complicated configuration is up to you.
  if (username == "" && req->getRequestString().substr(0,9) == "/conf") {
    // Same as the deny-part in middlewareAuthentication()
    res->setStatusCode(401);
    res->setStatusText("Unauthorized");
    res->setHeader("Content-Type", "text/plain");
    res->setHeader("WWW-Authenticate", "Basic realm=\"ESP32 privileged area\"");
    res->println("401. Unauthorized (try admin/secret or user/test)");

    // No call denies access to protected handler function.
  } else {
    // Everything else will be allowed, so we call next()
    next();
  }
}

// This is the internal page. It will greet the user with
// a personalized message and - if the user is in the ADMIN group -
// provide a link to the admin interface.
void handleInternalPage(HTTPRequest * req, HTTPResponse * res) {
  Serial.println("Entro a la handleInternalPage");
  // Header
  res->setStatusCode(200);
  res->setStatusText("OK");
  res->setHeader("Content-Type", "text/html; charset=utf8");

  // Write page
  res->println("<!DOCTYPE html>");
  res->println("<html>");
  res->println("<head>");
  res->println("<title>Internal Area</title>");
  res->println("</head>");
  res->println("<body>");

  // Personalized greeting
  res->print("<h1>Hello ");
  // We can safely use the header value, this area is only accessible if it's
  // set (the middleware takes care of this)
  res->printStd(req->getHeader(HEADER_USERNAME));
  res->print("!</h1>");

  res->println("<p>Welcome to the internal area. Congratulations on successfully entering your password!</p>");

  // The "admin area" will only be shown if the correct group has been assigned in the authenticationMiddleware
  if (req->getHeader(HEADER_GROUP) == "ADMIN") {
    res->println("<div style=\"border:1px solid red;margin: 20px auto;padding:10px;background:#ff8080\">");
    res->println("<h2>You are an administrator</h2>");
    res->println("<p>You are allowed to access the admin page:</p>");
    res->println("<p><a href=\"/internal/admin\">Go to secret admin page</a></p>");
    res->println("</div>");
  }

  // Link to the root page
  res->println("<p><a href=\"/\">Go back home</a></p>");
  res->println("</body>");
  res->println("</html>");
}

void handleAdminPage(HTTPRequest * req, HTTPResponse * res) {
  Serial.println("Entro a la handleAdminPage");
  // Headers
  res->setHeader("Content-Type", "text/html; charset=utf8");

  std::string header = "<!DOCTYPE html><html><head><title>Secret Admin Page</title></head><body><h1>Secret Admin Page</h1>";
  std::string footer = "</body></html>";

  // Checking permissions can not only be done centrally in the middleware function but also in the actual request handler.
  // This would be handy if you provide an API with lists of resources, but access rights are defined object-based.
  if (req->getHeader(HEADER_GROUP) == "ADMIN") {
    res->setStatusCode(200);
    res->setStatusText("OK");
    res->printStd(header);
    res->println("<div style=\"border:1px solid red;margin: 20px auto;padding:10px;background:#ff8080\">");
    res->println("<h1>Congratulations</h1>");
    res->println("<p>You found the secret administrator page!</p>");
    res->println("<p><a href=\"/internal\">Go back</a></p>");
    res->println("</div>");
  } else {
    res->printStd(header);
    res->setStatusCode(403);
    res->setStatusText("Unauthorized");
    res->println("<p><strong>403 Unauthorized</strong> You have no power here!</p>");
  }

  res->printStd(footer);
}

// Just a simple page for demonstration, very similar to the root page.
void handlePublicPage(HTTPRequest * req, HTTPResponse * res) {
  Serial.println("Entro a la handlePublicPage");
  res->setHeader("Content-Type", "text/html");
  res->println("<!DOCTYPE html>");
  res->println("<html>");
  res->println("<head><title>Hello World!</title></head>");
  res->println("<body>");
  res->println("<h1>Hello World!</h1>");
  res->print("<p>Your server is running for ");
  res->print((int)(millis()/1000), DEC);
  res->println(" seconds.</p>");
  res->println("<p><a href=\"/\">Go back</a></p>");
  res->println("</body>");
  res->println("</html>");
}

// For details on the implementation of the hanlder functions, refer to the Static-Page example.
void handleRoot(HTTPRequest * req, HTTPResponse * res) {
  Serial.println("Entro a la handleRoot");
  res->setHeader("Content-Type", "text/html");
  res->println("<!DOCTYPE html>");
  res->println("<html>");
  res->println("<head><title>SNMP HUB </title></head>");
  res->println("<body>");
  res->println("<h1>Pagina principal!</h1>");
  res->println("<p>Chachara.</p>");
  res->println("<p>Ir a: <a href=\"/conf\">Configuracion</a></p>");
  res->println("</body>");
  res->println("</html>");
}

void handleConf(HTTPRequest * req, HTTPResponse * res) {
  res->println("Bienvenido, ");
  res->printStd(req->getHeader(HEADER_USERNAME));
  Serial.println("Entro a la handleConf");


  std::string header = "<!DOCTYPE html><html><head><title>Configuracion SNMP HUB</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head><body>";
  std::string footer = "</body></html>";

  // Checking permissions can not only be done centrally in the middleware function but also in the actual request handler.
  // This would be handy if you provide an API with lists of resources, but access rights are defined object-based.
  if (req->getHeader(HEADER_GROUP) == "ADMIN") {
    res->setStatusCode(200);
    res->setStatusText("OK");
    res->setHeader("Content-Type", "text/html; charset=utf8");
    res->printStd(header);
    res->println("<form action=\"/action_page.php\">");
    res->println("<label for=\"modo\">Modo de configuración IP:</label>");
    res->println("<select id=\"modo\" name=\"modo\" onChange=\"funcion()\">");
    if(preferences.getChar("modo",0)==IP_FIJA){
      res->println("<option value=\"DHCP\">DHCP</option>");
      res->println("<option selected value=\"IP Fija\">IP Fija</option>");
    }else{
      res->println("<option selected value=\"DHCP\">DHCP</option>");
      res->println("<option value=\"IP Fija\">IP Fija</option>");
    } 

    res->println("</select>");
    res->println("</form>");
    res->println("<script>");
    res->println("function funcion() {");
    res->println("var x = document.getElementById(\"modo\").value;");
    res->println("if(x==\"DHCP\"){");
    res->println("document.getElementById(\"configuracion_ipfija\").style.display=\"none\";");
    res->println("document.getElementById(\"configuracion_ipdhcp\").style.display=\"block\";");
    res->println("}else{");
    res->println("document.getElementById(\"configuracion_ipfija\").style.display=\"block\";");
    res->println("document.getElementById(\"configuracion_ipdhcp\").style.display=\"none\";");
    res->println("}}</script>");


    
    //HTML para form de ip fija
    if(preferences.getChar("modo",0)==IP_FIJA){
      res->println("<form style=\"display:block\" id=\"configuracion_ipfija\" action=\"/conf_ipfija\" method=\"POST\">");
    }else{
      res->println("<form style=\"display:none\" id=\"configuracion_ipfija\" action=\"/conf_ipfija\" method=\"POST\">");
    } 
    
    res->println("<fieldset style=\"width:240px\">");
    res->println("<legend>Configuración Estática:</legend>");
    res->println("<label for=\"fname\">Dirección IP:</label><br>");
    res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ip1\" type=\"number\" min=\"0\" max=\"255\">");
    res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ip2\" type=\"number\" min=\"0\" max=\"255\">");
    res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ip3\" type=\"number\" min=\"0\" max=\"255\">");
    res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ip4\" type=\"number\" min=\"0\" max=\"255\">");
    res->println("<br><label for=\"fname\">Mascara de Subred:</label><br>");
    res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipm1\" type=\"number\" min=\"0\" max=\"255\">");
    res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipm2\" type=\"number\" min=\"0\" max=\"255\">");
    res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipm3\" type=\"number\" min=\"0\" max=\"255\">");
    res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipm4\" type=\"number\" min=\"0\" max=\"255\">");
    res->println("<br><label for=\"fname\">Puerta de Enlace predeterminada:</label><br>");
    res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipg1\" type=\"number\" min=\"0\" max=\"255\">");
    res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipg2\" type=\"number\" min=\"0\" max=\"255\">");
    res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipg3\" type=\"number\" min=\"0\" max=\"255\">");
    res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipg4\" type=\"number\" min=\"0\" max=\"255\">");
    res->println("<br></fieldset><br>");
    res->println("<input type=\"submit\" value=\"Aceptar\"></form>");

    //HTML para form de dhcp
    if(preferences.getChar("modo",0)==IP_FIJA){
      res->println("<form style=\"display:none\" id=\"configuracion_ipdhcp\" action=\"/conf_ipdhcp\" method=\"GET\">");
    }else{
      res->println("<form style=\"display:block\" id=\"configuracion_ipdhcp\" action=\"/conf_ipdhcp\" method=\"GET\">");
    }
    res->println("<input type=\"submit\" value=\"Aceptar\" />");
    res->println("</form>");
    

    //res->println("<p><a href=\"/internal\">Go back</a></p>");

  } else {
    res->printStd(header);
    res->setStatusCode(403);
    res->setStatusText("Unauthorized");
    res->println("<p><strong>403 Unauthorized</strong> You have no power here!</p>");
  }

  res->printStd(footer);


}


void handleConfIPFija(HTTPRequest * req, HTTPResponse * res) {


 // The echo callback will return the request body as response body.
  char ip[4],ipg[4],ipm[4];
  // We use text/plain for the response
  res->setHeader("Content-Type","text/plain");

  // Stream the incoming request body to the response body
  // Theoretically, this should work for every request size.
  byte buffer[256];
  char *bufferChar;
  // HTTPReqeust::requestComplete can be used to check whether the
  // body has been parsed completely.
  while(!(req->requestComplete())) {
    // HTTPRequest::readBytes provides access to the request body.
    // It requires a buffer, the max buffer length and it will return
    // the amount of bytes that have been written to the buffer.
    size_t s = req->readBytes(buffer, 256);
    bufferChar=(char *) malloc(s+1);
    for(int i=0;i<s;i++){
      bufferChar[i]= (char) buffer[i];
      Serial.print(bufferChar[i]);
    }
    Serial.println("");
    bufferChar[s]='\0';
    // The response does not only implement the Print interface to
    // write character data to the response but also the write function
    // to write binary data to the response.
    res->write(buffer, s);
  }
  char *parametro=NULL;
  parametro=strtok(bufferChar,"&");
  while(parametro!=NULL){
    //Serial.println(parametro);
    
    
    if(!strncmp(parametro,"ip1=",4)){
      Serial.print("El parametro ip1 es igual a ");
      ip[0]=(char) atoi((parametro+ sizeof(char)*4));
      Serial.println(ip[0]); 
    }

    if(!strncmp(parametro,"ip2=",4)){
      Serial.print("El parametro ip2 es igual a ");
      ip[1]=(char) atoi((parametro+ sizeof(char)*4));
      Serial.println(ip[1]); 
    }

    if(!strncmp(parametro,"ip3=",4)){
      Serial.print("El parametro ip3 es igual a ");
      ip[2]=(char) atoi((parametro+ sizeof(char)*4));
      Serial.println(ip[2]); 
    }


    if(!strncmp(parametro,"ip4=",4)){
      Serial.print("El parametro ip4 es igual a ");
      ip[3]=(char) atoi((parametro+ sizeof(char)*4));
      Serial.println(ip[3]); 
    }


    if(!strncmp(parametro,"ipm1=",5)){
      Serial.print("El parametro ipm1 es igual a ");
      ipm[0]=(char) atoi((parametro+ sizeof(char)*5));
      Serial.println(ipm[0]); 
    }

    if(!strncmp(parametro,"ipm2=",5)){
      Serial.print("El parametro ipm2 es igual a ");
      ipm[1]=(char) atoi((parametro+ sizeof(char)*5));
      Serial.println(ipm[1]); 
    }

    if(!strncmp(parametro,"ipm3=",5)){
      Serial.print("El parametro ipm3 es igual a ");
      ipm[2]=(char) atoi((parametro+ sizeof(char)*5));
      Serial.println(ipm[2]); 
    }


    if(!strncmp(parametro,"ipm4=",5)){
      Serial.print("El parametro ipm4 es igual a ");
      ipm[3]=(char) atoi((parametro+ sizeof(char)*5));
      Serial.println(ipm[3]); 
    }




    if(!strncmp(parametro,"ipg1=",5)){
      Serial.print("El parametro ipg1 es igual a ");
      ipg[0]=(char) atoi((parametro+ sizeof(char)*5));
      Serial.println(ipg[0]); 
    }

    if(!strncmp(parametro,"ipg2=",5)){
      Serial.print("El parametro ipg2 es igual a ");
      ipg[1]=(char) atoi((parametro+ sizeof(char)*5));
      Serial.println(ipg[1]); 
    }

    if(!strncmp(parametro,"ipg3=",5)){
      Serial.print("El parametro ipg3 es igual a ");
      ipg[2]=(char) atoi((parametro+ sizeof(char)*5));
      Serial.println(ipg[2]); 
    }


    if(!strncmp(parametro,"ipg4=",5)){
      Serial.print("El parametro ipg4 es igual a ");
      ipg[3]=(char) atoi((parametro+ sizeof(char)*5));
      Serial.println(ipg[3]); 
    }

   
    parametro= strtok(NULL,"&");
  }
  free(bufferChar);



    //Modo 0= dhcp, Modo 1= IP fija
  Serial.println("IP Fija, reiniciando");
  preferences.putChar("ip1",ip[0]);
  preferences.putChar("ip2",ip[1]);
  preferences.putChar("ip3",ip[2]);
  preferences.putChar("ip4",ip[3]);
  
  preferences.putChar("ipg1",ipg[0]);
  preferences.putChar("ipg2",ipg[1]);
  preferences.putChar("ipg3",ipg[2]);
  preferences.putChar("ipg4",ipg[3]);
  
  preferences.putChar("ipm1",ipm[0]);
  preferences.putChar("ipm2",ipm[1]);
  preferences.putChar("ipm3",ipm[2]);
  preferences.putChar("ipm4",ipm[3]);

  
  preferences.putChar("modo",IP_FIJA);
  delay(2000);
  ESP.restart();

}




void handleConfIPDHCP(HTTPRequest * req, HTTPResponse * res) {

  res->setHeader("Content-Type", "text/html");
  res->println("<!DOCTYPE html>");
  res->println("<html>");
  res->println("<head><title>DHCP</title></head>");
  res->println("<body><h1>DHCP</h1></body>");
  res->println("</html>");



  Serial.println("IP por DHCP, reiniciando");
  preferences.putChar("modo",IP_DHCP);
  delay(2000);
  ESP.restart();
}


void handle404(HTTPRequest * req, HTTPResponse * res) {
  Serial.println("Entro a la handle404");
  req->discardRequestBody();
  res->setStatusCode(404);
  res->setStatusText("Not Found");
  res->setHeader("Content-Type", "text/html");
  res->println("<!DOCTYPE html>");
  res->println("<html>");
  res->println("<head><title>Not Found</title></head>");
  res->println("<body><h1>404 Not Found</h1><p>Lo solicitado no fue encontrado en el servidor.</p></body>");
  res->println("</html>");
}
