package de.rosenau.simon.rockpaperscissors.config.security

import de.rosenau.simon.rockpaperscissors.domain.user.query.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(val user: User): UserDetails {
    override fun getAuthorities() = listOf<GrantedAuthority>()
    override fun getPassword() = user.password
    override fun getUsername() = user.username
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true

}