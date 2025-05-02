package ar.unq.ttip.neohub.model

import jakarta.persistence.*

@Entity
data class Room(
    @Id @GeneratedValue val id: Long = 0,
    val name: String,

//    @ManyToOne
//    @JoinColumn(name = "home_id")
//    val home: Home,

    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL], orphanRemoval = true)
    val deviceList: MutableList<Device> = mutableListOf()
)
