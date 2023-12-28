package it.polito.wa2group8.catalog_service.services

import it.polito.wa2group8.catalog_service.domain.EmailVerificationToken
import it.polito.wa2group8.catalog_service.domain.User
import it.polito.wa2group8.catalog_service.repositories.EmailVerificationTokenRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.cloud.gateway.route.Route.async
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.system.exitProcess


@Service
//@Transactional
@EnableScheduling
class NotificationServiceImpl(private val emailVerificationTokenRepository: EmailVerificationTokenRepository) : NotificationService
{
    override suspend fun createEmailVerificationToken(user: User): String
    {
        val token = UUID.randomUUID().toString()
        val timestamp = LocalDateTime.now().plusHours(5)
        val emailVerificationToken = EmailVerificationToken( timestamp, token, user.username)
        emailVerificationTokenRepository.save(emailVerificationToken)
        return token
    }

    @FlowPreview
    @Scheduled(fixedDelay = 60000)
    fun checkExpiration()
    {
        runBlocking { deleteTokens() }
    }

    @FlowPreview
    suspend fun deleteTokens()
    {
        val expiredList = emailVerificationTokenRepository.findEmailVerificationTokensByExpiryDateIsBefore(LocalDateTime.now()).toList()
        if (expiredList.isNotEmpty())
            println("Automatic delete expired token:\n ${expiredList.map{it.token}}")
        emailVerificationTokenRepository.deleteAll(expiredList)
    }
}
