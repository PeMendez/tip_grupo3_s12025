from devices import MqttDevice
import time, json

# --- Clase Específica del Dispositivo Dimmer --- WARNING: AUN NO SOPORTADO y FALTA TERMINAR ESTO
class DimmerDevice(MqttDevice):
    def __init__(self, device_id, mqtt_broker, mqtt_port, initial_topic_base="neohub/unconfigured"):
        # 1. Inicializar el estado específico ANTES de llamar a super().__init__
        #    para que _initialize_specific_state pueda usarlo si no hay config,
        #    o para que _get_config_data_to_save lo guarde en la primera ejecución.
        self.brightness = 0 # Estado por defecto del brillo de la lámpara
        
        # 2. Llamar al constructor de la clase base. Esto llamará a _initialize_state_from_config,
        #    que a su vez llamará a nuestro _initialize_specific_state y _save_config_file (si es necesario).
        super().__init__(device_id, "dimmer", mqtt_broker, mqtt_port, initial_topic_base)
        
        # 3. Publicar estado inicial después de que todo esté configurado
        if self.is_configured:
            self._publish_current_state()


    def _initialize_specific_state(self, config_data: dict):
        """Carga el brillo desde la configuración, si existe."""
        if "brightness" in config_data:
            self.brightness = config_data.get("brightness", 0)
            self.logger.info(f"Brillo cargado desde config: {self.brightness}")
        else:
            self.logger.info(f"No se encontró 'brightness' en config. Usando estado por defecto: {self.brightness}")

    def _get_config_data_to_save(self) -> dict:
        """Añade el brillo a los datos de configuración base para guardar."""
        data = super()._get_config_data_to_save()
        data["brightness"] = self.brightness
        return data

    def _get_specific_device_attributes_for_info(self) -> dict:
        """Añade el brillo actual y comandos disponibles a la información del dispositivo."""
        return {
            "current_brightness": self.brightness,
            "commands_available": ["set_brightness"]
        }

    def _handle_device_command(self, command_json: dict):
        """Maneja los comandos específicos para el Dimmer."""
        command = command_json.get("command")
        state_changed = False
        if command == "set_brightness":
            new_brightness = command_json.get("parameters")
            if (new_brightness != self.brightness):
                self.brightness = new_brightness
                state_changed = True
                self.logger.info(f"Brillo seteado a {new_brightness}")
        else:
            self.logger.warning(f"Comando Dimmer desconocido: {command}")

        if state_changed:
            self._save_config_file() # Guardar el nuevo brillo
            self._publish_current_state() # Publicar el nuevo estado

    def _publish_current_state(self):
        """Publica el brillo actual al tópico de estado del dispositivo."""
        if self.is_configured:
            state_topic = f"{self.current_mqtt_topic}" 
            payload = {
                "mac_address": self.device_mac_address,
                "device_id": self.device_id,
                "brightness": self.brightness,
                "timestamp": time.time() 
            }
            try:
                self.client.publish(state_topic, json.dumps(payload), retain=True) # Retener el último estado
                self.logger.info(f"brillo publicado en {state_topic}: {payload}")
            except Exception as e:
                self.logger.error(f"Error al publicar brillo: {e}")
        else:
            self.logger.debug("Dispositivo no configurado, no se publica brillo.")