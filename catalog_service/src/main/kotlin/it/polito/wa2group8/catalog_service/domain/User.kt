package it.polito.wa2group8.catalog_service.domain

import org.springframework.data.relational.core.mapping.Column

data class User (
    var username: String,
    var password: String,
    var email: String,
    var enabled: Boolean = false,
    var city: String,
    var street: String,
    var zip: String,
    var roles: String,
): EntityBase<Long>()
{
    @Column("is_admin")
    var bIsAdmin = false

    enum class Rolename
    {
        CUSTOMER,
        ADMIN
    }

    private inline fun <reified T : Enum<T>> enumContains(name: String): Boolean
    {
        return enumValues<T>().any{ it.name == name }
    }

    fun getRolenames(): Set<String>
    {
        val set: MutableSet<String> = mutableSetOf()
        set.addAll(roles.split(','))
        return set
    }

    fun addRolename(role: String)
    {
        if (!enumContains<Rolename>(role)) throw RuntimeException("Rolename not found")
        if (roles.split(',').contains(role)) throw RuntimeException("Rolename already exist")
        roles = if(roles.isEmpty()) role else "$roles,$role"
        if (role == "ADMIN")
            bIsAdmin = true
    }

    fun removeRolename(role: String)
    {
        if(!enumContains<Rolename>(role)) throw RuntimeException("Rolename not found")
        val rolesSet = getRolenames()
        if (!rolesSet.contains(role)) throw RuntimeException("Role not present")
        roles = rolesSet.filter { it!=role }.joinToString(",")
        if (role == "ADMIN")
            bIsAdmin = false
    }

    fun getDeliveryAddress() = "$city\n$street\n$zip"
}
