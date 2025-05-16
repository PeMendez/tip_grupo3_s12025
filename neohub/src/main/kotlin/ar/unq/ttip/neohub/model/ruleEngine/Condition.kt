package ar.unq.ttip.neohub.model.ruleEngine

import ar.unq.ttip.neohub.model.Attribute
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Operator
import ar.unq.ttip.neohub.model.devices.DeviceType
import jakarta.persistence.*

@Entity
@Table(name="rule_condition") //condition es palabra reservada
data class Condition(
    @Id @GeneratedValue val id: Long = 0,
    @ManyToOne @JoinColumn(name = "rule_id")
    var rule: Rule? = null,

    @ManyToOne @JoinColumn(name = "device_id")
    val device: Device,

    val attribute: Attribute,  // Ejemplo: "temperature"
    @Enumerated(EnumType.STRING)
    val operator: Operator,   // Ejemplo: ">"
    @Column(name="condition_value") //value es palabra reservada
    val value: String,       // Ejemplo: "25"
){
    fun validate() {
        // Obtener los atributos soportados por el tipo de dispositivo
        val supportedAttributes = DeviceType.getSupportedAttributes(device.type)
        if (!supportedAttributes.contains(attribute)) {
            throw IllegalArgumentException("El atributo $attribute no es soportado por dispositivos del tipo ${device.type}")
        }

        // Validar que el operador es compatible con el atributo
        val supportedOperators = Attribute.getSupportedOperators(attribute)
        if (!supportedOperators.contains(operator)) {
            throw IllegalArgumentException("El operador $operator no es soportado por el atributo $attribute del dispositivo ${device.type}")
        }
    }

    fun evaluate(): Boolean {
        val attributeValue = device.getAttributeValue(attribute)
        return operator.apply(attributeValue.toString(), value)
    }

}
