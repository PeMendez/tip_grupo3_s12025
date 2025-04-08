#include <WiFi.h>
#include <PubSubClient.h>
#define RELAY_ON false
#define RELAY_OFF true

// Configuración de WiFi y MQTT
const char* ssid = "Rengo-AP";
const char* password = "Acm27pts"; //Santo y seña por ahora hardcoded
const char* mqttBroker = "test.mosquitto.org";
const int mqttPort = 1883;
const char* topic = "LEDctrl";


WiFiClient espClient;
PubSubClient mqttClient(espClient);

// Configuración del pin del RELAY
const int relayPin = 15; //P15 del ESP
bool relayState = RELAY_OFF;

// Función para manejar mensajes MQTT
void callback(char* topic, byte* message, unsigned int length) {
  String receivedMessage = "";
  for (int i = 0; i < length; i++) {
    receivedMessage += (char)message[i];
  }

  if (receivedMessage == "on") digitalWrite(relayPin, RELAY_ON);
  if (receivedMessage == "off") digitalWrite(relayPin, RELAY_OFF);
  
  if (receivedMessage == "toggle") {
    relayState = !relayState;
    digitalWrite(relayPin, relayState);
  }
  Serial.println("Estado del RELAY: " + String(relayState));
}

void setup() {
  Serial.begin(115200);

  // Configuración del RELAY como salida
  pinMode(relayPin, OUTPUT);
  digitalWrite(relayPin, RELAY_OFF);

  // Conexión a WiFi
  Serial.println("Conectando a WiFi...");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi conectado!");

  // Configuración del cliente MQTT
  mqttClient.setServer(mqttBroker, mqttPort);
  mqttClient.setCallback(callback);

  // Generar un Client ID único usando la MAC
  uint8_t mac[6];
  WiFi.macAddress(mac);
  String clientId = "ESP32-" + String(mac[4], HEX) + String(mac[5], HEX);
  mqttClient.connect(clientId.c_str());

  if (mqttClient.connected()) {
    Serial.println("Conectado al broker MQTT!");
    mqttClient.subscribe(topic);
    Serial.println("Suscrito al tópico: " + String(topic));
  } else {
    Serial.println("Fallo al conectar al broker MQTT.");
  }
}

void loop() {
  if (!mqttClient.connected()) {
    Serial.println("Reconectando al broker MQTT...");
    uint8_t mac[6];
    WiFi.macAddress(mac);
    String clientId = "ESP32-" + String(mac[4], HEX) + String(mac[5], HEX);
    mqttClient.connect(clientId.c_str());
    
    if (mqttClient.connected()) {
      Serial.println("Reconexión exitosa!");
      mqttClient.subscribe(topic);
    } else {
      Serial.println("Fallo al reconectar al broker MQTT.");
      delay(5000);
    }
  }

  mqttClient.loop();
}
