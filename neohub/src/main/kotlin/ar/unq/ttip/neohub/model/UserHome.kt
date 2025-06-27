package ar.unq.ttip.neohub.model
import ar.unq.ttip.neohub.dto.UserHomeDTO
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import ar.unq.ttip.neohub.dto.toDTO
@Entity
data class UserHome(
    /* Esta clase tiene la lista de roles de cada usuario en cada home */
    @Id
    @GeneratedValue val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    var user: User? = null,

    @ManyToOne
    @JoinColumn(name = "home_id", nullable = false)
    @JsonBackReference
    var home: Home? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role // "admin" o "user"
){
    fun toDTO(): UserHomeDTO {
        return UserHomeDTO(
            user = toDTO(user!!),
            homeDTO= home!!.toDTO(),
            role= role.toString()
        )
    }
    override fun toString(): String {
        return "UserHome(id=$id, user=${user!!.username}, home=$home, role=$role)"
    }

}
