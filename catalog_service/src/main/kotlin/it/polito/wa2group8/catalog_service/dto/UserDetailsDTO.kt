package it.polito.wa2group8.catalog_service.dto

import it.polito.wa2group8.catalog_service.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size
import org.springframework.security.core.authority.SimpleGrantedAuthority

import java.util.ArrayList

data class LoginBody(
    @get:NotEmpty val username: String,
    @get:Size(min = 8) val password: String
)

data class UserDetailsDTO(
    private val username: String,
    private val password: String?,
    private val email: String?,
    private val isEnabled: Boolean?,
    private val roles: Set<String>,
    private val deliveryAddress: String,
): UserDetails
{
    override fun getPassword(): String = password ?: ""

    override fun getUsername(): String = username

    override fun isEnabled(): Boolean = isEnabled ?: false

    override fun getAuthorities(): MutableCollection<out GrantedAuthority>
    {
        val authorities: MutableList<SimpleGrantedAuthority> = ArrayList()
        for (role in roles)
            authorities.add(SimpleGrantedAuthority(role))
        return authorities
    }

    fun getEmail(): String = email ?: ""

    fun getRoles(): Set<String> = roles

    fun getDeliveryAddress(): String = deliveryAddress

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true
}

fun User.toUserDetailsDTO() = UserDetailsDTO(username, password, email, enabled, getRolenames(), getDeliveryAddress())
