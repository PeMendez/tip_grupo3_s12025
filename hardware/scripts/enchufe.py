import paho.mqtt.client as mqtt
import time
import json
import os
import random

# Configuración del broker MQTT y tópicos
MQTT_BROKER = "broker.hivemq.com"
MQTT_PORT = 1883
INITIAL_TOPIC = "neohub/unconfigured"
CONFIG_FILE = "device_config.json"

# Generar un identificador único simulado (últimos bits de MAC)
mac_address = f"{random.randint(0, 255):02X}{random.randint(0, 255):02X}"

# Estado inicial del "dispositivo"
relay_state = False  # Simula el estado del relay
current_topic = INITIAL_TOPIC  # Tópico actual del dispositivo

# Función para guardar la configuración en un archivo
def save_config(config):
    try:
        with open(CONFIG_FILE, "w") as file:
            json.dump(config, file)
        print("Configuración guardada en el archivo.")
    except IOError as e:
        print(f"Error al guardar la configuración: {e}")

# Función para cargar la configuración desde un archivo
def load_config():
    if os.path.exists(CONFIG_FILE):
        try:
            with open(CONFIG_FILE, "r") as file:
                config = json.load(file)
            print("Configuración cargada desde el archivo.")
            return config
        except (IOError, json.JSONDecodeError) as e:
            print(f"Error al cargar la configuración: {e}")
    else:
        print("No se encontró un archivo de configuración previo.")
    return None

# Función para manejar mensajes MQTT
def on_message(client, userdata, msg):
    global relay_state, new_topic
    print(f"Mensaje recibido en {msg.topic}: {msg.payload.decode()}")

    try:
        # Determinar si el mensaje es para configuración o comandos
        if msg.topic == INITIAL_TOPIC:
            # Intentar interpretar como JSON (configuración)
            config_message = json.loads(msg.payload.decode())
            
            # Validar si el mensaje contiene el identificador correcto
            if config_message.get("mac_address") == mac_address and "new_topic" in config_message:
                print(f"Configuración válida recibida para este dispositivo: {config_message}")
                new_topic = config_message.get("new_topic")
                if new_topic:
                    client.subscribe(new_topic)
                    print(f"Suscrito al nuevo tópico: {new_topic}")
                    save_config({"new_topic": new_topic, "mac_address": mac_address})
                    print("Guardada la configuración a archivo")
            else:
                print("El mensaje de configuración no está destinado a este dispositivo. Ignorando.")
        elif msg.topic == new_topic:
            # Procesar comandos (texto plano)
            command = msg.payload.decode()
            if command == "turn_on":
                relay_state = True
                print("Comando recibido: ENCENDER. Estado del relay: ENCENDIDO")
            elif command == "turn_off":
                relay_state = False
                print("Comando recibido: APAGAR. Estado del relay: APAGADO")
            elif command == "toggle":
                relay_state = not relay_state
                print(f"Comando recibido: ALTERNAR. Estado del relay: {'ENCENDIDO' if relay_state else 'APAGADO'}")
            else:
                print(f"Comando desconocido: {command}")
        else:
            print("El mensaje no está destinado a este dispositivo. Ignorando.")
    except json.JSONDecodeError:
        print("Mensaje no válido recibido. Ignorando.")

# Función para manejar la conexión
def on_connect(client, userdata, flags, rc, properties=None):
    global new_topic  # Para acceder a la configuración cargada

    if rc == 0:
        print("Conexión exitosa al broker MQTT")

        if new_topic:  # Si ya hay un tópico configurado
            client.subscribe(new_topic)
            print(f"Suscrito al tópico configurado previamente: {new_topic}")
        else:  # Si no hay configuración previa, usar el tópico inicial
            client.subscribe(INITIAL_TOPIC)
            print(f"Suscrito al tópico inicial: {INITIAL_TOPIC} (es mi primer dia)")

            # Publicar mensaje inicial con el identificador único
            device_info = {
                "name": "SimulatedRelay",
                "type": "smart_outlet",
                "mac_address": mac_address,  # Incluir identificador único
            }
            client.publish(INITIAL_TOPIC, json.dumps(device_info))
            print(f"Publicado mensaje inicial en {INITIAL_TOPIC}: {device_info}")
    else:
        print(f"Error en la conexión al broker, código de error: {rc}")

# Cargar configuración inicial
config = load_config()
if config:
    new_topic = config.get("new_topic", None)
else:
    new_topic = None

# Crear el cliente MQTT
client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, "SimulatedRelay", clean_session=True, userdata=None)
client.on_connect = on_connect
client.on_message = on_message

# Conectar al broker
print("Conectando al broker MQTT...")
client.connect(MQTT_BROKER, MQTT_PORT, keepalive=60)

# Mantener el cliente activo
try:
    client.loop_start()
    while True:
        time.sleep(1)  # Simula el dispositivo funcionando continuamente
except KeyboardInterrupt:
    print("Desconectando del broker MQTT...")
    client.loop_stop()
    client.disconnect()
