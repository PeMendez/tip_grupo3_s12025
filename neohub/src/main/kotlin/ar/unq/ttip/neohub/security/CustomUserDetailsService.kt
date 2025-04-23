package ar.unq.ttip.neohub.security

import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user: User = userRepository.findByUsername(username)?: throw UsernameNotFoundException("User not found with username: $username")
        println("User found: $user")

        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            emptyList()
        )
    }
}
