from devices import MqttDevice
import time, json

# --- Clase Específica del Dispositivo SmartOutlet ---
class SmartOutletDevice(MqttDevice):
    def __init__(self, device_id, mqtt_broker, mqtt_port, initial_topic_base="neohub/unconfigured"):
        # 1. Inicializar el estado específico ANTES de llamar a super().__init__
        #    para que _initialize_specific_state pueda usarlo si no hay config,
        #    o para que _get_config_data_to_save lo guarde en la primera ejecución.
        self.status = False # Estado por defecto del enchufe
        
        # 2. Llamar al constructor de la clase base. Esto llamará a _initialize_state_from_config,
        #    que a su vez llamará a nuestro _initialize_specific_state y _save_config_file (si es necesario).
        super().__init__(device_id, "smart_outlet", mqtt_broker, mqtt_port, initial_topic_base)
        
        # 3. Publicar estado inicial después de que todo esté configurado
        if self.is_configured:
            self._publish_current_state()


    def _initialize_specific_state(self, config_data: dict):
        """Carga el estado del relé desde la configuración, si existe."""
        if "status" in config_data:
            self.status = config_data.get("status", False)
            self.logger.info(f"Estado del relé cargado desde config: {self.status}")
        else:
            self.logger.info(f"No se encontró 'status' en config. Usando estado por defecto: {self.status}")

    def _get_config_data_to_save(self) -> dict:
        """Añade el estado del relé a los datos de configuración base para guardar."""
        data = super()._get_config_data_to_save()
        data["status"] = self.status
        return data

    def _get_specific_device_attributes_for_info(self) -> dict:
        """Añade el estado actual del relé y comandos disponibles a la información del dispositivo."""
        return {
            "current_status": self.status,
            "commands_available": ["turn_on", "turn_off", "toggle"]
        }

    def _handle_device_command(self, command_json: dict):
        """Maneja los comandos específicos para el Smart Outlet."""
        command = command_json.get("command")
        print(command)
        state_changed = False
        if command == "turn_on":
            if not self.status:
                self.status = True
                state_changed = True
                self.logger.info("Relay ENCENDIDO")
        elif command == "turn_off":
            if self.status:
                self.status = False
                state_changed = True
                self.logger.info("Relay APAGADO")
        elif command == "toggle":
            self.status = not self.status
            state_changed = True
            self.logger.info(f"Relay CAMBIADO a {'ENCENDIDO' if self.status else 'APAGADO'}")
        else:
            self.logger.warning(f"Comando SmartOutlet desconocido: {command}")

        if state_changed:
            self._save_config_file() # Guardar el nuevo estado del relé
            self._publish_current_state() # Publicar el nuevo estado

    def _publish_current_state(self):
        """Publica el estado actual del relé al tópico de estado del dispositivo."""
        if self.is_configured:
            state_topic = f"{self.current_mqtt_topic}" 
            payload = {
                "mac_address": self.device_mac_address,
                "device_id": self.device_id,
                "status": self.status,
                "timestamp": time.time() 
            }
            try:
                self.client.publish(state_topic, json.dumps(payload), retain=True) # Retener el último estado
                self.logger.info(f"Estado del relé publicado en {state_topic}: {payload}")
            except Exception as e:
                self.logger.error(f"Error al publicar estado del relé: {e}")
        else:
            self.logger.debug("Dispositivo no configurado, no se publica estado del relé.")