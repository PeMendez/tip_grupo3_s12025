package ar.unq.ttip.neohub.model

import jakarta.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Device(
    @Id @GeneratedValue val id: Long = 0,
    val name: String,
    val type: String,
    var topic: String = "neohub/unconfigured", //al instanciarse aun no esta configurado.

    @ManyToOne
    @JoinColumn(name = "room_id")
    var room: Room? = null //al instanciarse un nuevo device, no está en ningún room

)
{
    fun configureTopic() {
        topic = if (room == null) {
            "neohub/unconfigured"
        } else {
            "neohub/${room!!.name}/$type/$name"
        }
    }

    abstract fun handleIncomingMessage(message: String)
}
