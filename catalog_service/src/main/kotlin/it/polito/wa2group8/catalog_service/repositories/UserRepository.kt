package it.polito.wa2group8.catalog_service.repositories

import it.polito.wa2group8.catalog_service.domain.User
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: CoroutineCrudRepository<User, Long>
{
    suspend fun findUserByUsername(username: String): User?
    suspend fun findUserByEmail(email: String): User?

    @FlowPreview
    @Query("SELECT * FROM user WHERE is_admin = true")
    suspend fun findAllAdmins(): Flow<User>

    @Modifying
    @Query("UPDATE user SET enabled = true WHERE username = :username")
    suspend fun enableUser(username: String)

    @Modifying
    @Query("UPDATE user SET enabled = false WHERE username = :username")
    suspend fun disableUser(username: String)
}
