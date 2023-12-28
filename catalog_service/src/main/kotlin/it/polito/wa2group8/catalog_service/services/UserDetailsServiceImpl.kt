package it.polito.wa2group8.catalog_service.services

import it.polito.wa2group8.catalog_service.domain.User
import it.polito.wa2group8.catalog_service.dto.*
import it.polito.wa2group8.catalog_service.exceptions.BadCredentialsException
import it.polito.wa2group8.catalog_service.exceptions.BadRequestException
import it.polito.wa2group8.catalog_service.exceptions.ExpiredTokenException
import it.polito.wa2group8.catalog_service.exceptions.NotFoundException
import it.polito.wa2group8.catalog_service.repositories.EmailVerificationTokenRepository
import it.polito.wa2group8.catalog_service.repositories.UserRepository
import it.polito.wa2group8.catalog_service.security.JwtUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.InetAddress
import java.time.LocalDateTime

@Service
@Transactional
class UserDetailsServiceImpl(
    private val userRepository: UserRepository,
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val mailService: MailService,
    private val notificationService: NotificationService,
    private val jwtUtils: JwtUtils
): UserDetailsService
{
    @Autowired
    private val webServerAppContext: ReactiveWebServerApplicationContext? = null

    private val passwordEncoder = BCryptPasswordEncoder()

    override suspend fun findByUsername(username: String): UserDetailsDTO
    {
        val user = userRepository.findUserByUsername(username) ?: throw BadCredentialsException("Invalid Credentials")
        return user.toUserDetailsDTO()
    }

    override suspend fun createUser(registrationRequest: RegistrationRequestDTO): UserDetailsDTO
    {
        // Check if email is already used
        if(userRepository.findUserByEmail(registrationRequest.email) != null)
            throw BadRequestException("Email is already used")
        // Check if username is already in the DB
        if (userRepository.findUserByUsername(registrationRequest.username) != null)
            throw BadRequestException("Username already exist")
        // Save user in the DB
        val user = userRepository.save(User(registrationRequest.username, passwordEncoder.encode(registrationRequest.password), registrationRequest.email, false, registrationRequest.city, registrationRequest.street, registrationRequest.zip, roles="CUSTOMER"))

        // Create email message
        val token = notificationService.createEmailVerificationToken(user)
        val addr = InetAddress.getLoopbackAddress().hostAddress
        val port = webServerAppContext?.webServer?.port
        val text = "http://$addr:$port/auth/registrationConfirm?token=$token"
        // Send email
        mailService.sendMessage(user.email, "Confirm your registration", text)

        return user.toUserDetailsDTO()
    }

    override suspend fun addRoleToUser(role: String, username: String)
    {
        val user = userRepository.findUserByUsername(username) ?: throw BadRequestException("Username does not exist")
        try
        {
            user.addRolename(role)
            userRepository.save(user)
        }
        catch (ex: Exception)
        {
            throw ex
        }
    }

    override suspend fun removeRoleToUser(role: String, username: String)
    {
        val user = userRepository.findUserByUsername(username) ?: throw BadRequestException("Username does not exist")
        try
        {
            user.removeRolename(role)
            userRepository.save(user)
        }
        catch (ex: Exception)
        {
            throw ex
        }
    }

    override suspend fun enableUser(username: String)
    {
        if (userRepository.findUserByUsername(username) == null)
            throw BadRequestException("Username does not exist")
        userRepository.enableUser(username)
    }

    override suspend fun disableUser(username: String)
    {
        if (userRepository.findUserByUsername(username) == null)
            throw BadRequestException("Username does not exist")
        userRepository.disableUser(username)
    }

    override suspend fun confirmRegistration(token: String): String
    {
        val userToken = emailVerificationTokenRepository.findEmailVerificationTokenByToken(token)
            ?: throw NotFoundException("Token not valid.")
        if (userToken.expiryDate < LocalDateTime.now())
            throw ExpiredTokenException("The token is expired.")
        val user = userRepository.findUserByUsername(userToken.username)
            ?: throw NotFoundException("User not found.")
        if (user.enabled)
            return "User is already registered."
        user.enabled = true
        userRepository.save(user)
        return "Your registration is completed successfully."
    }

    override suspend fun getUserInfo(token: String): UserInfoDTO
    {
        val loggedUser = jwtUtils.getUserFromJwtToken(jwtUtils.getTokenFromHeader(token))
        val user = userRepository.findUserByUsername(loggedUser)
        return user!!.toUserInfoDTO()
    }

    override suspend fun updateUserInfo(token: String, newInfo: UserInfoDTO)
    {
        val loggedUser = jwtUtils.getUserFromJwtToken(jwtUtils.getTokenFromHeader(token))
        val user = userRepository.findUserByUsername(loggedUser)!!
        user.email = newInfo.email ?: user.email
        user.city = newInfo.city ?: user.city
        user.street = newInfo.street ?: user.street
        user.zip = newInfo.zip ?: user.zip
        userRepository.save(user)
    }

    override suspend fun changePassword(token: String, passwordDTO: PasswordDTO)
    {
        val loggedUser = jwtUtils.getUserFromJwtToken(jwtUtils.getTokenFromHeader(token))
        val user = userRepository.findUserByUsername(loggedUser)
        if(!passwordEncoder.matches(passwordDTO.oldPassword, user!!.password))
            throw BadRequestException("The old password is wrong")
        if(passwordDTO.newPassword!=passwordDTO.confirmPassword)
            throw BadRequestException("The confirm password doesn't match")
        user.password = passwordEncoder.encode(passwordDTO.newPassword)
        userRepository.save(user)
    }
}
