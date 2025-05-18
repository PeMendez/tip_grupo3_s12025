import paho.mqtt.client as mqtt
import time
import json

# Configuración del broker MQTT y tópicos
MQTT_BROKER = "broker.hivemq.com"
MQTT_PORT = 1883
INITIAL_TOPIC = "neohub/unconfigured"

# Estado inicial del "dispositivo"
relay_state = False  # Simula el estado del relay
current_topic = INITIAL_TOPIC  # Tópico actual del dispositivo

# Función para manejar mensajes MQTT
def on_message(client, userdata, msg):
    global relay_state, current_topic
    print(f"Mensaje recibido en {msg.topic}: {msg.payload.decode()}")

    # Verificar si el mensaje contiene un nuevo tópico
    try:
        message = json.loads(msg.payload.decode())
        if "new_topic" in message:
            new_topic = message["new_topic"]
            print(f"Nuevo tópico recibido: {new_topic}")

            # Cambiar el tópico de suscripción
            client.unsubscribe(current_topic)
            print(f"Desuscrito del tópico: {current_topic}")
            current_topic = new_topic
            client.subscribe(current_topic)
            print(f"Suscrito al nuevo tópico: {current_topic}")
            return
    except json.JSONDecodeError:
        pass

    # Decodificar comandos de control
    command = msg.payload.decode()
    if command == "turn_on":
        relay_state = True
    elif command == "turn_off":
        relay_state = False
    elif command == "toggle":
        relay_state = not relay_state

    print(f"Estado actual del relay: {'ENCENDIDO' if relay_state else 'APAGADO'}")

# Función para manejar la conexión
def on_connect(client, userdata, flags, rc, properties):
    if rc == 0:
        print("Conexión exitosa al broker MQTT")
        client.subscribe(INITIAL_TOPIC)
        print(f"Suscrito al tópico inicial: {INITIAL_TOPIC}")

        # Publicar mensaje inicial para configurar
        device_info = {
            "name": "SimulatedRelay",
            "type": "smart_outlet",
        }
        client.publish(INITIAL_TOPIC, json.dumps(device_info))
    else:
        print(f"Error en la conexión al broker, código de error: {rc}")

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
