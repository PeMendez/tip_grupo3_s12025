package ar.unq.ttip.neohub.security

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component

@Component
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf {csrf -> csrf.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/api/**", "/api/mqtt/**", "/ws/**").permitAll() // Permite acceso público a /api/**
                    .anyRequest().authenticated() // Requiere autenticación para todos los demás endpoints
            }
        return http.build()
    }
}