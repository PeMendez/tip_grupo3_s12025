package ar.unq.ttip.neohub.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
data class Home(
    @Id @GeneratedValue val id: Long = 0,

    @Column(unique = true, nullable = false)
    val name: String,

    @Column(nullable = false)
    val accessKey: String, // Clave para unirse a la home

    @OneToMany(mappedBy = "home", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonManagedReference
    val userHomes: MutableList<UserHome> = mutableListOf(),

    @OneToMany(mappedBy = "home", cascade = [CascadeType.PERSIST, CascadeType.MERGE], fetch = FetchType.LAZY)
    @JsonManagedReference
    val rooms: MutableList<Room> = mutableListOf()
) {
    fun addRoom(room: Room) {
        rooms.add(room)
        room.home = this
    }

    fun addUser(userHome: UserHome) {
        userHomes.add(userHome)
        userHome.home = this
    }
}
