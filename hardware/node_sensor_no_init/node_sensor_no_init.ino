#include <WiFi.h>
#include <PubSubClient.h>

// Configuración de WiFi y MQTT
const char* ssid = "Rengo-AP";
const char* password = "Acm27pts"; //Santo y seña por ahora hardcoded
const char* mqttBroker = "test.mosquitto.org";
const int mqttPort = 1883;
const char* topic = "unq-button"; //rever nombres de los topics

WiFiClient espClient;
PubSubClient mqttClient(espClient);

// Configuración del sensor
const int sensorPin = 26;
bool lastSensorState = HIGH;
unsigned long lastDebounceTime = 0;
const unsigned long debounceDelay = 50;  // ms para antirrebote

void setup() {
  Serial.begin(115200);

  // Configuración del botón como entrada con pull-up
  pinMode(sensorPin, INPUT_PULLUP);

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
  String clientId = "Sensor-" + String(mac[4], HEX) + String(mac[5], HEX);
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
    String clientId = "Sensor-" + String(mac[4], HEX) + String(mac[5], HEX);
    mqttClient.connect(clientId.c_str());

    if (mqttClient.connected()) {
      Serial.println("Reconexión exitosa!");
    } else {
      Serial.println("Fallo al reconectar al broker MQTT.");
      delay(5000);
    }
  }

  // Lectura del sensor con debounce
  int sensorState = digitalRead(sensorPin); //lee estado actual

  if (sensorState != lastSensorState) { //detecta si cambió
    unsigned long currentTime = millis(); //toma el tiempo 
    if ((currentTime - lastDebounceTime) > debounceDelay) {
    //si pasó el tiempo de rebote
      if (sensorState == HIGH) {
        Serial.println("Puerta cerrada (haz interrumpido)");
        //enviar mensaje si se cerro ? 
      } else {
        Serial.println("Puerta abierta (haz no interrumpido)");
        mqttClient.publish(topic, "wasPressed"); //mensaje que alerta que se abrio la puerta.
      }
      lastDebounceTime = currentTime; //actualiza el tiempo
    }
  }
  lastSensorState = sensorState; //actualiza el último estado

  mqttClient.loop();
}
