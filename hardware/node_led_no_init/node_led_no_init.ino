#include <WiFi.h>
#include <PubSubClient.h>

// Configuración de WiFi y MQTT
const char* ssid = "Rengo-AP";
const char* password = "Acm27pts"; //Santo y seña por ahora hardcoded
const char* mqttBroker = "test.mosquitto.org";
const int mqttPort = 1883;
const char* topic = "LEDctrl";

WiFiClient espClient;
PubSubClient mqttClient(espClient);

// Configuración del pin del LED
const int ledPin = 2; //Es el LED built-in de la placa, color azul
bool ledState = false;

// Función para manejar mensajes MQTT
void callback(char* topic, byte* message, unsigned int length) {
  String receivedMessage = "";
  for (int i = 0; i < length; i++) {
    receivedMessage += (char)message[i];
  }

  if (receivedMessage == "on") digitalWrite(ledPin, true);
  if (receivedMessage == "off") digitalWrite(ledPin, false);
  
  if (receivedMessage == "toggle") {
    ledState = !ledState;
    digitalWrite(ledPin, ledState);
  }
}

void setup() {
  Serial.begin(115200);

  // Configuración del LED como salida
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);

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
