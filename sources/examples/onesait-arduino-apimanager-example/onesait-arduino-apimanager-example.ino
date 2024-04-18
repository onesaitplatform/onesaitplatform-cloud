#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

///////////////////Definiciones para comunicaciones/////////////////////
/* change it with your ssid-password */
const char* ssid = "Honeypot";
const char* password = "kangaroo";


IPAddress local_IP(192, 168, 250, 28);
IPAddress gateway(192, 168, 250, 1);
IPAddress subnet(255, 255, 254, 0);
IPAddress primaryDNS(172, 16, 20, 103); //optional
IPAddress secondaryDNS(192, 168, 3, 18); //optional

//////////////////////////////servicios////////////////////////////
void setup_wifi(void);         // inicializa wifi

void setup() {
  randomSeed(87.23);
  setup_wifi();           // Inicializa wifi    
}


void loop() {
  if ((WiFi.status() == WL_CONNECTED)) { //Check the current connection status
  
      HTTPClient http;
      http.begin("http://www.onesaitplatform.online/api-manager/server/api/v1/ArduinoEndpoint");
      http.addHeader("Content-Type", "application/json");
      http.addHeader("X-OP-APIKey", "424abf94bfd24667a76fa0cdae722a85");
  
      StaticJsonBuffer<200> jsonBuffer2;
      JsonObject& ontologyInstance = jsonBuffer2.createObject();
      JsonObject& arduinoFrame = jsonBuffer2.createObject();
  
      arduinoFrame["device"] = "ArduinoBoard";
      arduinoFrame["value"] = String(random(10, 20)); //Swap random with your connected sensor String((analogRead(33))/947.88);
      ontologyInstance["ArduinoFrame"] = arduinoFrame;
  
      String string2send;
  
      ontologyInstance.printTo(string2send);
      Serial.print(string2send);
      
      int httpCode = http.POST(string2send);
      String payload = http.getString();       //Get the response payload
      
      Serial.print("HTTP Return Code: ");
      Serial.println(httpCode);   //Print HTTP return code
      Serial.print("HTTP Response Body: ");
      Serial.println(payload);    //Print request response payload
  
  
      http.end();
      if(httpCode == 200){
        String url = "http://www.onesaitplatform.online/api-manager/server/api/v1/ArduinoEndpoint/LastFiveFrames";
        Serial.println(url);
        http.begin(url); //Specify the URL   
        http.addHeader("Content-Type", "application/json");
        http.addHeader("X-OP-APIKey", "424abf94bfd24667a76fa0cdae722a85");
        int httpCode2 = http.GET();                                        //Make the request
        if (httpCode2 > 0) { //Check for the returning code
          Serial.print("HTTP Return Code: ");
          Serial.println(httpCode);   //Print HTTP return code
          String payloadGet = http.getString();       //Get the response payload
          Serial.print("HTTP Response Body: ");
          Serial.println(payloadGet);    //Print request response payload
        }
      }
  
      http.end();
      //WiFi.disconnect(true);
      //WiFi.mode(WIFI_OFF);   
    }
    else{
        Serial.println("WiFi disconnected");
        Serial.println("Please check your WiFi AP and restart your Arduino device to restart example");
    }
    delay(30000);
}

void setup_wifi() {
  Serial.begin(115200);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(300);
    Serial.print(".");
  }
  
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());   
}


