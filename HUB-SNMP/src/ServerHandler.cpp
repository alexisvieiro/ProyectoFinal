#include "ServerHandler.h"
#include <ETH.h>


extern int PuertaEstado[4];
extern int UltimoEstado[4];
char ip[4],ipm[4],ipg[4],modo;
HTTPSServer * secureServer;
SSLCert * cert;
Preferences preferences;
byte RemoteIP[4];
static bool CambiarIPESP32=0;

void ServerLoop(){
    secureServer->loop();
    if (CambiarIPESP32==1){
      delay(5000);
      Serial.println("Reconfigurando ETH.config()");
      ServerIP();
      CambiarIPESP32=0;
    }
}

void ServerStart(){

    Serial.println("Starting server...");
    secureServer->start();
    if (secureServer->isRunning()) {
    Serial.println("Server ready.");
    }
}

void ServerInit(){
    
    //------------------------------------------------Certificado HTTPS---------------------------------------------------------------------//
    preferences.begin("ip", false);
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
      preferences.putBytes("PK", (uint8_t *)cert->getPKData(), cert->getPKLength());  //Guardo PK del Certificado creado
      preferences.putBytes("cert", (uint8_t *)cert->getCertData(), cert->getCertLength());  //Guardo Certificado creado
    }    
    secureServer = new HTTPSServer(cert);
    //------------------------------------------------Fin-Certificado-HTTPS---------------------------------------------------------------------//


    ServerIP();    //Configuración de IP (DHCP o Fija)
    ServerPaths(); //Configuración de los URL del servidor HTTPS
    ServerStart(); //Declaración DE URLs y comienzo del servidor HTTPS

}


void ServerIP(){

  //----------------------------------------------Lectura de parametros de las configuraciónes IP---------------------------------------------//

    //IP del HUB
    ip[0]=preferences.getChar("ip1",0);
    ip[1]=preferences.getChar("ip2",0);
    ip[2]=preferences.getChar("ip3",0);
    ip[3]=preferences.getChar("ip4",0);
    //IP del Gateway
    ipg[0]=preferences.getChar("ipg1",0);
    ipg[1]=preferences.getChar("ipg2",0);
    ipg[2]=preferences.getChar("ipg3",0);
    ipg[3]=preferences.getChar("ipg4",0);
    //Mascara de subred
    ipm[0]=preferences.getChar("ipm1",0);
    ipm[1]=preferences.getChar("ipm2",0);
    ipm[2]=preferences.getChar("ipm3",0);
    ipm[3]=preferences.getChar("ipm4",0);
    //IP del servidor SNMP
    RemoteIP[0] = (byte) preferences.getChar("ipsnmp1",0);
    RemoteIP[1] = (byte) preferences.getChar("ipsnmp2",0);
    RemoteIP[2] = (byte) preferences.getChar("ipsnmp3",0);
    RemoteIP[3] = (byte) preferences.getChar("ipsnmp4",0);
    //Modo (0 para DHCP, 1 para IP Fija)
    modo=preferences.getChar("modo",0);

    if (!ip[0] && !ip[1] && !ip[2] && !ip[3] && !modo){ //Primer inicio
      Serial.println("IP por DHCP, primer inicio.");
      preferences.putChar("ip1",192);
      preferences.putChar("ip2",168);
      preferences.putChar("ip3",1);
      preferences.putChar("ip4",99);
      preferences.putChar("modo",IP_FIJA);
    }
  //---------------------------------------Fin de Lectura de parametros de las configuraciónes IP-------------------------------------------//
  

  //----------------------------------------------Seteo de parametros de las configuraciónes IP---------------------------------------------//
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
  }else{
    Serial.print("Modo: ");
    Serial.println("IP por DHCP");
    if (!ETH.config(0u,0u,0u,0u)) {
    Serial.println("STA Fallo al configurar");
    }
 
  }
  //-------------------------------------------Fin de Seteo de parametros de las configuraciónes IP------------------------------------------//  

}




void ServerPaths(){

    //Declaración de URLs del servidor HTTPS
    ResourceNode * nodeRoot     = new ResourceNode("/", "GET", &handleRoot);
    ResourceNode * node404      = new ResourceNode("", "GET", &handle404);
    ResourceNode * nodeConf     = new ResourceNode("/conf", "GET", &handleConf);
    ResourceNode * nodeConfIPFija = new ResourceNode("/conf_ipfija", "POST", &handleConfIPFija);
    ResourceNode * nodeConfIPDHCP = new ResourceNode("/conf_ipdhcp", "GET", &handleConfIPDHCP);
    ResourceNode * nodeConfIPSNMP = new ResourceNode("/conf_ipsnmp", "POST", &handleConfIPSNMP);

    // Los agrego al servidor
    secureServer->registerNode(nodeRoot);
    secureServer->registerNode(nodeConf);
    secureServer->registerNode(nodeConfIPFija);
    secureServer->registerNode(nodeConfIPDHCP);
    secureServer->registerNode(nodeConfIPSNMP);
    // Agrego el default (Error 404)
    secureServer->setDefaultNode(node404);
    
    // Antes de cargar la página, verifico si esta identificado
    secureServer->addMiddleware(&middlewareAuthentication);
    secureServer->addMiddleware(&middlewareAuthorization);

}


void middlewareAuthentication(HTTPRequest * req, HTTPResponse * res, std::function<void()> next) {


    //Info de Debug
    Serial.print("Request: ");
    for(int i=0;i<req->getRequestString().length();i++){
      Serial.print(req->getRequestString()[i]);
    }
    //Info de Debug
    Serial.println("");
    Serial.println("Entro a la middlewareAuthentication");
    
    // Limpio los headers de los usuarios para evitar falsas autenticaciones
    req->setHeader(HEADER_USERNAME, "");
    req->setHeader(HEADER_GROUP, "");

    // Tomo la informacion del login a través del request.
    std::string reqUsername = req->getBasicAuthUser();
    std::string reqPassword = req->getBasicAuthPassword();

    // Si el usuario ingreso datos de login
    if (reqUsername.length() > 0 && reqPassword.length() > 0) {

      bool authValid = true;
      std::string group = "";
      if (reqUsername == "Admin" && reqPassword == "secret") {
        group = "ADMIN";
      }  else {
        authValid = false;
      }

      
      if (authValid) {
        // Si se autenticó bien:
        // Fijo los headers del usuario Admin identificado
        req->setHeader(HEADER_USERNAME, reqUsername);
        req->setHeader(HEADER_GROUP, group);
        next();
      } else {
        // Si no se autenticó bien:
        res->setStatusCode(401);
        res->setStatusText("Sin Autorizacion");
        res->setHeader("Content-Type", "text/plain");
        //Provoco la ventana de login
        res->setHeader("WWW-Authenticate", "Basic realm=\"ESP32 privileged area\"");
        res->println("401 - Sin Autorizacion");
      }
    } else {
      // No intentó autenticarse
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
    // Me traigo el usuario ingresado previamente
    std::string username = req->getHeader(HEADER_USERNAME);
    
    // Chequeo que solo los usuarios logueados puedan descargar las paginas (Para los URLs comenzados con /)
    if (username == "" && req->getRequestString().substr(0,9) == "/") {
      res->setStatusCode(401);
      res->setStatusText("Sin Autorizacion");
      res->setHeader("Content-Type", "text/html; charset=utf8");
      res->setHeader("WWW-Authenticate", "Basic realm=\"ESP32 privileged area\"");
      res->println("<!DOCTYPE html><html><head><title>SNMP HUB</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head>");
      res->println("<body>");
      res->println("401 - Sin Autorizacion");
      res->println("</body></html>");
    
    } else {
      next();
    }
}



// Handler del URL https://<IP>/
void handleRoot(HTTPRequest * req, HTTPResponse * res) {

    Serial.println("Entro a la handleRoot");

    if (req->getHeader(HEADER_GROUP) == "ADMIN") {
      //------------------------------------------------------HTML para encabezado y bienvenida---------------------------------------------------//
      res->setStatusCode(200);
      res->setStatusText("OK");  
      res->setHeader("Content-Type", "text/html; charset=utf8");
      res->println("<!DOCTYPE html><html><head><title>SNMP HUB </title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head><body>");
      res->println("<body>");
      res->println("Bienvenido,");
      res->printStd(req->getHeader(HEADER_USERNAME));
      //---------------------------------------------------Fin HTML para encabezado y bienvenida---------------------------------------------------//

      //----------------------------------------------------HTML para información de ESP32 HUB---------------------------------------------------//
      res->println("<br><br><fieldset style=\"width:280px\">");
      res->println("<legend>Estado de HUB SNMP:</legend>");
      res->println("<label for=\"fname\">Tiempo de encendido: ");
      int hs,ms,ss;
      hs= (locUpTime/100)/3600;
      ms= ((locUpTime/100)- hs*60)/60;
      ss= (locUpTime/100) - hs*3600 - ms*60;
      res->println((String) hs);
      res->println("h ");
      res->println((String)ms);
      res->println("m ");
      res->println((String)ss);
      res->println("s ");
      res->println("</label><br>");
      res->println("<label for=\"fname\">Uso de memoria RAM: ");
      res->println((1- ((float)ESP.getFreeHeap()/(float)ESP.getHeapSize()))*100);
      res->println("%");
      res->println("</label>");
      res->println("</fieldset>");
      //----------------------------------------------------HTML para información de ESP32 HUB----------------------------------------------------//


      //------------------------------------------------------HTML para información de Sensores----------------------------------------------------//
      res->println("<br><fieldset style=\"width:280px\">");
      res->println("<legend>Estado de Sensores:</legend>");
      if(UltimoEstado[0]==ABIERTA){
        res->println("<label for=\"fname\">Estado Puerta 1: ABIERTA</label><br>");
      }else{
        res->println("<label for=\"fname\">Estado Puerta 1: CERRADA</label><br>");  
      }

      if(UltimoEstado[1]==ABIERTA){
        res->println("<label for=\"fname\">Estado Puerta 2: ABIERTA</label><br>");
      }else{
        res->println("<label for=\"fname\">Estado Puerta 2: CERRADA</label><br>");  
      }

      if(UltimoEstado[2]==ABIERTA){
        res->println("<label for=\"fname\">Estado Puerta 3: ABIERTA</label><br>");
      }else{
        res->println("<label for=\"fname\">Estado Puerta 3: CERRADA</label><br>");  
      }

      if(UltimoEstado[3]==ABIERTA){
        res->println("<label for=\"fname\">Estado Puerta 4: ABIERTA</label><br>");
      }else{
        res->println("<label for=\"fname\">Estado Puerta 4: CERRADA</label><br>");  
      }

      res->println("<label for=\"fname\">Temperatura de Sensor: ");
      res->println(temperaturaC);
      res->println("</label>");
      res->println("</fieldset>");
      //------------------------------------------------------HTML para información de Sensores------------------------------------------------------//
      res->println("<p>Ir a: <a href=\"/conf\">Configuracion</a></p>");
      res->println("</body></html>");

    }else{

      //--------------------------------------------------------HTML para usuarios sin permiso---------------------------------------------------------------------//
      res->println("<!DOCTYPE html><html><head><title>Configuracion SNMP HUB</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head><body>");
      res->setStatusCode(403);
      res->setStatusText("Sin Autorización");
      res->println("403 - Sin Autorizacion");
      res->println("</body></html>");
      //--------------------------------------------------------Fin HTML para usuarios sin permiso----------------------------------------------------------------//
    }
}


// Handler del URL https://<IP>/conf/
void handleConf(HTTPRequest * req, HTTPResponse * res) {

    Serial.println("Entro a la handleConf");

    if (req->getHeader(HEADER_GROUP) == "ADMIN") {
      //------------------------------------------------------HTML para encabezado y bienvenida---------------------------------------------------//
      res->setStatusCode(200);
      res->setStatusText("OK");
      res->setHeader("Content-Type", "text/html; charset=utf8");
      res->println("<!DOCTYPE html><html><head><title>Configuracion SNMP HUB</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head>");
      res->println("<body>");
      res->println("Bienvenido, ");
      res->printStd(req->getHeader(HEADER_USERNAME));
      //------------------------------------------------------Fin HTML para encabezado y bienvenida-------------------------------------------------//


      //------------------------------------------------------HTML para elegir si es por DHCP o IP FIja---------------------------------------------------//
      res->println("<form action=\"/action_page.php\">");
      res->println("<fieldset style=\"width:280px\">");
      res->println("<legend>HUB SNMP:</legend>");
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
      //------------------------------------------------------Fin HTML para elegir si es por DHCP o IP FIja---------------------------------------------------//


      //---------------------------------------------------HTML para solicitar la configuracion de IP/Mascara/Gateway-------------------------------------------//
      if(preferences.getChar("modo",0)==IP_FIJA){
        res->println("<form style=\"display:block\" id=\"configuracion_ipfija\" action=\"/conf_ipfija\" method=\"POST\">");
      }else{
        res->println("<form style=\"display:none\" id=\"configuracion_ipfija\" action=\"/conf_ipfija\" method=\"POST\">");
      } 
      res->println("<label for=\"fname\">Dirección IP:</label><br>");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ip1\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char)preferences.getChar("ip1",0)));
      res->println(">"); 
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ip2\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char)preferences.getChar("ip2",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ip3\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ip3",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ip4\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ip4",0)));
      res->println(">");
      res->println("<br><label for=\"fname\">Mascara de Subred:</label><br>");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipm1\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipm1",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipm2\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char)preferences.getChar("ipm2",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipm3\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipm3",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipm4\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipm4",0)));
      res->println(">");
      res->println("<br><label for=\"fname\">Puerta de Enlace predeterminada:</label><br>");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipg1\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipg1",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipg2\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char)preferences.getChar("ipg2",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipg3\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipg3",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipg4\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipg4",0)));
      res->println(">");
      res->println("<br><br><input type=\"submit\" value=\"Aceptar\"></form>");
      //------------------------------------------------Fin HTML para solicitar la configuracion de IP/Mascara/Gateway-------------------------------------------//


      //-----------------------------------------------------HTML para configurar el ESP32 con IP por DHCP--------------------------------------------------------//
      if(preferences.getChar("modo",0)==IP_FIJA){
        res->println("<form style=\"display:none\" id=\"configuracion_ipdhcp\" action=\"/conf_ipdhcp\" method=\"GET\">");
      }else{
        res->println("<form style=\"display:block\" id=\"configuracion_ipdhcp\" action=\"/conf_ipdhcp\" method=\"GET\">");
      }
      res->println("<br><input type=\"submit\" value=\"Aceptar\" />");
      res->println("</form>");
      res->println("</fieldset><br>");
      //-----------------------------------------------------Fin HTML para configurar el ESP32 con IP por DHCP----------------------------------------------------//


      //---------------------------------------------------HTML para solicitar la configuracion de IP del servidor SNMP-------------------------------------------//
      res->println("<form style=\"display:block\" id=\"configuracion_snmp\" action=\"/conf_ipsnmp\" method=\"POST\">");
      res->println("<fieldset style=\"width:280px\">");
      res->println("<legend>Servidor SNMP de Traps:</legend>");
      res->println("<label for=\"fname\">Dirección IP:</label><br>");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipsnmp1\" type=\"number\" min=\"1\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipsnmp1",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipsnmp2\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipsnmp2",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipsnmp3\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipsnmp3",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipsnmp4\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipsnmp4",0)));
      res->println(">");
      res->println("<br><br><input type=\"submit\" value=\"Aceptar\"></form>");
      res->println("<br></fieldset><br>");
      //------------------------------------------------Fin HTML para solicitar la configuracion de IP del servidor SNMP-------------------------------------------//


      //---------------------------------------------------HTML para boton volver (hacia https://<IP>/------------------------------------------------------------//
      res->println("<form id=\"configuracion_volver\" action=\"/\" method=\"GET\">");
      res->println("<input type=\"submit\" value=\"Volver\" />");
      res->println("</form>");
      res->println("</body></html>");
      //---------------------------------------------------Fin  HTML para boton volver (hacia https://<IP>/--------------------------------------------------------//

    } else {
      //--------------------------------------------------------HTML para usuarios sin permiso---------------------------------------------------------------------//
      res->println("<!DOCTYPE html><html><head><title>SNMP HUB</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head><body>");
      res->setStatusCode(403);
      res->setStatusText("Sin Autorización");
      res->println("403 - Sin Autorizacion");
      res->println("</body></html>");
      //--------------------------------------------------------FIN HTML para usuarios sin permiso------------------------------------------------------------------//

    }
}


void handleConfIPFija(HTTPRequest * req, HTTPResponse * res) {

    byte buffer[256]; //buffer para guardar el request
    char *bufferChar; //puntero para contener el request recibido, pero casteado a char
    char *parametro=NULL; //puntero para el strtok

    if (req->getHeader(HEADER_GROUP) == "ADMIN") {

    size_t s = req->readBytes(buffer, 256);
    bufferChar=(char *) malloc(s+1);
    //casteo el buffer a char
    for(int i=0;i<s;i++){
      bufferChar[i]= (char) buffer[i];
      }

    bufferChar[s]='\0';
    
    while(!(req->requestComplete())) req->readBytes(buffer, 256); //descarto el resto si hubiese
    
    
    //--------------------------------------------Divido el Request en sus diferentes partes--------------------------//
    parametro=strtok(bufferChar,"&");
    while(parametro!=NULL){

      if(!strncmp(parametro,"ip1=",4)){
        //Serial.print("El parametro ip1 es igual a ");
        ip[0]=(char) atoi((parametro+ sizeof(char)*4));
        //Serial.println(ip[0]); 
      }

      if(!strncmp(parametro,"ip2=",4)){
        //Serial.print("El parametro ip2 es igual a ");
        ip[1]=(char) atoi((parametro+ sizeof(char)*4));
        //Serial.println(ip[1]); 
      }

      if(!strncmp(parametro,"ip3=",4)){
        //Serial.print("El parametro ip3 es igual a ");
        ip[2]=(char) atoi((parametro+ sizeof(char)*4));
        //Serial.println(ip[2]); 
      }

      if(!strncmp(parametro,"ip4=",4)){
        //Serial.print("El parametro ip4 es igual a ");
        ip[3]=(char) atoi((parametro+ sizeof(char)*4));
        //Serial.println(ip[3]); 
      }

      if(!strncmp(parametro,"ipm1=",5)){
        //Serial.print("El parametro ipm1 es igual a ");
        ipm[0]=(char) atoi((parametro+ sizeof(char)*5));
        //Serial.println(ipm[0]); 
      }

      if(!strncmp(parametro,"ipm2=",5)){
        //Serial.print("El parametro ipm2 es igual a ");
        ipm[1]=(char) atoi((parametro+ sizeof(char)*5));
        //Serial.println(ipm[1]); 
      }

      if(!strncmp(parametro,"ipm3=",5)){
        //Serial.print("El parametro ipm3 es igual a ");
        ipm[2]=(char) atoi((parametro+ sizeof(char)*5));
        //Serial.println(ipm[2]); 
      }

      if(!strncmp(parametro,"ipm4=",5)){
        //Serial.print("El parametro ipm4 es igual a ");
        ipm[3]=(char) atoi((parametro+ sizeof(char)*5));
        //Serial.println(ipm[3]); 
      }

      if(!strncmp(parametro,"ipg1=",5)){
        //Serial.print("El parametro ipg1 es igual a ");
        ipg[0]=(char) atoi((parametro+ sizeof(char)*5));
        //Serial.println(ipg[0]); 
      }

      if(!strncmp(parametro,"ipg2=",5)){
        //Serial.print("El parametro ipg2 es igual a ");
        ipg[1]=(char) atoi((parametro+ sizeof(char)*5));
        //Serial.println(ipg[1]); 
      }

      if(!strncmp(parametro,"ipg3=",5)){
        //Serial.print("El parametro ipg3 es igual a ");
        ipg[2]=(char) atoi((parametro+ sizeof(char)*5));
        //Serial.println(ipg[2]); 
      }

      if(!strncmp(parametro,"ipg4=",5)){
        //Serial.print("El parametro ipg4 es igual a ");
        ipg[3]=(char) atoi((parametro+ sizeof(char)*5));
        //Serial.println(ipg[3]); 
      }

      parametro= strtok(NULL,"&");
    }

    free(bufferChar);
    //--------------------------------------Fin de Divido el Request en sus diferentes partes--------------------------//


    //codigo de chequeo de errores de datos ingrados por usuario


    //----------------------------------Guardo la información de la IP ingresada por el usuario------------------------//
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
    CambiarIPESP32=1;
    //-----------------------------Fin de Guardo la información de la IP ingresada por el usuario------------------------//


    //------------------------------------------------------HTML para encabezado y bienvenida---------------------------------------------------//
    res->setStatusCode(200);
    res->setStatusText("OK");
    res->setHeader("Content-Type", "text/html; charset=utf8");
    res->println("<!DOCTYPE html><html><head><title>Configuracion SNMP HUB</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head>");
    res->println("<body>");
    //------------------------------------------------------Fin HTML para encabezado y bienvenida-------------------------------------------------//
    
   //----------------------------------------------------HTML para mensaje de confirmación en verde-----------------------------------------------//
    res->println("<div class=\"alert\">");
    res->println("<span class=\"closebtn\" onclick=\"this.parentElement.style.display='none';\">&times;</span>");
    res->println("IP Fija configurada correctamente. Ingrese con su nueva IP.");
    res->println("</div> ");
    res->println("<style>");
    res->println(".alert {padding: 20px; display:inline-block; background-color: #00fc4c; color: white; margin-bottom: 15px;}");
    res->println(".closebtn { margin-left: 15px; color: white; font-weight: bold; float: right; font-size: 22px; line-height: 20px; cursor: pointer; transition: 0.3s;}");
    res->println(".closebtn:hover {color: black;} ");
    res->println("</style>");
    //--------------------------------------------------Fin HTML para mensaje de confirmación en verde-----------------------------------------------//

    }else{
      //--------------------------------------------------------HTML para usuarios sin permiso---------------------------------------------------------------------//
      res->println("<!DOCTYPE html><html><head><title>SNMP HUB</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head><body>");
      res->setStatusCode(403);
      res->setStatusText("Sin Autorización");
      res->println("403 - Sin Autorizacion");
      res->println("</body></html>");
      //--------------------------------------------------------FIN HTML para usuarios sin permiso------------------------------------------------------------------//
    }

}


void handleConfIPDHCP(HTTPRequest * req, HTTPResponse * res) {

    Serial.println("Entro a la handleConf");

    if (req->getHeader(HEADER_GROUP) == "ADMIN") {
      preferences.putChar("modo",IP_DHCP);
      CambiarIPESP32=1;

      //------------------------------------------------------HTML para encabezado y bienvenida---------------------------------------------------//
      res->setStatusCode(200);
      res->setStatusText("OK");
      res->setHeader("Content-Type", "text/html; charset=utf8");
      res->println("<!DOCTYPE html><html><head><title>Configuracion SNMP HUB</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head>");
      res->println("<body>");
      //------------------------------------------------------Fin HTML para encabezado y bienvenida-------------------------------------------------//

      //----------------------------------------------------HTML para mensaje de confirmación en verde-----------------------------------------------//
      res->println("<div class=\"alert\">");
      res->println("<span class=\"closebtn\" onclick=\"this.parentElement.style.display='none';\">&times;</span>");
      res->println("IP por DHCP configurado correctamente. Ingrese con su nueva IP.");
      res->println("</div> ");
      res->println("<style>");
      res->println(".alert {padding: 20px; display:inline-block; background-color: #00fc4c; color: white; margin-bottom: 15px;}");
      res->println(".closebtn { margin-left: 15px; color: white; font-weight: bold; float: right; font-size: 22px; line-height: 20px; cursor: pointer; transition: 0.3s;}");
      res->println(".closebtn:hover {color: black;} ");
      res->println("</style>");
      //--------------------------------------------------Fin HTML para mensaje de confirmación en verde-----------------------------------------------//
      
      //------------------------------------------------------Fin HTML para encabezado y bienvenida-------------------------------------------------//

    } else {
      //--------------------------------------------------------HTML para usuarios sin permiso---------------------------------------------------------------------//
      res->println("<!DOCTYPE html><html><head><title>SNMP HUB</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head><body>");
      res->setStatusCode(403);
      res->setStatusText("Sin Autorización");
      res->println("403 - Sin Autorizacion");
      res->println("</body></html>");
      //--------------------------------------------------------FIN HTML para usuarios sin permiso------------------------------------------------------------------//

    }
    
}



void handleConfIPSNMP(HTTPRequest * req, HTTPResponse * res) {

    Serial.println("Entro a la handleConfIPSNMP");
    byte buffer[256]; //buffer para guardar el request
    char *bufferChar; //puntero para contener el request recibido, pero casteado a char
    char *parametro=NULL; //puntero para el strtok
    char ipsnmp[4]={0,0,0,0};

    if (req->getHeader(HEADER_GROUP) == "ADMIN") {
    
    size_t s = req->readBytes(buffer, 256);
    bufferChar=(char *) malloc(s+1);
    //casteo el buffer a char
    for(int i=0;i<s;i++){
      bufferChar[i]= (char) buffer[i];
      //Serial.print(bufferChar[i]);
    }
    //Serial.println("");
    bufferChar[s]='\0';
      
    while(!(req->requestComplete())) req->readBytes(buffer, 256); //descarto el resto si hubiese

    //--------------------------------------------Divido el Request en sus diferentes partes--------------------------//
    parametro=strtok(bufferChar,"&");
    while(parametro!=NULL){

      if(!strncmp(parametro,"ipsnmp1=",8)){
        //Serial.print("El parametro ipsnmp1 es igual a ");
        ipsnmp[0]=(char) atoi((parametro+ sizeof(char)*8));
        //Serial.println((int) ipsnmp[0]); 
      }

      if(!strncmp(parametro,"ipsnmp2=",8)){
        //Serial.print("El parametro ipsnmp2 es igual a ");
        ipsnmp[1]=(char) atoi((parametro+ sizeof(char)*8));
        //Serial.println((int) ipsnmp[1]); 
      }

      if(!strncmp(parametro,"ipsnmp3=",8)){
        //Serial.print("El parametro ipsnmp3 es igual a ");
        ipsnmp[2]=(char) atoi((parametro+ sizeof(char)*8));
        //Serial.println((int)  ipsnmp[2]); 
      }

      if(!strncmp(parametro,"ipsnmp4=",8)){
        //Serial.print("El parametro ipsnmp4 es igual a ");
        ipsnmp[3]=(char) atoi((parametro+ sizeof(char)*8));
        //Serial.println((int) ipsnmp[3]); 
      }
      parametro= strtok(NULL,"&");
      
      
    }

    free(bufferChar);
    //------------------------------------------Fin de Divido el Request en sus diferentes partes--------------------------//
   

    //codigo de chequeo de errores de datos ingrados por usuario




    //----------------------------------Guardo la información de la IP ingresada por el usuario------------------------//
    preferences.putChar("ipsnmp1",ipsnmp[0]);
    preferences.putChar("ipsnmp2",ipsnmp[1]);
    preferences.putChar("ipsnmp3",ipsnmp[2]);
    preferences.putChar("ipsnmp4",ipsnmp[3]);

    RemoteIP[0] = (byte) ipsnmp[0];
    RemoteIP[1] = (byte) ipsnmp[1];
    RemoteIP[2] = (byte) ipsnmp[2];
    RemoteIP[3] = (byte) ipsnmp[3];
    //-----------------------------Fin de Guardo la información de la IP ingresada por el usuario------------------------//


    //------------------------------------------------------HTML para encabezado y bienvenida---------------------------------------------------//
    res->setStatusCode(200);
    res->setStatusText("OK");
    res->setHeader("Content-Type", "text/html; charset=utf8");
    res->println("<!DOCTYPE html><html><head><title>Configuracion SNMP HUB</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head>");
    res->println("<body>");
    //------------------------------------------------------Fin HTML para encabezado y bienvenida-------------------------------------------------//    
    

    //----------------------------------------------------HTML para mensaje de confirmación en verde-----------------------------------------------//
    res->println("<br><div class=\"alert\">");
    res->println("<span class=\"closebtn\" onclick=\"this.parentElement.style.display='none';\">&times;</span>");
    res->println("IP del Servidor configurada correctamente.");
    res->println("</div> ");
    res->println("<style>");
    res->println(".alert {padding: 20px; display:inline-block; background-color: #00fc4c; color: white; margin-bottom: 15px;}");
    res->println(".closebtn { margin-left: 15px; color: white; font-weight: bold; float: right; font-size: 22px; line-height: 20px; cursor: pointer; transition: 0.3s;}");
    res->println(".closebtn:hover {color: black;} ");
    res->println("</style>");
    //--------------------------------------------------Fin HTML para mensaje de confirmación en verde-----------------------------------------------//
    
    //------------------------------------------------------HTML para elegir si es por DHCP o IP FIja---------------------------------------------------//
      res->println("<form action=\"/action_page.php\">");
      res->println("<fieldset style=\"width:280px\">");
      res->println("<legend>HUB SNMP:</legend>");
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
      //------------------------------------------------------Fin HTML para elegir si es por DHCP o IP FIja---------------------------------------------------//


      //---------------------------------------------------HTML para solicitar la configuracion de IP/Mascara/Gateway-------------------------------------------//
      if(preferences.getChar("modo",0)==IP_FIJA){
        res->println("<form style=\"display:block\" id=\"configuracion_ipfija\" action=\"/conf_ipfija\" method=\"POST\">");
      }else{
        res->println("<form style=\"display:none\" id=\"configuracion_ipfija\" action=\"/conf_ipfija\" method=\"POST\">");
      } 
      res->println("<label for=\"fname\">Dirección IP:</label><br>");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ip1\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char)preferences.getChar("ip1",0)));
      res->println(">"); 
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ip2\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char)preferences.getChar("ip2",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ip3\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ip3",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ip4\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ip4",0)));
      res->println(">");
      res->println("<br><label for=\"fname\">Mascara de Subred:</label><br>");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipm1\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipm1",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipm2\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char)preferences.getChar("ipm2",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipm3\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipm3",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipm4\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipm4",0)));
      res->println(">");
      res->println("<br><label for=\"fname\">Puerta de Enlace predeterminada:</label><br>");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipg1\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipg1",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipg2\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char)preferences.getChar("ipg2",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipg3\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipg3",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipg4\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipg4",0)));
      res->println(">");
      res->println("<br><br><input type=\"submit\" value=\"Aceptar\"></form>");
      //------------------------------------------------Fin HTML para solicitar la configuracion de IP/Mascara/Gateway-------------------------------------------//


      //-----------------------------------------------------HTML para configurar el ESP32 con IP por DHCP--------------------------------------------------------//
      if(preferences.getChar("modo",0)==IP_FIJA){
        res->println("<form style=\"display:none\" id=\"configuracion_ipdhcp\" action=\"/conf_ipdhcp\" method=\"GET\">");
      }else{
        res->println("<form style=\"display:block\" id=\"configuracion_ipdhcp\" action=\"/conf_ipdhcp\" method=\"GET\">");
      }
      res->println("<br><input type=\"submit\" value=\"Aceptar\" />");
      res->println("</form>");
      res->println("</fieldset><br>");
      //-----------------------------------------------------Fin HTML para configurar el ESP32 con IP por DHCP----------------------------------------------------//


      //---------------------------------------------------HTML para solicitar la configuracion de IP del servidor SNMP-------------------------------------------//
      res->println("<form style=\"display:block\" id=\"configuracion_snmp\" action=\"/conf_ipsnmp\" method=\"POST\">");
      res->println("<fieldset style=\"width:280px\">");
      res->println("<legend>Servidor SNMP de Traps:</legend>");
      res->println("<label for=\"fname\">Dirección IP:</label><br>");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipsnmp1\" type=\"number\" min=\"1\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipsnmp1",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipsnmp2\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipsnmp2",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipsnmp3\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipsnmp3",0)));
      res->println(">");
      res->println("<input required style=\"width:45px; height:15px; font-size:12px;\" maxlength=\"3\" name=\"ipsnmp4\" type=\"number\" min=\"0\" max=\"255\" value=");
      res->println(int((unsigned char) preferences.getChar("ipsnmp4",0)));
      res->println(">");
      res->println("<br><br><input type=\"submit\" value=\"Aceptar\"></form>");
      res->println("<br></fieldset><br>");
      //------------------------------------------------Fin HTML para solicitar la configuracion de IP del servidor SNMP-------------------------------------------//


      //---------------------------------------------------HTML para boton volver (hacia https://<IP>/------------------------------------------------------------//
      res->println("<form id=\"configuracion_volver\" action=\"/\" method=\"GET\">");
      res->println("<input type=\"submit\" value=\"Volver\" />");
      res->println("</form>");
      res->println("</body></html>");
      //---------------------------------------------------Fin  HTML para boton volver (hacia https://<IP>/--------------------------------------------------------//

    }else{
      //--------------------------------------------------------HTML para usuarios sin permiso---------------------------------------------------------------------//
      res->println("<!DOCTYPE html><html><head><title>SNMP HUB</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head><body>");
      res->setStatusCode(403);
      res->setStatusText("Sin Autorización");
      res->println("403 - Sin Autorizacion");
      res->println("</body></html>");
      //--------------------------------------------------------FIN HTML para usuarios sin permiso------------------------------------------------------------------//
    }

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


