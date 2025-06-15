package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.UserHome
import ar.unq.ttip.neohub.repository.UserHomeRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UserHomeService(private val userHomeRepository: UserHomeRepository) {

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
}