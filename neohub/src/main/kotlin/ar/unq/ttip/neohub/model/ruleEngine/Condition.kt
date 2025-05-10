package ar.unq.ttip.neohub.model.ruleEngine

import ar.unq.ttip.neohub.model.Device
import jakarta.persistence.*
import kotlin.jvm.Transient

@Entity
@Table(name="rule_condition") //condition es palabra reservada
data class Condition(
    @Id @GeneratedValue val id: Long = 0,
    @ManyToOne @JoinColumn(name = "rule_id")
    var rule: Rule? = null,

    @ManyToOne @JoinColumn(name = "device_id")
    val device: Device,

    val attribute: String,  // Ejemplo: "temperature"
    val operator: String,   // Ejemplo: ">"
    @Column(name="condition_value") //value es palabra reservada
    val value: String,       // Ejemplo: "25"
){
    @Transient
    private val operators = mapOf<String, (String, String) -> Boolean>(
        ">" to { attr, value -> attr.toDouble() > value.toDouble() },
        "<" to { attr, value -> attr.toDouble() < value.toDouble() },
        "==" to { attr, value -> attr == value }
    )

    fun evaluate(): Boolean {
        val attributeValue = getAttributeValue(device, attribute)
        return operators[operator]?.invoke(attributeValue, value)
            ?: throw IllegalArgumentException("Operador no soportado: ${operator}")
    }

    private fun getAttributeValue(device: Device, attribute: String): String {
        return device.getAttribute(attribute)
    }
}
