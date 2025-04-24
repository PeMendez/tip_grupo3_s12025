#include <IRremoteESP8266.h>
#include <IRrecv.h>
#include <IRsend.h>
#include <IRutils.h>

#include <WiFi.h>
#include <PubSubClient.h>

#include <map>
// Configuración de WiFi y MQTT
const char* ssid = "Rengo-AP";
const char* password = "Acm27pts"; //Santo y seña por ahora hardcoded
const char* mqttBroker = "broker.hivemq.com"; //test.mosquitto.org
const int mqttPort = 1883;
const char* topic = "unq/remote-control"; //rever nombres de los topics

// Pines GPIO
const uint16_t RECV_PIN = 15; // Pin para el receptor IR
const uint16_t SEND_PIN = 13;  // Pin para el emisor IR (LED IR)
//Nota: el pin 4 del ESP32 que estaba usando se quemó por accidente.

// Diccionario de comandos
std::map<String, uint64_t> nikaiCommands = {
    {"mute", 0xC0F3F}, 
    {"power", 0xD5F2A}, 
    {"vol_up", 0xD0F2F}, 
    {"vol_down", 0xD1F2E},
    {"ok", 0xBFF4}
    // Agrega más teclas aquí
};

WiFiClient espClient;
PubSubClient mqttClient(espClient);

// Tamaño del buffer para capturar señales IR
IRrecv irrecv(RECV_PIN, 1024, 50, true); // GPIO, Buffer, Timeout, True=Debug
decode_results results;
IRsend irsend(SEND_PIN);

unsigned long lastReconnectAttempt = 0;

// Función para manejar mensajes MQTT
void callback(char* topic, byte* payload, unsigned int length) {
  String message = "";
  for (unsigned int i = 0; i < length; i++) {
    message += (char)payload[i];
  }
  Serial.print("Mensaje recibido: ");
  Serial.println(message);

  // Busca el comando en el diccionario
  if (nikaiCommands.find(message) != nikaiCommands.end()) {
    uint64_t command = nikaiCommands[message];
    Serial.print("Enviando comando IR: ");
    Serial.println(command, HEX);
    irsend.sendNikai(command);
  } else {
    Serial.println("Comando desconocido");
  }
}

/*
void my_mqtt_connect(){
    // Generar un Client ID único usando la MAC
  uint8_t mac[6];
  WiFi.macAddress(mac);
  String clientId = "RemoteControl-" + String(mac[4], HEX) + String(mac[5], HEX);
  mqttClient.connect(clientId.c_str());

  if (mqttClient.connected()) {
    Serial.println("Conectado al broker MQTT!");
  } else {
    Serial.println("Fallo al conectar al broker MQTT.");
  }
}*/

// Función para reconectar al broker MQTT
bool reconnectMQTT() {
  // Generar un Client ID único usando la MAC
  uint8_t mac[6];
  WiFi.macAddress(mac);
  String clientId = "RemoteControl-" + String(mac[4], HEX) + String(mac[5], HEX);

  if (mqttClient.connect(clientId.c_str())) {
    Serial.println("Conectado al broker MQTT!");
    mqttClient.subscribe(topic);
    return true;
  } else {
    Serial.print("Error al conectar al broker MQTT: ");
    Serial.println(mqttClient.state());
    return false;
  }
}

// Función para reconexión WiFi
void ensureWiFiConnected() {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Reconectando a WiFi...");
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
      delay(500);
      Serial.print(".");
    }
    Serial.println("\nWiFi reconectado!");
  }
}

void setup() {
  Serial.begin(115200);

  // Configura el receptor IR
  irrecv.enableIRIn();
  Serial.println("Receptor IR listo para capturar señales");

  // Configura el emisor IR
  irsend.begin();
  Serial.println("Emisor IR configurado");

  // Conexión a WiFi
  Serial.println("Conectando a WiFi...");
  WiFi.begin(ssid, password);
  ensureWiFiConnected();

  // Configuración del cliente MQTT
  mqttClient.setServer(mqttBroker, mqttPort);
  mqttClient.setCallback(callback);
  
  // Conexión inicial al broker
  if (!reconnectMQTT()) {
    Serial.println("No se pudo conectar al broker MQTT en el inicio.");
  }
}

void loop() {

  // Verifica la conexión WiFi
  ensureWiFiConnected();

  // Verifica la conexión al broker MQTT
  if (!mqttClient.connected()) {
    unsigned long now = millis();
    if (now - lastReconnectAttempt > 5000) { // Intentar reconexión cada 5 segundos
      lastReconnectAttempt = now;
      if (reconnectMQTT()) {
        lastReconnectAttempt = 0;
      }
    }
  } else {
    mqttClient.loop();
  }

  // Verifica si se recibe una señal IR
  if (irrecv.decode(&results)) {
    Serial.println("Señal capturada!");
    // Imprime detalles del protocolo, address y comando
    Serial.print("PROTOCOLO: ");
    Serial.println(typeToString(results.decode_type));
    if (!results.address && !results.command){
      Serial.print("ADDRESS: ");
      Serial.println(results.address, HEX);
      Serial.print("COMANDO: ");
      Serial.println(results.command, HEX);
    }
    // Imprime el valor completo capturado
    Serial.print("RAW VALUE: ");
    serialPrintUint64(results.value, HEX);
    Serial.println();

    irrecv.resume(); // Listo para recibir la siguiente señal
  }
}
