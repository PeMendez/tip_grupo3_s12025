import paho.mqtt.client as mqtt
import time
import json
import os
import random
import argparse

# Configuración del broker MQTT y tópicos
MQTT_BROKER = "broker.hivemq.com"
MQTT_PORT = 1883
INITIAL_TOPIC = "neohub/unconfigured" # Tópico para dispositivos no configurados

# --- Configuración de Argumentos ---
parser = argparse.ArgumentParser(description="Simulador de Smart Outlet MQTT.")
parser.add_argument("--id", required=True, help="Identificador único para esta instancia del dispositivo (ej: outlet1)")
args = parser.parse_args()

DEVICE_INSTANCE_ID = args.id
CONFIG_FILE = f"device_config_{DEVICE_INSTANCE_ID}.json" # Nombre de archivo único

# --- Variables Globales de Estado del Dispositivo ---
relay_state = False
current_mqtt_topic = INITIAL_TOPIC
is_configured = False
device_mac_address = None
first_ever_run = False # Nueva flag: se pondrá a True si no hay config file al inicio

# --- Funciones Auxiliares ---
def save_config(topic_to_save, mac_to_save):
    """Guarda la configuración (tópico y MAC) en un archivo JSON."""
    try:
        with open(CONFIG_FILE, "w") as file:
            json.dump({"device_topic": topic_to_save, "mac_address": mac_to_save}, file)
        print(f"Configuración guardada: Tópico '{topic_to_save}', MAC '{mac_to_save}'")
    except IOError as e:
        print(f"Error al guardar la configuración: {e}")

def load_config():
    """Carga la configuración desde un archivo JSON."""
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

def publish_device_info(client):
    """Publica la información del dispositivo en el tópico inicial."""
    global device_mac_address, INITIAL_TOPIC, DEVICE_INSTANCE_ID
    device_info = {
        "name": DEVICE_INSTANCE_ID,
        "type": "smart_outlet",
        "mac_address": device_mac_address,
    }
    client.publish(INITIAL_TOPIC, json.dumps(device_info), retain=True)
    print(f"Publicado mensaje de información del dispositivo en {INITIAL_TOPIC}: {device_info}")

# --- Callbacks MQTT ---
def on_connect(client, userdata, flags, rc, properties=None):
    """Maneja la conexión al broker MQTT."""
    global current_mqtt_topic, is_configured, first_ever_run # Añadir first_ever_run
    if rc == 0:
        print(f"Conexión exitosa al broker MQTT (Código: {rc})")
        client.subscribe(current_mqtt_topic)
        print(f"Suscrito al tópico: {current_mqtt_topic}")

        # Publicar información solo si NO está configurado Y es la primerísima ejecución (no había config file)
        if not is_configured and first_ever_run:
            print("Primera ejecución (sin archivo de config previo) y no configurado: publicando información del dispositivo.")
            publish_device_info(client)
    else:
        print(f"Error en la conexión al broker, código de error: {rc}")

def on_message(client, userdata, msg):
    """Maneja los mensajes MQTT recibidos."""
    global relay_state, current_mqtt_topic, is_configured, device_mac_address

    payload_str = msg.payload.decode()
    print(f"Mensaje recibido en '{msg.topic}': {payload_str}")

    try:
        message_json = json.loads(payload_str)
        msg_mac = message_json.get("mac_address")

        if msg_mac == device_mac_address:
            requested_topic = message_json.get("new_topic")
            command = message_json.get("command")

            if requested_topic:
                if requested_topic == INITIAL_TOPIC: # Solicitud de Desconfiguración
                    if is_configured:
                        print("Solicitud de desconfiguración recibida. Restableciendo.")
                        client.unsubscribe(current_mqtt_topic)
                        print(f"Desuscrito del tópico anterior: {current_mqtt_topic}")
                        
                        current_mqtt_topic = INITIAL_TOPIC
                        client.subscribe(INITIAL_TOPIC)
                        print(f"Suscrito nuevamente al tópico inicial: {INITIAL_TOPIC}")
                        
                        is_configured = False
                        save_config(INITIAL_TOPIC, device_mac_address)
                        # NO publicar info aquí, ya que el dispositivo ya fue conocido por la API
                    else:
                        print("El dispositivo ya está desconfigurado. Ignorando.")
                else: # Solicitud de Configuración
                    print(f"Solicitud de configuración recibida para el tópico: {requested_topic}")
                    if is_configured and current_mqtt_topic != requested_topic:
                        client.unsubscribe(current_mqtt_topic)
                        print(f"Desuscrito del tópico anterior: {current_mqtt_topic}")
                    elif not is_configured and current_mqtt_topic != INITIAL_TOPIC:
                         client.unsubscribe(current_mqtt_topic)
                         print(f"Advertencia: Desconfigurado pero en tópico inesperado {current_mqtt_topic}. Desuscribiendo.")

                    current_mqtt_topic = requested_topic
                    client.subscribe(current_mqtt_topic)
                    print(f"Suscrito al nuevo tópico: {current_mqtt_topic}")
                    is_configured = True
                    save_config(current_mqtt_topic, device_mac_address)
            
            elif command and is_configured and msg.topic == current_mqtt_topic:
                print(f"Comando JSON '{command}' recibido.")
                if command == "turn_on":
                    relay_state = True
                    print("Relay ENCENDIDO")
                elif command == "turn_off":
                    relay_state = False
                    print("Relay APAGADO")
                elif command == "toggle":
                    relay_state = not relay_state
                    print(f"Relay {'ENCENDIDO' if relay_state else 'APAGADO'}")
                else:
                    print(f"Comando JSON desconocido: {command}")
            elif command and not is_configured:
                print(f"Comando JSON '{command}' ignorado: el dispositivo no está configurado.")
            else:
                print("Mensaje JSON para este MAC pero sin 'new_topic' ni 'command' válidos.")

        elif msg_mac:
            print(f"Mensaje JSON ignorado (MAC '{msg_mac}' no coincide).")

    except json.JSONDecodeError:
        print(f"Mensaje no es JSON válido o es texto plano no esperado: '{payload_str}'. Ignorando.")
    except Exception as e:
        print(f"Error procesando mensaje: {e}")

# --- Bloque Principal de Ejecución ---
if __name__ == "__main__":
    # Declarar que vamos a modificar la variable global
    globals()['first_ever_run'] = False # Inicializar como False

    config_data = load_config()
    if config_data and "mac_address" in config_data and config_data["mac_address"]:
        device_mac_address = config_data["mac_address"]
        print(f"Dirección MAC cargada desde config: {device_mac_address}")
        current_mqtt_topic = config_data.get("device_topic", INITIAL_TOPIC)
        is_configured = (current_mqtt_topic != INITIAL_TOPIC)
        # Si el archivo de config existe, no es la "primera ejecución absoluta"
        globals()['first_ever_run'] = False
    else:
        # No se encontró archivo de config o MAC válida, es la primera ejecución absoluta.
        globals()['first_ever_run'] = True
        device_mac_address = f"{random.randint(0, 255):02X}{random.randint(0, 255):02X}{random.randint(0, 255):02X}"
        current_mqtt_topic = INITIAL_TOPIC
        is_configured = False
        print(f"No se encontró MAC válida en config. Generada nueva MAC: {device_mac_address}.")
        save_config(current_mqtt_topic, device_mac_address)

    print(f"Estado inicial del dispositivo: Tópico='{current_mqtt_topic}', Configurado={is_configured}, MAC='{device_mac_address}', PrimeraEjecuciónAbsoluta={first_ever_run}")

    client_id = f"{DEVICE_INSTANCE_ID}-{device_mac_address}"
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=client_id, clean_session=True)
    client.on_connect = on_connect
    client.on_message = on_message

    print(f"Conectando al broker MQTT ({MQTT_BROKER}:{MQTT_PORT}) como '{client_id}'...")
    try:
        client.connect(MQTT_BROKER, MQTT_PORT, keepalive=60)
    except Exception as e:
        print(f"No se pudo conectar al broker MQTT: {e}")
        exit(1)

    try:
        client.loop_start()
        while True:
            time.sleep(10)
    except KeyboardInterrupt:
        print("\nDesconectando del broker MQTT...")
    finally:
        client.loop_stop()
        client.disconnect()
        print("Desconectado.")