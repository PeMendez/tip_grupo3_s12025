from devices import MqttDevice
import time, json 

# --- Clase Específica del Dispositivo OpeningSensor ---
class OpeningSensorDevice(MqttDevice):
    def __init__(self, device_id, mqtt_broker, mqtt_port, initial_topic_base="neohub/unconfigured"):
        # 1. Inicializar el estado específico ANTES de llamar a super().__init__
        #    para que _initialize_specific_state pueda usarlo si no hay config,
        #    o para que _get_config_data_to_save lo guarde en la primera ejecución.
        self.isOpen = False
        
        # 2. Llamar al constructor de la clase base. Esto llamará a _initialize_state_from_config,
        #    que a su vez llamará a nuestro _initialize_specific_state y _save_config_file (si es necesario).
        super().__init__(device_id, "opening_sensor", mqtt_broker, mqtt_port, initial_topic_base)
        
        # 3. Publicar estado inicial después de que todo esté configurado
        if self.is_configured:
            self._publish_current_state()

    def _initialize_specific_state(self, config_data: dict):
        """Carga el estado desde la configuración, si existe."""
        if "status" in config_data:
            # Usar .get con un default es un poco más seguro aunque ya hayas puesto un default en __init__
            self.isOpen = config_data.get("status", self.isOpen)
            self.logger.info(f"Estado cargado desde config: {self.isOpen}")
        else:
            self.logger.info(f"No se encontró 'status' en config. Usando valor por defecto: {self.isOpen}")
        
    def _get_config_data_to_save(self) -> dict:
        """Añade el estado a los datos de configuración base para guardar."""
        data = super()._get_config_data_to_save()
        data["status"] = self.isOpen
        return data

    def _get_specific_device_attributes_for_info(self) -> dict:
        """Añade el estado a la información del dispositivo."""
        return {
            "current_status": self.isOpen, # No soporta comandos.
        }
    
    def _publish_current_state(self):
        """Publica el estado actual al tópico de estado del dispositivo."""
        if self.is_configured:
            # Definir un sub-tópico para el estado, ej: dispositivo/cocina/luz/estado
            state_topic = f"{self.current_mqtt_topic}" # /state" 
            payload = {
                "mac_address": self.device_mac_address,
                "device_id": self.device_id,
                "status": self.isOpen,
                "timestamp": time.time() 
            }
            try:
                self.client.publish(state_topic, json.dumps(payload), retain=True) # Retener el último estado
                self.logger.info(f"Estado publicado en {state_topic}: {payload}")
            except Exception as e:
                self.logger.error(f"Error al publicar estado: {e}")
        else:
            self.logger.debug("Dispositivo no configurado, no se publica estado.")

     # Método para actualizar el estado de la abertura (simulando apertura o cierre fisicos)
    def update_status(self, new_status: float):
        """Actualiza el estado del sensor y publica el nuevo estado."""
        if(new_status): 
            status_print = "abierto" 
        else: 
            status_print = "cerrado"

        if self.isOpen != new_status:
            self.isOpen = new_status
            self.logger.info(f"Estado actualizado del sensor: {status_print}")
            self._publish_current_state() # Publicar el nuevo estado
            self._save_config_file() # Guardar el último valor "leído"
        else:
            self.logger.debug(f"Estado sin cambios ({status_print}), no se publica ni guarda.")