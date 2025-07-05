#include <DHT.h>
#include <SPI.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SH110X.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <LittleFS.h>

// --- Configuración de Hardware ---
#define DHTPIN 26
#define DHTTYPE DHT11

#define I2C_ADDRESS 0x3C // Dirección I2C del display
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1

DHT dht(DHTPIN, DHTTYPE);
Adafruit_SH1106G display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

const int RESET_BUTTON_PIN = 23;
const char* device_type = "temperature_sensor";

const unsigned char logo[] PROGMEM = {
	// 'logo mono v2, 128x64px
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x60, 0x00, 0x13, 0xff, 0xfc, 0x3f, 0x80, 0x00, 0x10, 0x00, 0x09, 0x00, 0x00, 0x4f, 0xff, 0xfc, 
	0x50, 0x00, 0x11, 0x00, 0x00, 0xc0, 0x60, 0x00, 0x10, 0x00, 0x09, 0x00, 0x00, 0x48, 0x00, 0x0c, 
	0x4c, 0x00, 0x10, 0x80, 0x01, 0x00, 0x30, 0x00, 0x10, 0x00, 0x09, 0x00, 0x00, 0x48, 0x00, 0x18, 
	0x46, 0x00, 0x10, 0x40, 0x02, 0x00, 0x08, 0x00, 0x10, 0x00, 0x09, 0x00, 0x00, 0x48, 0x00, 0x30, 
	0x43, 0x00, 0x10, 0x20, 0x06, 0x00, 0x08, 0x00, 0x10, 0x00, 0x09, 0x00, 0x00, 0x48, 0x00, 0x60, 
	0x41, 0x80, 0x10, 0x10, 0x04, 0x00, 0x84, 0x00, 0x10, 0x00, 0x09, 0x00, 0x00, 0x48, 0x00, 0xc0, 
	0x40, 0xc0, 0x10, 0x88, 0x08, 0x04, 0x44, 0x00, 0x10, 0x00, 0x09, 0x00, 0x00, 0x43, 0x01, 0x80, 
	0x40, 0x60, 0x11, 0xc4, 0x08, 0x0e, 0x02, 0x00, 0x10, 0x18, 0x09, 0x00, 0x00, 0x47, 0x82, 0x00, 
	0x40, 0x30, 0x13, 0x32, 0x08, 0x19, 0x82, 0x00, 0x17, 0xe7, 0xe9, 0x00, 0x00, 0x4c, 0xff, 0xe0, 
	0x40, 0x18, 0x11, 0x22, 0x08, 0x09, 0x02, 0x00, 0x10, 0x64, 0x09, 0x00, 0x00, 0x44, 0x80, 0x10, 
	0x40, 0x0c, 0x10, 0xc4, 0x08, 0x06, 0x02, 0x00, 0x10, 0x18, 0x09, 0x00, 0x00, 0x43, 0x00, 0x08, 
	0x40, 0x02, 0x10, 0x08, 0x04, 0x00, 0x46, 0x00, 0x10, 0x00, 0x09, 0x80, 0x00, 0x40, 0x00, 0x04, 
	0x40, 0x01, 0x10, 0x10, 0x04, 0x00, 0x04, 0x00, 0x10, 0x00, 0x09, 0x80, 0x00, 0xc8, 0x00, 0x04, 
	0x40, 0x00, 0x90, 0x20, 0x06, 0x00, 0x08, 0x00, 0x10, 0x00, 0x08, 0xc0, 0x01, 0xc8, 0x00, 0x04, 
	0x40, 0x00, 0x50, 0x40, 0x02, 0x00, 0x18, 0x00, 0x10, 0x00, 0x08, 0x60, 0x01, 0x48, 0x00, 0x04, 
	0x40, 0x00, 0x30, 0x80, 0x01, 0x80, 0x30, 0x00, 0x10, 0x00, 0x08, 0x30, 0x06, 0x48, 0x00, 0x0c, 
	0x40, 0x00, 0x11, 0x00, 0x00, 0xe1, 0xc0, 0x00, 0x10, 0x00, 0x08, 0x0c, 0x1c, 0x48, 0x00, 0x18, 
	0x40, 0x00, 0x13, 0xff, 0xfc, 0x1f, 0x00, 0x00, 0x10, 0x00, 0x08, 0x03, 0xe0, 0x4f, 0xff, 0xe0, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
};

const int TEMP_HISTORY_SIZE = 5;
const float TEMP_PUBLISH_THRESHOLD = 0.5;

float tempHistory[TEMP_HISTORY_SIZE] = {24.0F, NAN, NAN, NAN, NAN}; // Historial de temperaturas
int tempIndex = 0;
float lastPublishedTemp = -99.0;
int startTime;
int screenRefreshTime;


// --- Configuración de Red y MQTT ---
const char* ssid = "Rengo-AP";     // Tu SSID de WiFi
const char* password = "Acm27pts"; // Tu contraseña de WiFi
const char* mqttBroker = "192.168.1.56";
const int mqttPort = 1883;
const char* initialTopic = "neohub/unconfigured";

// --- Variables Globales de Estado y Configuración ---
struct DeviceConfig {
  char device_topic[128]; // Almacena el tópico BASE del dispositivo
  char mac_address[18];   // Almacena la MAC como string
  bool is_configured;
};

DeviceConfig config;

WiFiClient espClient;
PubSubClient mqttClient(espClient);
//-- factory reset
unsigned long buttonPressStartTime = 0;
const long resetHoldTime = 5000;

// --- Declaración de Funciones Modulares ---
void setup_wifi();
void load_configuration();
void save_configuration();
void connect_to_mqtt();
void publish_device_info();
void publish_current_state(float temp_to_publish);
void handle_configuration_message(JsonObject doc);
void factory_reset();
void handle_command_message(JsonObject doc);
void callback(char* topic, byte* payload, unsigned int length);
void show_logo();
void oled_begin();
void get_sensor_data();
void check_and_publish_sensor_data();
float get_last_valid_reading();
void update_display();

// =================================================================
// FUNCIÓN SETUP PRINCIPAL
// =================================================================
void setup() {
  Serial.begin(115200);
  dht.begin();
  oled_begin();
  
  pinMode(RESET_BUTTON_PIN, INPUT_PULLUP);

  if (!LittleFS.begin(true)) {
    Serial.println("Ocurrió un error al montar LittleFS");
    return;
  }
  
  setup_wifi();
  load_configuration();
  mqttClient.setServer(mqttBroker, mqttPort);
  mqttClient.setCallback(callback);
  startTime=millis();
  screenRefreshTime=millis();
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
  // TODO: leer el sensor!!
  get_sensor_data();
  // Sigo mostrando el logo mientras se llena buffer de temp.
  if (millis() - startTime < 3000) {
    return;  // Esperamos sin hacer nada visible
  }
  if (millis() - screenRefreshTime > 2000 && config.is_configured){
    check_and_publish_sensor_data();
    screenRefreshTime = millis();
  }
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
void show_logo() {
  display.clearDisplay();
  display.drawBitmap(0, 0, logo, 128, 64, SH110X_WHITE);
  display.display();
}

void oled_begin(){
    // Inicializar la pantalla OLED
  if (!display.begin(I2C_ADDRESS, OLED_RESET)) {
    Serial.println(F("Error al iniciar el display OLED"));
    while (1);
  }
  show_logo();
}

void check_and_publish_sensor_data(){
  // Obtener la última lectura válida del historial
  float currentValidTemp = get_last_valid_reading();
  float currentHumidity = dht.readHumidity(); // Leer humedad actual

  // Actualizar la pantalla
  update_display(currentValidTemp, currentHumidity);

  // Decidir si publicar el nuevo valor
  // Solo publicar si la temperatura es válida (no es NaN)
  if (!isnan(currentValidTemp)) {
    // Comprobar si el cambio supera el umbral definido
    if (abs(currentValidTemp - lastPublishedTemp) >= TEMP_PUBLISH_THRESHOLD) {
      Serial.printf("Cambio de temperatura significativo (%.1f -> %.1f). Publicando...\n", lastPublishedTemp, currentValidTemp);
      
      // Actualizar el valor en la clase antes de publicar
      // (Suponiendo que tu clase TemperatureSensorDevice se llama 'device_instance')
      // device_instance.update_temperature(currentValidTemp); <--- Esta es la forma ideal si usas la clase.
      
      // Si no usas la clase, al menos actualiza la variable global y publica.
      // Pero la forma correcta es encapsularlo en el método de la clase.
      publish_current_state(currentValidTemp); // Modificamos publish_current_state para que acepte el valor

      lastPublishedTemp = currentValidTemp; // Actualizar el último valor publicado
    } else {
      Serial.printf("Cambio de temperatura (%.1f -> %.1f) no supera el umbral de %.1f. No se publica.\n", lastPublishedTemp, currentValidTemp, TEMP_PUBLISH_THRESHOLD);
    }
  }
}

void get_sensor_data(){
  float temp = dht.readTemperature();
  //float sendTemp = temp;

  // Guardar el valor en el array circular
  tempHistory[tempIndex] = temp;
  tempIndex = (tempIndex + 1) % TEMP_HISTORY_SIZE; // Mantener el índice dentro de los bounds del array.
}

float get_last_valid_reading() {
  // Busca hacia atrás en el buffer circular la última lectura que no sea NaN
  for (int i = 0; i < TEMP_HISTORY_SIZE; i++) {
    int idx = (tempIndex - 1 - i + TEMP_HISTORY_SIZE) % TEMP_HISTORY_SIZE;
    if (!isnan(tempHistory[idx])) {
      return tempHistory[idx];
    }
  }
  return NAN; // Devolver NaN si no se encontró ninguna lectura válida
}

void update_display(float temp, float humidity) {
  display.clearDisplay();
  
  if (isnan(temp)) {
    // Mostrar mensaje de error si no hay temperatura válida
    display.setTextSize(2);
    display.setTextColor(SH110X_WHITE);
    display.setCursor(5, 20);
    display.print("Esperando");
    display.setCursor(25, 45);
    display.print("sensor");
  } else {
    // Mostrar la temperatura
    display.setTextSize(2);
    display.setTextColor(SH110X_WHITE);
    display.setCursor(35, 15);
    display.print(temp, 1);
    display.print(" C");

    // Mostrar la humedad
    if (!isnan(humidity)) {
      display.setTextSize(1);
      display.setCursor(35, 45);
      display.print("Humedad: ");
      display.print(humidity, 0);
      display.print("%");
    }
  }
  display.display();
}

void load_configuration() {
  // Generar la MAC address una sola vez y guardarla
  uint8_t mac[6];
  WiFi.macAddress(mac);
  snprintf(config.mac_address, sizeof(config.mac_address), "%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
  
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
  Serial.printf("MAC: %s\n", config.mac_address);
  Serial.printf("Tópico Base: %s\n", config.device_topic);
  Serial.printf("Configurado: %s\n", config.is_configured ? "Sí" : "No");
  Serial.println("--------------------------");
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
  // Guardamos también la temperatura para recordarla al reiniciar
  doc["temperature"] = tempHistory[tempIndex];

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
    String clientId = "ESP32-";
    clientId += String(config.mac_address[1], HEX) + String(config.mac_address[2], HEX); //String(config.mac_address);
    
    if (mqttClient.connect(clientId.c_str())) {
      Serial.println("¡Conectado!");

      if (config.is_configured) {
        String command_topic = String(config.device_topic) + "/command";
        mqttClient.subscribe(command_topic.c_str());
        Serial.println("Suscrito a tópico de comandos: " + command_topic);
        publish_current_state(lastPublishedTemp); // Publicar estado al reconectar
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
  doc["name"] = String(device_type) + "_" + String(config.mac_address[1], HEX) + String(config.mac_address[2], HEX);//String(config.mac_address);
  doc["type"] = String(device_type);
  doc["mac_address"] = config.mac_address;
  // Aquí podrías añadir más información si quisieras
  
  String output;
  serializeJson(doc, output);
  mqttClient.publish(initialTopic, output.c_str(), true); // retain = true
  Serial.println("Información del dispositivo publicada.");
}

void publish_current_state(float temp_to_publish) {
  if (!config.is_configured) return; // No publicar estado si no está configurado

  JsonDocument doc;
  doc["mac_address"] = config.mac_address;
  doc["temperature"] = temp_to_publish; // Enviar valor de temp
  doc["unit"]="C"; //solo centigrados por ahora.
  doc["timestamp"] = time(nullptr);

  String output;
  serializeJson(doc, output);
  // Publica en el tópico base
  mqttClient.publish(config.device_topic, output.c_str(), true); // retain = true
  Serial.println("Temperatura actual publicada: " + output);
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
  // No hay comandos especificos para temperature_sensor
}