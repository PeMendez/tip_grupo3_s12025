package ar.unq.ttip.neohub.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
data class Room(
    @Id @GeneratedValue val id: Long = 0,
    val name: String,

    @ManyToOne
    @JoinColumn(name = "home_id")
    @JsonBackReference
    var home: Home?,

    @OneToMany(mappedBy = "room", cascade = [CascadeType.PERSIST, CascadeType.MERGE], fetch = FetchType.LAZY)
    val deviceList: MutableList<Device> = mutableListOf()
){
    fun addDevice(device: Device){
        deviceList.add(device)
        device.room=this
        device.configureTopic()
    }
    override fun toString(): String {
        return "Room(id=$id, name=$name, devices=$deviceList)"
    }
}
