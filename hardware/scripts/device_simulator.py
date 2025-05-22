import paho.mqtt.client as mqtt
import time
import json
import os
import random
import logging
import argparse

# Configuración básica de logging para ver qué está pasando
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')

class MqttDevice:
    def __init__(self, device_id, device_type, mqtt_broker, mqtt_port, initial_topic_base="neohub/unconfigured"):
        self.logger = logging.getLogger(f"MqttDevice.{device_type}.{device_id}") # Logger específico por instancia

        self.device_id = device_id
        self.device_type = device_type
        self.mqtt_broker = mqtt_broker
        self.mqtt_port = mqtt_port
        
        # Tópico inicial donde los dispositivos no configurados escuchan/anuncian.
        # Todos los tipos de dispositivos usarán este mismo tópico base para simplificar.
        self.initial_topic = initial_topic_base 

        self.config_file = f"config_{self.device_type}_{self.device_id}.json"
        
        # Atributos de estado que se inicializarán desde el archivo de config o por defecto
        self.device_mac_address = None
        self.current_mqtt_topic = self.initial_topic # Por defecto, empieza en el tópico inicial
        self.is_configured = False
        self.first_ever_run = False # Se determinará si el archivo de config no existía

        self._initialize_state_from_config() # Carga config, MAC, y estado específico del hijo

        # Configuración del Cliente MQTT
        # El Client ID debe ser único. Combinar tipo, ID y MAC es una buena práctica.
        self.client_id = f"{self.device_type}-{self.device_id}-{self.device_mac_address}"
        self.client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=self.client_id, clean_session=True)
        self.client.on_connect = self._on_connect
        self.client.on_message = self._on_message
        # self.client.user_data_set(self) # Opcional, ya que los callbacks son métodos y tienen 'self'

        self.logger.info(f"Dispositivo MQTT '{self.device_id}' ({self.device_type}) inicializado. MAC: {self.device_mac_address}")

    def _initialize_state_from_config(self):
        """
        Carga la configuración desde el archivo, inicializa la MAC, el tópico actual,
        el estado de configuración, y llama a la inicialización de estado específico de la subclase.
        Si no hay archivo de config, genera MAC y prepara para guardar la config inicial.
        """
        config_data = self._load_config_file() # Carga el contenido del archivo JSON

        if config_data and "mac_address" in config_data and config_data["mac_address"]:
            self.device_mac_address = config_data["mac_address"]
            self.current_mqtt_topic = config_data.get("device_topic", self.initial_topic)
            self.is_configured = (self.current_mqtt_topic != self.initial_topic)
            self.first_ever_run = False # El archivo de configuración ya existía
            self.logger.info(f"MAC y tópico cargados desde config: MAC={self.device_mac_address}, Tópico={self.current_mqtt_topic}")
        else:
            self.first_ever_run = True # No había archivo de config o MAC válida
            self.device_mac_address = f"{random.randint(0, 255):02X}{random.randint(0, 255):02X}{random.randint(0, 255):02X}"
            self.current_mqtt_topic = self.initial_topic # Comienza desconfigurado
            self.is_configured = False
            self.logger.info(f"No se encontró MAC en config. Nueva MAC generada: {self.device_mac_address}")
            # La configuración se guardará después de inicializar el estado específico si es first_ever_run

        # Permitir a la subclase inicializar su propio estado desde config_data (si existe)
        self._initialize_specific_state(config_data if config_data else {})

        if self.first_ever_run:
            # Si es la primera ejecución (no había config), ahora guardamos la config inicial completa
            # (incluyendo el estado específico que la subclase haya podido establecer por defecto).
            self._save_config_file()
        
        self.logger.info(f"Estado inicializado: Tópico='{self.current_mqtt_topic}', Configurado={self.is_configured}, PrimeraEjecución={self.first_ever_run}")

    def _initialize_specific_state(self, config_data: dict):
        """
        Método para ser sobrescrito por las subclases.
        Permite a las subclases cargar su estado específico desde el diccionario de configuración.
        """
        # Ejemplo en subclase: self.relay_state = config_data.get("relay_state", False)
        pass

    def _get_config_data_to_save(self) -> dict:
        """
        Método para ser sobrescrito por las subclases.
        Debe devolver un diccionario con todos los datos que se deben guardar en el archivo de config.
        La subclase DEBE llamar a super()._get_config_data_to_save() y añadir sus datos.
        """
        return {
            "device_topic": self.current_mqtt_topic,
            "mac_address": self.device_mac_address,
            "device_type": self.device_type,
            "device_id": self.device_id,
            # La subclase añadirá aquí sus propios campos, ej: "relay_state": self.relay_state
        }

    def _save_config_file(self):
        """Guarda la configuración actual (incluyendo datos específicos de la subclase) en el archivo."""
        data_to_save = self._get_config_data_to_save()
        try:
            with open(self.config_file, "w") as file:
                json.dump(data_to_save, file, indent=4)
            self.logger.info(f"Configuración guardada en {self.config_file}: {data_to_save}")
        except IOError as e:
            self.logger.error(f"Error al guardar la configuración en {self.config_file}: {e}")

    def _load_config_file(self) -> dict | None:
        """Carga la configuración desde el archivo JSON."""
        if os.path.exists(self.config_file):
            try:
                with open(self.config_file, "r") as file:
                    config = json.load(file)
                self.logger.info(f"Configuración cargada desde {self.config_file}.")
                return config
            except (IOError, json.JSONDecodeError) as e:
                self.logger.error(f"Error al cargar la configuración de {self.config_file}: {e}")
        else:
            self.logger.info(f"No se encontró archivo de configuración: {self.config_file}.")
        return None

    def _get_specific_device_attributes_for_info(self) -> dict:
        """
        Método para ser sobrescrito por las subclases.
        Permite añadir atributos específicos del tipo de dispositivo al mensaje de información inicial.
        """
        return {}

    def _publish_device_info(self):
        """Publica la información inicial del dispositivo en el initial_topic."""
        device_info = {
            "name": f"{self.device_id}", # Nombre genérico
            "type": self.device_type,
            "mac_address": self.device_mac_address,
            "device_id": self.device_id,
        }
        # Añadir atributos específicos del tipo de dispositivo
        device_info.update(self._get_specific_device_attributes_for_info())
        
        try:
            self.client.publish(self.initial_topic, json.dumps(device_info), retain=True)
            self.logger.info(f"Publicado mensaje de información del dispositivo en {self.initial_topic}: {device_info}")
        except Exception as e:
            self.logger.error(f"Error al publicar información del dispositivo: {e}")

    def _on_connect(self, client, userdata, flags, rc, properties=None):
        if rc == 0:
            self.logger.info(f"Conexión exitosa al broker MQTT (Código: {rc})")
            client.subscribe(self.current_mqtt_topic)
            self.logger.info(f"Suscrito al tópico: {self.current_mqtt_topic}")

            if not self.is_configured and self.first_ever_run:
                self.logger.info(f"Primera ejecución (sin config previa) y no configurado: publicando info.")
                self._publish_device_info()
        else:
            self.logger.error(f"Error en la conexión al broker, código de error: {rc}")

    def _on_message(self, client, userdata, msg):
        payload_str = ""
        try:
            payload_str = msg.payload.decode()
            self.logger.debug(f"Mensaje crudo recibido en '{msg.topic}': {payload_str}")
            message_json = json.loads(payload_str)
            
            msg_mac = message_json.get("mac_address")

            if msg_mac == self.device_mac_address:
                self.logger.info(f"Mensaje para este dispositivo (MAC: {msg_mac}) en '{msg.topic}': {message_json}")
                requested_topic = message_json.get("new_topic")
                
                # Verificar si es un mensaje de comando buscando la clave "command"
                # Asumimos que un mensaje o es de configuración (tiene "new_topic") o es de comando (tiene "command")
                is_command_message = "command" in message_json

                if requested_topic: # Mensaje de Configuración/Desconfiguración
                    if requested_topic == self.initial_topic: # Solicitud de Desconfiguración
                        if self.is_configured:
                            self.logger.info("Solicitud de desconfiguración recibida. Restableciendo.")
                            client.unsubscribe(self.current_mqtt_topic)
                            self.logger.info(f"Desuscrito de: {self.current_mqtt_topic}")
                            
                            self.current_mqtt_topic = self.initial_topic
                            client.subscribe(self.initial_topic)
                            self.logger.info(f"Suscrito nuevamente al tópico inicial: {self.initial_topic}")
                            
                            self.is_configured = False
                            self._save_config_file() # Guardar el nuevo estado desconfigurado
                        else:
                            self.logger.info("El dispositivo ya está desconfigurado. Ignorando.")
                    else: # Solicitud de Configuración para un nuevo tópico específico
                        self.logger.info(f"Solicitud de configuración recibida para el tópico: {requested_topic}")
                        if self.is_configured and self.current_mqtt_topic != requested_topic:
                            client.unsubscribe(self.current_mqtt_topic)
                            self.logger.info(f"Desuscrito del tópico anterior: {self.current_mqtt_topic}")
                        elif not self.is_configured and self.current_mqtt_topic == self.initial_topic and self.initial_topic != requested_topic :
                             client.unsubscribe(self.initial_topic) # Estaba en initial, se configura a uno nuevo
                             self.logger.info(f"Desuscrito del tópico inicial: {self.initial_topic}")
                        
                        self.current_mqtt_topic = requested_topic
                        client.subscribe(self.current_mqtt_topic)
                        self.logger.info(f"Suscrito al nuevo tópico: {self.current_mqtt_topic}")
                        self.is_configured = True
                        self._save_config_file()
                
                elif is_command_message: # Mensaje de Comando
                    if self.is_configured and msg.topic == self.current_mqtt_topic:
                        self.logger.info(f"Procesando comando JSON: {message_json.get('command')}")
                        self._handle_device_command(message_json) # Pasar el JSON completo del comando
                    elif not self.is_configured:
                        self.logger.warning(f"Comando JSON '{message_json.get('command')}' ignorado: dispositivo no configurado.")
                    else: # Configurado pero mensaje de comando en tópico incorrecto
                        self.logger.warning(f"Comando JSON '{message_json.get('command')}' ignorado: recibido en tópico incorrecto '{msg.topic}' (esperado en '{self.current_mqtt_topic}').")
                else:
                    self.logger.warning(f"Mensaje JSON para este MAC pero sin 'new_topic' ni 'command'. Contenido: {message_json}")

            elif msg_mac: # JSON con MAC, pero no para este dispositivo
                self.logger.debug(f"Mensaje JSON ignorado (MAC '{msg_mac}' no coincide con {self.device_mac_address}).")
            # else: Mensaje JSON sin MAC. Podría ser un broadcast. Ignorar por ahora.

        except json.JSONDecodeError:
            self.logger.warning(f"Mensaje recibido en '{msg.topic}' no es JSON válido: '{payload_str}'. Ignorando.")
        except Exception as e:
            self.logger.error(f"Error procesando mensaje en '{msg.topic}': {e}. Payload: '{payload_str}'", exc_info=True)

    def _handle_device_command(self, command_json: dict):
        """
        Método abstracto para ser implementado por subclases.
        Maneja los comandos específicos del dispositivo.
        
        Args:
            command_json (dict): El mensaje JSON decodificado que contiene el comando.
                                 Se espera que tenga una clave "command" y "mac_address".
        """
        self.logger.warning(f"_handle_device_command no implementado para {self.device_type}. Comando ignorado: {command_json.get('command')}")

    def start(self):
        """Conecta al broker MQTT e inicia el bucle del cliente."""
        try:
            self.logger.info(f"Conectando a {self.mqtt_broker}:{self.mqtt_port} como '{self.client_id}'...")
            self.client.connect(self.mqtt_broker, self.mqtt_port, keepalive=60)
            self.client.loop_start() # Inicia un hilo para el bucle MQTT
            self.logger.info("Bucle MQTT iniciado en hilo separado.")
        except Exception as e:
            self.logger.critical(f"No se pudo conectar o iniciar el bucle MQTT: {e}", exc_info=True)
            # Considerar una estrategia de reintento o terminar si es crítico

    def stop(self):
        """Detiene el bucle del cliente MQTT y desconecta."""
        try:
            self.logger.info("Deteniendo el cliente MQTT...")
            self.client.loop_stop()
            self.client.disconnect()
            self.logger.info("Cliente MQTT desconectado limpiamente.")
        except Exception as e:
            self.logger.error(f"Error al detener el cliente MQTT: {e}", exc_info=True)

# --- Clase Específica del Dispositivo SmartOutlet ---
class SmartOutletDevice(MqttDevice):
    def __init__(self, device_id, mqtt_broker, mqtt_port, initial_topic_base="neohub/unconfigured"):
        # 1. Inicializar el estado específico ANTES de llamar a super().__init__
        #    para que _initialize_specific_state pueda usarlo si no hay config,
        #    o para que _get_config_data_to_save lo guarde en la primera ejecución.
        self.relay_state = False # Estado por defecto del enchufe
        
        # 2. Llamar al constructor de la clase base. Esto llamará a _initialize_state_from_config,
        #    que a su vez llamará a nuestro _initialize_specific_state y _save_config_file (si es necesario).
        super().__init__(device_id, "smart_outlet", mqtt_broker, mqtt_port, initial_topic_base)
        
        # 3. Publicar estado inicial después de que todo esté configurado
        if self.is_configured:
            self._publish_current_state()


    def _initialize_specific_state(self, config_data: dict):
        """Carga el estado del relé desde la configuración, si existe."""
        if "relay_state" in config_data:
            self.relay_state = config_data.get("relay_state", False)
            self.logger.info(f"Estado del relé cargado desde config: {self.relay_state}")
        else:
            self.logger.info(f"No se encontró 'relay_state' en config. Usando estado por defecto: {self.relay_state}")

    def _get_config_data_to_save(self) -> dict:
        """Añade el estado del relé a los datos de configuración base para guardar."""
        data = super()._get_config_data_to_save()
        data["relay_state"] = self.relay_state
        return data

    def _get_specific_device_attributes_for_info(self) -> dict:
        """Añade el estado actual del relé y comandos disponibles a la información del dispositivo."""
        return {
            "current_relay_state": self.relay_state,
            "commands_available": ["turn_on", "turn_off", "toggle"]
        }

    def _handle_device_command(self, command_json: dict):
        """Maneja los comandos específicos para el Smart Outlet."""
        command = command_json.get("command")
        state_changed = False

        if command == "turn_on":
            if not self.relay_state:
                self.relay_state = True
                state_changed = True
                self.logger.info("Relay ENCENDIDO")
        elif command == "turn_off":
            if self.relay_state:
                self.relay_state = False
                state_changed = True
                self.logger.info("Relay APAGADO")
        elif command == "toggle":
            self.relay_state = not self.relay_state
            state_changed = True
            self.logger.info(f"Relay CAMBIADO a {'ENCENDIDO' if self.relay_state else 'APAGADO'}")
        else:
            self.logger.warning(f"Comando SmartOutlet desconocido: {command}")

        if state_changed:
            self._save_config_file() # Guardar el nuevo estado del relé
            self._publish_current_state() # Publicar el nuevo estado

    def _publish_current_state(self):
        """Publica el estado actual del relé al tópico de estado del dispositivo."""
        if self.is_configured:
            # Definir un sub-tópico para el estado, ej: dispositivo/cocina/luz/estado
            state_topic = f"{self.current_mqtt_topic}/state" 
            payload = {
                "mac_address": self.device_mac_address,
                "device_id": self.device_id,
                "relay_state": self.relay_state,
                "timestamp": time.time() 
            }
            try:
                self.client.publish(state_topic, json.dumps(payload), retain=True) # Retener el último estado
                self.logger.info(f"Estado del relé publicado en {state_topic}: {payload}")
            except Exception as e:
                self.logger.error(f"Error al publicar estado del relé: {e}")
        else:
            self.logger.debug("Dispositivo no configurado, no se publica estado del relé.")

class TemperatureSensorDevice(MqttDevice):
    def __init__(self, device_id, mqtt_broker, mqtt_port, initial_topic_base="neohub/unconfigured"):
        # 1. Inicializar el estado específico ANTES de llamar a super().__init__
        #    para que _initialize_specific_state pueda usarlo si no hay config,
        #    o para que _get_config_data_to_save lo guarde en la primera ejecución.
        self.temperature = 18.0 # Temperatura por defecto
        
        # 2. Llamar al constructor de la clase base. Esto llamará a _initialize_state_from_config,
        #    que a su vez llamará a nuestro _initialize_specific_state y _save_config_file (si es necesario).
        super().__init__(device_id, "temperature_sensor", mqtt_broker, mqtt_port, initial_topic_base)
        
        # 3. Publicar estado inicial después de que todo esté configurado
        if self.is_configured:
            self._publish_current_state()

    def _initialize_specific_state(self, config_data: dict):
        """Carga la temperatura desde la configuración, si existe."""
        if "temperature" in config_data:
            # Usar .get con un default es un poco más seguro aunque ya hayas puesto un default en __init__
            self.temperature = float(config_data.get("temperature", self.temperature)) # Convertir a float por si se guardó como int o string
            self.logger.info(f"Temperatura cargada desde config: {self.temperature}°C")
        else:
            self.logger.info(f"No se encontró 'temperature' en config. Usando valor por defecto: {self.temperature}°C")
        
    def _get_config_data_to_save(self) -> dict:
        """Añade la temperatura a los datos de configuración base para guardar."""
        data = super()._get_config_data_to_save()
        data["temperature"] = self.temperature
        return data

    def _get_specific_device_attributes_for_info(self) -> dict:
        """Añade la temperatura a la información del dispositivo."""
        return {
            "current_temperature_celsius": self.temperature, # No soporta comandos.
            "unit":"C",
        }
    
    def _publish_current_state(self):
        """Publica la temperatura actual al tópico de estado del dispositivo."""
        if self.is_configured:
            # Definir un sub-tópico para el estado, ej: dispositivo/cocina/luz/estado
            state_topic = f"{self.current_mqtt_topic}" # /state" 
            payload = {
                "mac_address": self.device_mac_address,
                "device_id": self.device_id,
                "temperature": self.temperature,
                "unit": "C",
                "timestamp": time.time() 
            }
            try:
                self.client.publish(state_topic, json.dumps(payload), retain=True) # Retener el último estado
                self.logger.info(f"Temperatura publicada en {state_topic}: {payload}")
            except Exception as e:
                self.logger.error(f"Error al publicar temperatura: {e}")
        else:
            self.logger.debug("Dispositivo no configurado, no se publica temperatura.")

     # Método para actualizar la temperatura (simulando una lectura física)
    def update_temperature(self, new_temperature: float):
        """Actualiza la temperatura del sensor y publica el nuevo estado."""
        if self.temperature != new_temperature:
            self.temperature = new_temperature
            self.logger.info(f"Temperatura del sensor actualizada a: {self.temperature}°C")
            self._publish_current_state() # Publicar el nuevo estado
            self._save_config_file() # Guardar el último valor "leído"
        else:
            self.logger.debug(f"Temperatura sin cambios ({self.temperature}°C), no se publica ni guarda.")


# --- Bloque Principal para Ejecutar el Simulador ---
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Simulador de Dispositivo MQTT IoT.")
    parser.add_argument("--id", required=True, help="Identificador único para esta instancia del dispositivo (ej: outlet_sala).")
    parser.add_argument("--type", default="smart_outlet", help="Tipo de dispositivo a simular (ej: smart_outlet).")
    parser.add_argument("--broker", default="broker.hivemq.com", help="Dirección del broker MQTT.")
    parser.add_argument("--port", type=int, default=1883, help="Puerto del broker MQTT.")
    parser.add_argument("--initial-topic", default="neohub/unconfigured", help="Tópico base para dispositivos no configurados.")
    
    cli_args = parser.parse_args()

    device_instance = None

    if cli_args.type.lower() == "smart_outlet":
        device_instance = SmartOutletDevice(
            device_id=cli_args.id,
            mqtt_broker=cli_args.broker,
            mqtt_port=cli_args.port,
            initial_topic_base=cli_args.initial_topic
        )
    # Aquí podrías añadir más 'elif' para otros tipos de dispositivos
    # elif cli_args.type.lower() == "smartlight":
    #     device_instance = SmartLightDevice(...) 
    elif(cli_args.type.lower() == "temperature_sensor"):
        device_instance = TemperatureSensorDevice(
            device_id=cli_args.id,
            mqtt_broker=cli_args.broker,
            mqtt_port=cli_args.port,
            initial_topic_base=cli_args.initial_topic
        )
    else:
        print(f"Tipo de dispositivo desconocido: {cli_args.type}")
        exit(1)

    if device_instance:
        device_instance.start()

        try:
            while True:
                if isinstance(device_instance, TemperatureSensorDevice): # Solo para el sensor de temperatura
                    try:
                        temp_input = input(f"[{device_instance.device_id}] Ingrese nueva temperatura (actual: {device_instance.temperature:.1f}°C) o 's' para omitir: ")
                        if temp_input.lower() == 's':
                            time.sleep(5) # Esperar un poco si se omite
                            continue
                        new_temp = float(temp_input)
                        device_instance.update_temperature(new_temp) # Usar el nuevo método
                    except ValueError:
                        print("Entrada inválida. Por favor ingrese un número o 's'.")
                    except EOFError: # Manejar si el input se cierra (ej. en algunos entornos de ejecución)
                        print("Entrada finalizada, saliendo del bucle de input de temperatura.")
                        break 
                    time.sleep(1) # Pequeña pausa después del input
                else:
                    time.sleep(5) # Para otros dispositivos, solo esperar
        except KeyboardInterrupt:
            print("\nSimulación interrumpida por el usuario.")
        finally:
            if device_instance:
                device_instance.stop()