package ar.unq.ttip.neohub.model
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
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

    @Column(nullable = false)
    val role: String // "admin" o "user"
)
