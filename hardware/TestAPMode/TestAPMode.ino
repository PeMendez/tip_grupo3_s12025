#include <WiFiManager.h>
#include <PubSubClient.h>

#define LED_BUILTIN 2

WiFiClient espClient;
PubSubClient mqttClient(espClient);

const char* mqtt_server = "test.mosquitto.org";//"broker.hivemq.com";
const int mqtt_port = 1883;
const char* topic = "LEDctrl";

bool ledState = LOW;
bool isSubscribed = false;

void callback(char* topic, byte* payload, unsigned int length) {
    String message;
    for (unsigned int i = 0; i < length; i++) {
        message += (char)payload[i];
    }

    if (message == "toggle") {
        ledState = !ledState;
        digitalWrite(LED_BUILTIN, ledState);
        Serial.println(ledState ? "LED ENCENDIDO" : "LED APAGADO");
    }
}

void setup() {
    Serial.begin(115200);
    pinMode(LED_BUILTIN, OUTPUT);
    digitalWrite(LED_BUILTIN, ledState);

    uint8_t mac[6];
    WiFi.macAddress(mac);
    String apName = "NeoNode-" + String(mac[4], HEX) + String(mac[5], HEX);

    WiFiManager wm;
    if (!wm.autoConnect(apName.c_str(), "testpassword")) {
        Serial.println("Falló la configuración de WiFi");
        delay(3000);
        ESP.restart();
    }

    mqttClient.setServer(mqtt_server, mqtt_port);
    mqttClient.setCallback(callback);

    connectToMQTT();
}

void connectToMQTT() {

    uint8_t mac[6];
    WiFi.macAddress(mac);
    String clientId = "NeoNode-" + String(mac[4], HEX) + String(mac[5], HEX);

    static unsigned long lastAttemptTime = 0;
    if (millis() - lastAttemptTime < 5000) {
        return; // Espera 5 segundos entre intentos de reconexión
    }
    lastAttemptTime = millis();

    if (!mqttClient.connected()) {
        Serial.println("Conectando al broker MQTT...");
        if (mqttClient.connect(clientId.c_str())) {
            Serial.println("Conectado!");
            if (mqttClient.subscribe(topic)) {
                Serial.print("Suscrito al tópico: ");
                Serial.println(topic);
                isSubscribed = true;
            } else {
                Serial.println("Error al suscribirse al tópico");
            }
        } else {
            Serial.print("Error de conexión. Estado: ");
            Serial.println(mqttClient.state());
        }
    }
}

void loop() {
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("WiFi desconectado. Intentando reconectar...");
        WiFi.reconnect();
        delay(1000);
        return;
    }

    if (!mqttClient.connected()) {
        isSubscribed = false;
        connectToMQTT();
    }

    mqttClient.loop();
}
