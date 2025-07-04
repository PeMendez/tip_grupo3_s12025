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
    var deviceList: MutableList<Device> = mutableListOf()
){
    fun addDevice(device: Device){
        deviceList.add(device)
        device.room=this
        device.configure()
    }

    fun removeDevice(device: Device){
        deviceList.remove(device)
        device.room=null
        device.configure()
    }

    override fun toString(): String {
        return "Room(id=$id, name=$name, devices=$deviceList)"
    }
}
