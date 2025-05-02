package ar.unq.ttip.neohub.model

import jakarta.persistence.*

@Entity
data class Device(
    @Id @GeneratedValue val id: Long = 0,
    val name: String,
    val type: String,

    @ManyToOne
    @JoinColumn(name = "room_id")
    val room: Room
)
