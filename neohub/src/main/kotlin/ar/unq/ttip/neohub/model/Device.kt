package ar.unq.ttip.neohub.model

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.devices.DeviceType
import jakarta.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Device(
    @Id @GeneratedValue val id: Long = 0,
    val name: String,
    @Enumerated(EnumType.STRING)
    val type: DeviceType,
    var topic: String = "neohub/unconfigured", //al instanciarse aun no esta configurado.

    @ManyToOne
    @JoinColumn(name = "room_id")
    var room: Room? = null, //al instanciarse un nuevo device, no está en ningún room

    var macAddress: String? = null,
)
{
    fun configureTopic() {
        topic = if (room == null) {
            "neohub/unconfigured"
        } else {
            "neohub/${room!!.name}/$type/$name"
        }
    }

    fun toDTO(): DeviceDTO{
        return DeviceDTO(
            id = id,
            name = name,
            type = type.toString(),
            topic = topic,
            roomId = room?.id,
        )
    }

    override fun toString(): String {
        return "Device(id=$id, name='$name', type='${type}', topic='$topic')"
    }

    abstract fun handleIncomingMessage(message: String)
    abstract fun executeAction(actionType: ActionType, parameters: String)
    abstract fun getAttributeValue(attribute: Attribute): Any
    abstract fun setAttributeValue(valor: String)
}
