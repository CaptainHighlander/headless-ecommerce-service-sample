package it.polito.wa2group8.catalog_service.controllers

import it.polito.wa2group8.catalog_service.dto.*
import it.polito.wa2group8.catalog_service.security.JwtUtils
import it.polito.wa2group8.catalog_service.services.UserDetailsService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import it.polito.wa2group8.catalog_service.security.AuthenticationManager
import it.polito.wa2group8.catalog_service.security.PATH_LOGIN
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import java.security.Principal
import javax.validation.Valid

@RestController
class UserController(val userDetailsService: UserDetailsService, val jwtUtils: JwtUtils, val authenticationManager: AuthenticationManager)
{
    private val passwordEncoder = BCryptPasswordEncoder()

    @GetMapping("/")
    fun root(principal: Principal?): String
    {
        return "Hello, ${principal?.name ?: "guest"}"
    }

    @PostMapping(value = [PATH_LOGIN], produces=[MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    suspend fun login(@RequestBody @Valid authRequest: LoginBody): ResponseEntity<Any>
    {
        //Check user credentials
        val user = authenticationManager.areCredentialsValid(authRequest.username, authRequest.password)
        //If here, credentials are valid so a JWT will be generated and associated to the user
        val jwt = jwtUtils.generateJwtToken(user)
        return ResponseEntity.ok().header("jwt", jwt).body(user.toUserInfoDTO())
    }

    @PostMapping(value=["/auth/register"], produces=[MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    suspend fun register(@RequestBody @Valid registrationRequest: RegistrationRequestDTO): ResponseEntity<Any>
    {
        // Check password
        if (registrationRequest.password != registrationRequest.confirmPassword)
            return ResponseEntity.badRequest().body("Password and confirmPassword do not match")
        // Add new user
        val user = userDetailsService.createUser(registrationRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(user.toUserInfoDTO())
    }

    @GetMapping(value=["/auth/registrationConfirm"], produces=[MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    suspend fun registrationConfirm(@RequestParam token : String): ResponseEntity<Any>
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(userDetailsService.confirmRegistration(token))
    }

    //Retrieve user information
    @GetMapping(value = ["/users"], produces=[MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    suspend fun retrieveUserInfo(@RequestHeader("Authorization") token: String): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(userDetailsService.getUserInfo(token))
    }

    //Update user information
    @PatchMapping(value = ["/users"], produces=[MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    suspend fun updateUserInfo(
        @RequestHeader("Authorization") token: String,
        @RequestBody userInfoDTO: UserInfoDTO
    ): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(userDetailsService.updateUserInfo(token, userInfoDTO))
    }

    // Change user password
    @PatchMapping(value = ["/users/password"], produces=[MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    suspend fun changePassword(
        @RequestHeader("Authorization") token: String,
        @RequestBody @Valid passwordDTO: PasswordDTO
    ): ResponseEntity<Any>
    {
        return ResponseEntity.ok().body(userDetailsService.changePassword(token, passwordDTO))
    }

    //------------------- ENDPOINTS RESERVED TO ADMINS -------------------

    @GetMapping(value = ["/enable/{username}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseBody
    suspend fun enableUserByUsername(@PathVariable("username") username: String): ResponseEntity<Any>
    {
        //Only admins can enable an user manually
        return ResponseEntity.status(HttpStatus.OK).body(userDetailsService.enableUser(username))
    }

    @GetMapping(value = ["/disable/{username}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseBody
    suspend fun disableUserByUsername(@PathVariable("username") username: String): ResponseEntity<Any>
    {
        //Only admins can disable an user manually
        return ResponseEntity.status(HttpStatus.OK).body(userDetailsService.disableUser(username))
    }
}
