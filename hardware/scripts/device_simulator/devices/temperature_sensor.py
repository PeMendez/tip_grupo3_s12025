from devices import MqttDevice
import time, json

# --- Clase Específica del Dispositivo TemperatureSensor ---
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