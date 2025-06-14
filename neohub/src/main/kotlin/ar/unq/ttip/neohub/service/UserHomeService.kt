package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.repository.UserHomeRepository
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
}