import argparse, time
from devices import SmartOutletDevice, TemperatureSensorDevice, OpeningSensorDevice, DimmerDevice

# --- Bloque Principal para Ejecutar el Simulador ---
def main():
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
    elif(cli_args.type.lower() == "temperature_sensor"):
        device_instance = TemperatureSensorDevice(
            device_id=cli_args.id,
            mqtt_broker=cli_args.broker,
            mqtt_port=cli_args.port,
            initial_topic_base=cli_args.initial_topic
        )
    elif(cli_args.type.lower() == "opening_sensor"):
        device_instance = OpeningSensorDevice(
            device_id=cli_args.id,
            mqtt_broker=cli_args.broker,
            mqtt_port=cli_args.port,
            initial_topic_base=cli_args.initial_topic
        )
    elif(cli_args.type.lower() == "dimmer"):
        device_instance = DimmerDevice(
            device_id=cli_args.id,
            mqtt_broker=cli_args.broker,
            mqtt_port=cli_args.port,
            initial_topic_base=cli_args.initial_topic
        )
    # elif (para añadir otros dispositivos)
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
                elif isinstance(device_instance, OpeningSensorDevice): #Solo para sensor de puerta ventana
                    try:
                        opening_input = input(f"[{device_instance.device_id}] Ingrese estado (actual: {device_instance.isOpen} o 's' para omitir: ")
                        if opening_input.lower() == 's':
                            time.sleep(5) # Esperar un poco si se omite
                            continue
                        elif opening_input.lower() == 'o':
                            new_status = True
                        elif opening_input.lower() == 'c':
                            new_status = False
                        else: 
                            new_status = device_instance.isOpen
                        device_instance.update_status(new_status)
                    except ValueError:
                        print("Entrada inválida. Por favor ingrese 'o' (open) o 'c' (close).")
                    except EOFError: # Manejar si el input se cierra (ej. en algunos entornos de ejecución)
                        print("Entrada finalizada, saliendo del bucle de input de apertura de puerta.")
                        break 
                    time.sleep(1) # Pequeña pausa después del input
                else:
                    time.sleep(5) # Para otros dispositivos, solo esperar
        except KeyboardInterrupt:
            print("\nSimulación interrumpida por el usuario.")
        finally:
            if device_instance:
                device_instance.stop()

if __name__ == "__main__":
    main()