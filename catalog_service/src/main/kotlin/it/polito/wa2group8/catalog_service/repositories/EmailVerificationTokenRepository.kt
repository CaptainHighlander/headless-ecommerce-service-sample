package it.polito.wa2group8.catalog_service.repositories

import it.polito.wa2group8.catalog_service.domain.EmailVerificationToken
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface EmailVerificationTokenRepository: CoroutineCrudRepository<EmailVerificationToken, Long>
{
    suspend fun findEmailVerificationTokenByToken(token: String) : EmailVerificationToken?

    @FlowPreview
    fun findEmailVerificationTokensByExpiryDateIsBefore(timestamp: LocalDateTime) : Flow<EmailVerificationToken>
}
