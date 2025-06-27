package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {
    fun getUserByUsername(username: String): User {
        return userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("Usuario no encontrado")
    }

    fun getUserById(id: Long): User {
        return userRepository.findById(id)
        .orElseThrow { IllegalArgumentException("Usuario con ID $id no encontrado") }
    }
}