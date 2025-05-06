package ar.unq.ttip.neohub.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
data class Home(
    @Id @GeneratedValue val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @OneToMany(mappedBy = "home", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    val rooms: List<Room> = mutableListOf()
)
{
    override fun toString(): String {
        return "Home(id=$id, user=$user, rooms=$rooms)"
    }
}