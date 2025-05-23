from devices import MqttDevice
import time, json, logging

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