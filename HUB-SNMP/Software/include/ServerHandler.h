//Servidor
#include <functional>
#include <HTTPSServer.hpp>
#include <SSLCert.hpp>
#include <HTTPRequest.hpp>
#include <HTTPResponse.hpp>
#include <Preferences.h>

//SNMP
#include "Variable.h"


#define HEADER_USERNAME "X-USERNAME"
#define HEADER_GROUP    "X-GROUP"
#define IP_FIJA 1
#define IP_DHCP 0

#ifndef ABIERTA
#define ABIERTA 1
#define CERRADA 0
#endif



using namespace httpsserver;

void handleRoot(HTTPRequest * req, HTTPResponse * res);
void handleInternalPage(HTTPRequest * req, HTTPResponse * res);
void handleAdminPage(HTTPRequest * req, HTTPResponse * res);
void handlePublicPage(HTTPRequest * req, HTTPResponse * res);
void handle404(HTTPRequest * req, HTTPResponse * res);
void handleConf(HTTPRequest * req, HTTPResponse * res);
void handleConfIPFija(HTTPRequest * req, HTTPResponse * res);
void handleConfIPDHCP(HTTPRequest * req, HTTPResponse * res);
void handleConfIPSNMP(HTTPRequest * req, HTTPResponse * res);
void handlePassword(HTTPRequest * req, HTTPResponse * res);

void middlewareAuthentication(HTTPRequest * req, HTTPResponse * res, std::function<void()> next);
void middlewareAuthorization(HTTPRequest * req, HTTPResponse * res, std::function<void()> next);

void ServerInit();
void ServerIP();
void ServerPaths();
void ServerStart();
void ServerLoop();

