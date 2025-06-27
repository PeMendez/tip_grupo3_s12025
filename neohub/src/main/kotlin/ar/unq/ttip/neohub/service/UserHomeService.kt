package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.userHomeRequest
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.Role
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.UserHome
import ar.unq.ttip.neohub.repository.HomeRepository
import ar.unq.ttip.neohub.repository.UserHomeRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UserHomeService(
    private val userHomeRepository: UserHomeRepository,
    private val userService: UserService,
    private val homeRepository: HomeRepository
) {

    fun getRoleForUsername(username: String): String? {
        val userHomes = userHomeRepository.findByUsername(username)
        return userHomes.firstOrNull()?.role?.name
    }

    fun getRoleForUserInCurrentHome(username: String, homeId: Long): String? {
        return userHomeRepository.findByUsernameAndHomeId(username, homeId)?.role?.name
    }


    @Transactional
    fun deleteMember(homeId: Long, user: User, username: String) {
        val userHome = userHomeRepository.findByUsernameAndHomeId(username, homeId)?:
        throw EntityNotFoundException("Relación UserHome no encontrada")

        user.userHomes.remove(userHome)
        userHomeRepository.delete(userHome)
    }

    @Transactional
    fun createUserHome(username: String, request: userHomeRequest): UserHome {
        val user = userService.getUserByUsername(username)
        when (request.action) {
            "CREATE" -> {
                // Crear una nueva home y asignar al usuario como admin
                val newHome = Home(
                    name = request.homeName ?: throw IllegalArgumentException("El nombre de la home es requerido"),
                    accessKey = request.accessKey ?: throw IllegalArgumentException("La clave de acceso es requerida")
                )

                val userHome = UserHome(user = user, home = newHome, role = Role.ADMIN)
                newHome.addUserHome(userHome)
                user.addHome(userHome)
                homeRepository.save(newHome)
                userHomeRepository.save(userHome)
                return userHome;
            }

            "JOIN" -> {
                // Unirse a una home existente
                val home = homeRepository.findByName(request.homeName!!)
                    ?: throw IllegalArgumentException("La home no existe")
                if (home.accessKey != request.accessKey) {
                    throw IllegalArgumentException("Clave de acceso incorrecta")
                }

                val userHome = UserHome(user = user, home = home, role = Role.USER)
                home.addUserHome(userHome)
                //homeRepository.save(home) esto aca estaba duplicando la relación.
                userHomeRepository.save(userHome)
                user.addHome(userHome)
                return userHome;
            }

            else -> throw IllegalArgumentException("Acción no válida")
        }

    }
}
