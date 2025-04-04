#include <WiFi.h>
#include <PubSubClient.h>

// Configuración de WiFi y MQTT
const char* ssid = "Rengo-AP";
const char* password = "Acm27pts"; //Santo y seña por ahora hardcoded
const char* mqttBroker = "test.mosquitto.org";
const int mqttPort = 1883;
const char* topic = "unq-button";

WiFiClient espClient;
PubSubClient mqttClient(espClient);

// Configuración del botón
const int buttonPin = 4;  // Es el marcado como P4 en la placa
bool lastButtonState = HIGH;
unsigned long lastDebounceTime = 0;
const unsigned long debounceDelay = 50;  // 50ms para antirrebote del pulsador

void setup() {
  Serial.begin(115200);

  // Configuración del botón como entrada con pull-up
  pinMode(buttonPin, INPUT_PULLUP);

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

  // Generar un Client ID único usando la MAC
  uint8_t mac[6];
  WiFi.macAddress(mac);
  String clientId = "Button-" + String(mac[4], HEX) + String(mac[5], HEX);
  mqttClient.connect(clientId.c_str());

  if (mqttClient.connected()) {
    Serial.println("Conectado al broker MQTT!");
  } else {
    Serial.println("Fallo al conectar al broker MQTT.");
  }
}

void loop() {
  if (!mqttClient.connected()) {
    Serial.println("Reconectando al broker MQTT...");
    uint8_t mac[6];
    WiFi.macAddress(mac);
    String clientId = "Button-" + String(mac[4], HEX) + String(mac[5], HEX);
    mqttClient.connect(clientId.c_str());

    if (mqttClient.connected()) {
      Serial.println("Reconexión exitosa!");
    } else {
      Serial.println("Fallo al reconectar al broker MQTT.");
      delay(5000);
    }
  }

  // Lectura del botón con debounce
  int buttonState = digitalRead(buttonPin);
  if (buttonState == LOW && lastButtonState == HIGH) {
    unsigned long currentTime = millis(); //Con delay no bloqueante
    if ((currentTime - lastDebounceTime) > debounceDelay) {
      Serial.println("Botón presionado! Enviando mensaje MQTT...");
      mqttClient.publish(topic, "wasPressed");
      lastDebounceTime = currentTime;
    }
  }
  lastButtonState = buttonState;

  mqttClient.loop();
}
