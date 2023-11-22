package de.rosenau.simon.rockpaperscissors.config.security

import de.rosenau.simon.rockpaperscissors.domain.user.api.query.GetUserByUsernameQuery
import de.rosenau.simon.rockpaperscissors.domain.user.query.model.User
import org.axonframework.queryhandling.QueryGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Autowired
    private lateinit var queryGateway: QueryGateway

    @Bean
    fun userDetailsService(): UserDetailsService {
        return UserDetailsService { username: String ->
            val query = GetUserByUsernameQuery(username)
            queryGateway.query(query, User::class.java)
                .thenApply { UserPrincipal(it) }
                .join()
        }
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            // Configure cors and csrf
            .cors { it.configurationSource { _ -> CorsConfiguration().applyPermitDefaultValues() } }
            .csrf { it.disable() }
            // Set session management to stateless
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // Set permissions on endpoints
            .authorizeHttpRequests {
                it
                    // Register endpoint is publicly accessible
                    .requestMatchers(HttpMethod.POST, "/v1/users").permitAll()
                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            }
            .httpBasic(Customizer.withDefaults())
            .build()
    }
}