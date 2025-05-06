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
    val home: Home,

    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL], orphanRemoval = true)
    val deviceList: MutableList<Device> = mutableListOf()
){
    fun addDevice(device: Device){
        deviceList.add(device)
        device.room=this
    }
}
