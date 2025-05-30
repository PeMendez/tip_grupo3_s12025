import json
import os
import random
import logging
from time import time
import paho.mqtt.client as mqtt

# --- Clase base de la que heredarán todos los tipos de dispositivo ---
class MqttDevice:
    def __init__(self, device_id, device_type, mqtt_broker, mqtt_port, initial_topic_base="neohub/unconfigured"):
        # Configurar logger dinámicamente basado en la clase hija
        self.logger = logging.getLogger(f"{self.__class__.__name__}.{device_type}.{device_id}")
        
        self.device_id = device_id
        self.device_type = device_type
        self.mqtt_broker = mqtt_broker
        self.mqtt_port = mqtt_port
        
        # Tópico inicial donde los dispositivos no configurados escuchan/anuncian.
        # Todos los tipos de dispositivos usarán este mismo tópico base para simplificar.
        self.initial_topic = initial_topic_base 

        self.config_file = f"config/config_{self.device_type}_{self.device_id}.json"
        
        # Atributos de estado que se inicializarán desde el archivo de config o por defecto
        self.device_mac_address = None
        self.current_mqtt_topic = self.initial_topic # Por defecto, empieza en el tópico inicial
        self.command_topic = None
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
            if self.is_configured:
                self.command_topic = f"{self.current_mqtt_topic}/command"
            self.first_ever_run = False # El archivo de configuración ya existía
            self.logger.info(f"MAC y tópico base cargados desde config: MAC={self.device_mac_address}, Tópico={self.current_mqtt_topic}")
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
        
        self.logger.info(f"Estado inicializado: TópicoBase='{self.current_mqtt_topic}', Configurado={self.is_configured}, PrimeraEjecución={self.first_ever_run}")

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
            # Chequear que el directorio existe
            os.makedirs(os.path.dirname(self.config_file), exist_ok=True)
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
            if self.is_configured:
                # Si esta configurado, suscribirse al topicComando
                if self.command_topic:           
                    client.subscribe(self.command_topic)
                    self.logger.info(f"Suscrito al topico: {self.command_topic}")
                else:
                    self.logger.error("Dispositivo configurado pero command_topic no está definido!")
            else: 
                # No esta configurado. Suscribirse al initial topic.
                client.subscribe(self.initial_topic)
                self.logger.info(f"Suscrito al tópico inicial: {self.initial_topic}")

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
                requested_new_base_topic = message_json.get("new_topic")
                
                # Verificar si es un mensaje de comando buscando la clave "command"
                # Asumimos que un mensaje o es de configuración (tiene "new_topic") o es de comando (tiene "command")
                is_command_message = "command" in message_json

                # --- Manejo de Configuración/Desconfiguración ---
                if requested_new_base_topic:
                    # Estos mensajes pueden llegar a initial_topic (si no está configurado)
                    # o al command_topic actual (si está configurado y se quiere reconfigurar/desconfigurar)
                    
                    if not self.is_configured and msg.topic == self.initial_topic:
                        # Configuración Inicial (o reconfiguración desde un estado "perdido" a initial_topic)
                        if requested_new_base_topic != self.initial_topic: # Configurando a un tópico específico
                            self.logger.info(f"Configuración inicial recibida. Nuevo tópico base: {requested_new_base_topic}")
                            client.unsubscribe(self.initial_topic)
                            self.logger.info(f"Desuscrito de: {self.initial_topic}")

                            self.current_mqtt_topic = requested_new_base_topic
                            self.command_topic = f"{self.current_mqtt_topic}/command"
                            client.subscribe(self.command_topic)
                            self.logger.info(f"Suscrito a nuevo tópico de comandos: {self.command_topic}")
                            
                            self.is_configured = True
                            self._save_config_file()
                        else:
                            self.logger.info("Mensaje de 'new_topic' apunta al initial_topic pero ya estamos ahí y no configurados. Ignorando.")

                    elif self.is_configured and msg.topic == self.command_topic:
                        # Desconfiguración o Reconfiguración de un dispositivo ya configurado
                        if requested_new_base_topic == self.initial_topic: # Solicitud de Desconfiguración
                            self.logger.info("Solicitud de desconfiguración recibida en command_topic.")
                            client.unsubscribe(self.command_topic)
                            self.logger.info(f"Desuscrito de: {self.command_topic}")
                            
                            self.current_mqtt_topic = self.initial_topic
                            self.command_topic = None # Ya no tiene un command_topic específico
                            client.subscribe(self.initial_topic)
                            self.logger.info(f"Suscrito nuevamente al tópico inicial: {self.initial_topic}")
                            
                            self.is_configured = False
                            self._save_config_file()
                        else: # Solicitud de Reconfiguración a un nuevo tópico base específico
                            self.logger.info(f"Solicitud de reconfiguración recibida en command_topic. Nuevo tópico base: {requested_new_base_topic}")
                            client.unsubscribe(self.command_topic)
                            self.logger.info(f"Desuscrito del tópico de comandos anterior: {self.command_topic}")

                            self.current_mqtt_topic = requested_new_base_topic
                            self.command_topic = f"{self.current_mqtt_topic}/command"
                            client.subscribe(self.command_topic)
                            self.logger.info(f"Suscrito al nuevo tópico de comandos: {self.command_topic}")
                            # self.is_configured sigue siendo True
                            self._save_config_file()
                    else:
                        self.logger.warning(f"Mensaje de configuración/new_topic recibido en un tópico inesperado '{msg.topic}'. Ignorando.")

                # --- Manejo de Comandos ---
                elif is_command_message:
                    if self.is_configured and msg.topic == self.command_topic:
                        command_value = message_json.get("command")
                        self.logger.info(f"Comando JSON '{command_value}' recibido en command_topic.")
                        
                        if command_value == "ack":
                            self._send_ack_response(message_json)
                        else:
                            self._handle_device_command(message_json)
                    elif not self.is_configured:
                        self.logger.warning(f"Comando JSON '{message_json.get('command')}' ignorado: dispositivo no configurado.")
                    else: 
                        self.logger.warning(f"Comando JSON '{message_json.get('command')}' ignorado: recibido en tópico incorrecto '{msg.topic}' (esperado en '{self.command_topic}').")
                else:
                    self.logger.warning(f"Mensaje JSON para este MAC pero sin 'new_topic' ni 'command'. Contenido: {message_json}")

            elif msg_mac: 
                self.logger.debug(f"Mensaje JSON ignorado (MAC '{msg_mac}' no coincide con {self.device_mac_address}).")

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

    def _send_ack_response(self, original_command_json: dict):
        """Envía una respuesta ACK al tópico base del dispositivo."""
        if not self.current_mqtt_topic or self.current_mqtt_topic == self.initial_topic:
            self.logger.warning("No se puede enviar ACK, el dispositivo no tiene un tópico base configurado o está en initial_topic.")
            return

        ack_payload = {
            "mac_address": self.device_mac_address,
            "device_id": self.device_id,
            "response_to_command": original_command_json.get("command"),
            "status": "ack_received",
            "timestamp": time.time()
        }
        try:
            self.client.publish(self.current_mqtt_topic, json.dumps(ack_payload))
            self.logger.info(f"Respuesta ACK enviada a {self.current_mqtt_topic}: {ack_payload}")
        except Exception as e:
            self.logger.error(f"Error al enviar respuesta ACK: {e}")
        

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