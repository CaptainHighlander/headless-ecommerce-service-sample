package it.polito.wa2group8.catalog_service.services

import it.polito.wa2group8.catalog_service.dto.EmailEvent
import it.polito.wa2group8.catalog_service.dto.FROM_ORDER_TOPIC
import it.polito.wa2group8.catalog_service.dto.FROM_WAREHOUSE_TOPIC
import it.polito.wa2group8.catalog_service.dto.QuantityAlarmEvent
import it.polito.wa2group8.catalog_service.repositories.UserRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Value("\${spring.mail.host}")
private val host : String = ""
@Value("\${spring.mail.port}")
private val port: Int = 0
@Value("\${spring.mail.username}")
private val username: String = ""
@Value("\${spring.mail.password}")
private val password: String = ""
@Value("\${spring.mail.properties.mail.smtp.auth}")
private val auth: Boolean = true
@Value("\${spring.mail.properties.mail.smtp.starttls.enable}")
private val starttls: Boolean = true
@Value("\${spring.mail.properties.mail.debug}")
private val debug: Boolean = true

@Bean
fun getMailSender() : JavaMailSender
{
    // Setting Mail Sender
    val mailSender = JavaMailSenderImpl()
    mailSender.host = host
    mailSender.port = port
    mailSender.username = username
    mailSender.password = password
    println("$password $username")
    // Setting properties
    val properties = mailSender.javaMailProperties
    properties["spring.mail.properties.mail.smtp.auth"] = auth
    properties["spring.mail.properties.mail.smtp.starttls.enable"] = starttls
    properties["spring.mail.properties.mail.debug"] = debug
    // Returning Mail Sender
    return mailSender
}


@Service
@EnableKafka
class MailServiceImpl(private val mailSender : JavaMailSender, private val userRepository: UserRepository): MailService
{
    override fun sendMessage(toMail: String, subject: String, mailBody: String)
    {
        // Send the message
        Mono.fromCallable {
            try
            {
                val helper = MimeMessageHelper(mailSender.createMimeMessage());
                helper.setTo(toMail);
                helper.setSubject(subject);
                helper.setText(mailBody);
                mailSender.send(helper.mimeMessage);
            }
            catch (e: Exception) { }
        }
        .subscribe()
    }

    @KafkaListener(topics = [FROM_ORDER_TOPIC])
    fun consumeFromOrder(message: String)
    {
        //Get event
        val event = EmailEvent.createByString(message)
        val user = runBlocking { userRepository.findUserByUsername(event.username) } ?: return
        // Create email message
        val text = "Your order ${event.orderId} now is ${event.status}"
        //Send an email to the user who performed the order
        sendMessage(user.email, "Order status", text)
        println("Email to user ${user.username} ok")
    }

    @FlowPreview
    @KafkaListener(topics = [FROM_WAREHOUSE_TOPIC])
    fun consumeFromWarehouse(message: String)
    {
        //Get event
        val event = QuantityAlarmEvent.createByString(message)
        // Create email message
        val text = "The quantity of product having ID ${event.productId} is " +
                "below the alarm level in the warehouse having id ${event.warehouseId}"
        //Get admins
        val admins = runBlocking { userRepository.findAllAdmins().toList() }
        //Send an email to each admin
        if (admins.isNotEmpty())
            admins.forEach { sendMessage(it.email, "Product alarm", text) }
        println("Email to admins $admins ok")
    }
}
