package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.ActionType
import ar.unq.ttip.neohub.model.Attribute
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import jakarta.persistence.Entity

@Entity
class SmartOutlet(
    name: String,
    room: Room?= null
): Device(name=name, type = DeviceType.SMART_OUTLET, room = room){
    var isOn: Boolean = false

    fun turnOn(){
        setOutletStatus("true")
        println("$name turned on")
    }

    fun turnOff(){
        setOutletStatus("false")
        println("$name turned off")
    }

    fun toggle(){
        if (isOn) {
            setOutletStatus("false")
        } else{
            setOutletStatus("true")
        }
        println("$name toggled to ${if (isOn) "ON" else "OFF"}")
    }

    private fun setOutletStatus(valor: String) {
        // El valor JSON booleano se convierte a String "true" o "false" por valueNode.asText()
        this.isOn = valor.toBooleanStrictOrNull() ?: run {
            println("ERROR: Valor de status '$valor' no es un booleano válido para SmartOutlet '${this.name}'.")
            return // No cambiar el estado si el valor es inválido
        }
        println("INFO: Estado del SmartOutlet '${this.name}' actualizado a: ${if(this.isOn) "ENCENDIDO" else "APAGADO"}")
    }

    override fun handleAttributeUpdate(attribute: String, value: String): Boolean {
        return when (attribute.lowercase()) {
            "status" -> {
                setOutletStatus(value)
                true
            }
            else -> super.handleAttributeUpdate(attribute, value)
        }
    }

    override fun executeAction(actionType: ActionType, parameters: String) {
        println("$name ejecutando $actionType...")
        when (actionType) {
            ActionType.TURN_ON -> setOutletStatus("true")
            ActionType.TURN_OFF -> setOutletStatus("false")
            else -> {
                throw UnsupportedOperationException("El SmartOutlet no soporta la acción: $actionType")
            }
        }
    }

    override fun getAttributeValue(attribute: Attribute): Any {
        when (attribute){
            Attribute.IS_ON -> return isOn
            else -> throw IllegalArgumentException("Atributo no soportado por ${name}")
        }
    }

    override fun setAttributeValue(attribute: Attribute, value: String) {
        when (attribute){
            Attribute.IS_ON -> setOutletStatus(value)
            else -> throw IllegalArgumentException("Atributo no soportado por ${name}")
        }
    }

    override fun toDTO(): DeviceDTO {
        return super.toDTO().copy(status = isOn)
    }
}