#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <LittleFS.h>

// --- Configuración de Hardware ---
const int LIGHT_PIN = 13; //ver que pin conviene usar.
const int RESET_BUTTON_PIN = 23;
const int LED_CHANNEL = 0;
const int FREQ = 5000; //5khz
const int RESOLUTION = 8; // ocho bits 0-255 

// --- Configuración de Red y MQTT ---
const char* ssid = "Rengo-AP";     // Tu SSID de WiFi
const char* password = "Acm27pts"; // Tu contraseña de WiFi
const char* mqttBroker = "192.168.1.56";
const int mqttPort = 1883;
const char* initialTopic = "neohub/unconfigured";
const char* device_type = "dimmer";
const char* device_name_prefix = "luz_";

// --- Variables Globales de Estado y Configuración ---
struct DeviceConfig {
  char device_name[64];
  char device_topic[128]; // Almacena el tópico BASE del dispositivo
  char mac_address[18];   // Almacena la MAC como string
  bool is_configured;
};

DeviceConfig config;
uint8_t dutyCycle = 0;

WiFiClient espClient;
PubSubClient mqttClient(espClient);

unsigned long buttonPressStartTime = 0;
const long resetHoldTime = 5000;

// --- Declaración de Funciones Modulares ---
void setup_wifi();
void load_configuration();
void save_configuration();
void connect_to_mqtt();
void publish_device_info();
void publish_current_state();
void handle_configuration_message(JsonObject doc);
void factory_reset();
void handle_command_message(JsonObject doc);
void callback(char* topic, byte* payload, unsigned int length);
uint8_t dutyToPercentage(uint8_t dutyCycle);
uint8_t percentageToDuty(uint8_t percentage);

// =================================================================
// FUNCIÓN SETUP PRINCIPAL
// =================================================================
void setup() {
  Serial.begin(115200);
  int channelPWM = ledcAttach(LIGHT_PIN, FREQ, RESOLUTION);
  if (channelPWM < 0){
    Serial.println("Error al configurar PWM.");
  } else {
    Serial.printf("PWM al canal: %d pin usado: %d\n", channelPWM, LIGHT_PIN);
  }
  //ledcAttach(LIGHT_PIN, LED_CHANNEL);
  pinMode(RESET_BUTTON_PIN, INPUT_PULLUP);

  if (!LittleFS.begin(true)) {
    Serial.println("Ocurrió un error al montar LittleFS");
    return;
  }
  
  setup_wifi();
  load_configuration();
  mqttClient.setServer(mqttBroker, mqttPort);
  mqttClient.setCallback(callback);
}

// =================================================================
// FUNCIÓN LOOP PRINCIPAL
// =================================================================
void loop() {
  if (!mqttClient.connected()) {
    connect_to_mqtt();
  }
  mqttClient.loop();
  // Aquí no se pone lógica bloqueante
  // --- Lógica para el botón de reset físico (no bloqueante) ---
  // El pin leerá HIGH normalmente (pull-up) y LOW cuando se presiona.
  if (digitalRead(RESET_BUTTON_PIN) == LOW) {
    if (buttonPressStartTime == 0) {
      // El botón se acaba de presionar
      buttonPressStartTime = millis();
      Serial.println("Botón de reset presionado. Mantener presionado para resetear...");
    } else if (millis() - buttonPressStartTime > resetHoldTime) {
      // El botón se ha mantenido presionado el tiempo suficiente
      factory_reset();
    }
  } else {
    if (buttonPressStartTime > 0) {
      // El botón se soltó antes de tiempo
      Serial.println("Reseteo cancelado.");
      buttonPressStartTime = 0;
    }
  }
}

// =================================================================
// IMPLEMENTACIÓN DE FUNCIONES MODULARES
// =================================================================

void load_configuration() {
  // Generar la MAC address una sola vez y guardarla
  uint8_t mac[6];
  WiFi.macAddress(mac);
  snprintf(config.mac_address, sizeof(config.mac_address), "%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
  // Generar el nombre una sola vez y guardarlo
  String name = ""; 
  name = String(device_name_prefix) + String(config.mac_address[4], HEX) + String(config.mac_address[5], HEX);
  //snprintf(config.device_name, sizeof(config.device_name), (String(device_name_prefix) + String(config.mac_address[4], HEX) + String(config.mac_address[5], HEX)));
  name.toCharArray(config.device_name, sizeof(config.device_name));
  File configFile = LittleFS.open("/config.json", "r");
  if (!configFile) {
    Serial.println("No se encontró config.json. Creando configuración por defecto (primera ejecución).");
    config.is_configured = false;
    strcpy(config.device_topic, initialTopic);
    save_configuration();
  } else {
    JsonDocument doc;
    DeserializationError error = deserializeJson(doc, configFile);
    if (error) {
      Serial.println("Fallo al leer config.json, usando defaults.");
      config.is_configured = false;
      strcpy(config.device_topic, initialTopic);
    } else {
      strcpy(config.device_topic, doc["device_topic"]);
      config.is_configured = doc["is_configured"];
      Serial.println("Configuración cargada exitosamente.");
    }
    configFile.close();
  }
  Serial.println("--- Configuración Actual ---");
  Serial.printf("Nombre: %s\n", config.device_name);
  Serial.printf("MAC: %s\n", config.mac_address);
  Serial.printf("Tópico Base: %s\n", config.device_topic);
  Serial.printf("Configurado: %s\n", config.is_configured ? "Sí" : "No");
  Serial.println("--------------------------");
}

uint8_t dutyToPercentage(uint8_t dutyCycle) {
  return uint8_t((dutyCycle / 255.0) * 100.0);
}
uint8_t percentageToDuty(uint8_t percentage) {
  percentage = constrain(percentage, 0, 100); // limita el rango
  return round((percentage / 100.0) * 255.0);
}


void save_configuration() {
  File configFile = LittleFS.open("/config.json", "w");
  if (!configFile) {
    Serial.println("Fallo al abrir config.json para escritura");
    return;
  }

  JsonDocument doc;
  doc["device_topic"] = config.device_topic;
  doc["is_configured"] = config.is_configured;
  // Guardamos también el estado del relé para recordarlo al reiniciar
  doc["brightness"] = dutyToPercentage(dutyCycle);

  if (serializeJson(doc, configFile) == 0) {
    Serial.println("Fallo al escribir en config.json");
  } else {
    Serial.println("Configuración guardada en config.json");
  }
  configFile.close();
}

void factory_reset() {
  Serial.println("!!! INICIANDO RESET DE FÁBRICA !!!");
  
  // Borrar el archivo de configuración de LittleFS
  if (LittleFS.exists("/config.json")) {
    LittleFS.remove("/config.json");
    Serial.println("Archivo de configuración borrado.");
  } else {
    Serial.println("No existía archivo de configuración para borrar.");
  }
  
  // Aquí podrías añadir el borrado de otros archivos si los tuvieras.
  
  // Dar una señal visual (opcional, si tienes un LED)
  // por ejemplo, parpadear rápidamente un LED durante 2 segundos.
  
  Serial.println("Reseteo completado. Reiniciando el dispositivo...");
  delay(1000);
  ESP.restart();
}

void setup_wifi() {
  Serial.print("Conectando a WiFi...");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi conectado!");
  Serial.print("Dirección IP: ");
  Serial.println(WiFi.localIP());
}

void connect_to_mqtt() {
  while (!mqttClient.connected()) {
    Serial.print("Intentando conexión MQTT...");
    String clientId = device_name_prefix;
    clientId += String(config.mac_address[1], HEX) + String(config.mac_address[2], HEX); //String(config.mac_address);
    
    if (mqttClient.connect(clientId.c_str())) {
      Serial.println("¡Conectado!");

      if (config.is_configured) {
        String command_topic = String(config.device_topic) + "/command";
        mqttClient.subscribe(command_topic.c_str());
        Serial.println("Suscrito a tópico de comandos: " + command_topic);
        publish_current_state(); // Publicar estado al reconectar
      } else {
        mqttClient.subscribe(initialTopic);
        Serial.println("Suscrito a tópico inicial: " + String(initialTopic));
        publish_device_info(); // Publicar info si no está configurado
      }
    } else {
      Serial.print("falló, rc=");
      Serial.print(mqttClient.state());
      Serial.println(" intentando de nuevo en 5 segundos");
      delay(5000);
    }
  }
}

void publish_device_info() {
  JsonDocument doc;
  doc["name"] = config.device_name;
  doc["type"] = device_type;
  doc["mac_address"] = config.mac_address;
  // Aquí podrías añadir más información si quisieras
  
  String output;
  serializeJson(doc, output);
  mqttClient.publish(initialTopic, output.c_str(), true); // retain = true
  Serial.println("Información del dispositivo publicada.");
}

void publish_current_state() {
  if (!config.is_configured) return; // No publicar estado si no está configurado

  JsonDocument doc;
  doc["mac_address"] = config.mac_address;
  doc["brightness"] = dutyToPercentage(dutyCycle); // Enviar porcentaje
  doc["timestamp"] = time(nullptr);

  String output;
  serializeJson(doc, output);
  // Publica en el tópico base
  mqttClient.publish(config.device_topic, output.c_str(), true); // retain = true
  Serial.println("Estado actual publicado: " + output);
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Mensaje recibido en [");
  Serial.print(topic);
  Serial.print("]: ");
  
  JsonDocument doc;
  DeserializationError error = deserializeJson(doc, payload, length);

  if (error) {
    Serial.print("deserializeJson() falló: ");
    Serial.println(error.c_str());
    return;
  }

  // Verificar MAC
  if (!doc.containsKey("mac_address") || strcmp(doc["mac_address"], config.mac_address) != 0) {
    Serial.println("MAC no coincide o no presente. Ignorando mensaje.");
    return;
  }

  // Rutear mensaje a la función de manejo correspondiente
  if (doc.containsKey("new_topic")) {
    handle_configuration_message(doc.as<JsonObject>());
  } else if (doc.containsKey("command")) {
    handle_command_message(doc.as<JsonObject>());
  } else {
    Serial.println("Mensaje JSON sin 'new_topic' ni 'command'. Ignorando.");
  }
}

void handle_configuration_message(JsonObject doc) {
  const char* new_topic = doc["new_topic"];

  // Desconfiguración
  if (strcmp(new_topic, initialTopic) == 0) {
    if (config.is_configured) {
      Serial.println("Recibida orden de desconfiguración.");
      config.is_configured = false;
      strcpy(config.device_topic, initialTopic);
      save_configuration();
      Serial.println("Dispositivo desconfigurado. Reiniciando para aplicar cambios...");
      delay(1000);
      ESP.restart(); // La forma más fácil de cambiar suscripciones es reiniciar
    }
  } 
  // Configuración
  else {
    if (!config.is_configured || strcmp(config.device_topic, new_topic) != 0) {
      Serial.printf("Recibida nueva configuración. Tópico base: %s\n", new_topic);
      config.is_configured = true;
      strcpy(config.device_topic, new_topic);
      save_configuration();
      Serial.println("Dispositivo configurado. Reiniciando para aplicar cambios...");
      delay(1000);
      ESP.restart(); // Reiniciar para suscribirse al nuevo command_topic
    }
  }
}

void handle_command_message(JsonObject doc) {
  const char* command = doc["command"];
  int new_brightness = -1;

  // Comando genérico ACK
  if (strcmp(command, "ack") == 0) {
    Serial.println("Comando 'ack' recibido. Enviando respuesta.");
    JsonDocument ack_response;
    ack_response["mac_address"] = config.mac_address;
    ack_response["response_to_command"] = "ack";
    ack_response["status"] = "ack_received";
    ack_response["timestamp"] = time(nullptr);
    String output;
    serializeJson(ack_response, output);
    mqttClient.publish(config.device_topic, output.c_str()); // Publica en el tópico base
    return;
  }

  // --- Manejo del comando de reseteo ---
  if (strcmp(command, "factory_reset") == 0) {
    Serial.println("Comando 'factory_reset' recibido por MQTT.");
    // Opcional: Publicar un mensaje de confirmación antes de resetear
    mqttClient.publish(config.device_topic, "{\"status\":\"resetting_in_3s\"}");
    delay(3000); // Dar tiempo a que el mensaje se envíe
    factory_reset(); // Llamar a la misma función de reseteo
    return; // No continuar con otros comandos
  }

  // Comandos específicos del Dimmer
  bool state_changed = false;
  if (strcmp(command, "set_brightness") == 0) {
    Serial.println("Comando de brillo recibido. Seteando brillo...");
    if (!doc.containsKey("parameters")) {
      Serial.println("Error: comando 'set_brightness' sin 'parameters'");
      return;
    }
    new_brightness = doc["parameters"].as<int>();
    uint8_t new_duty = percentageToDuty(new_brightness);
    Serial.printf("Brillo recibido: %d => Duty: %d\n", new_brightness, new_duty);
    if (dutyCycle != new_duty) {
      dutyCycle = new_duty;
      state_changed = true;
    }
  } else {
    Serial.printf("Comando desconocido: %s\n", command);
  }

  if (state_changed) {
    Serial.printf("Brillo cambiado a: %d\n", new_brightness);
    ledcWrite(LIGHT_PIN, dutyCycle);
    publish_current_state(); // Publicar nuevo estado
    save_configuration();    // Guardar también el estado en la config
  }
  else {
    Serial.println("No cambió el brillo...");
  }
}