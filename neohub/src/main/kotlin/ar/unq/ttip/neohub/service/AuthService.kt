package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.RegisterRequest
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {


    fun registerUser(dto: RegisterRequest): User {
        val user = User(
            username = dto.username,
            password = passwordEncoder.encode(dto.password),
            email = dto.email,
            enabled = true
        )
        return userRepository.save(user)
    }

    fun authenticate(username: String, password: String): Boolean {
        val user = userRepository.findByUsername(username)
            ?: return false
        return passwordEncoder.matches(password, user.password)
    }
}