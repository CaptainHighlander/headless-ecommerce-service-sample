package it.polito.wa2group8.catalog_service.services

import reactor.core.publisher.Mono

interface MailService
{
    fun sendMessage(toMail:String, subject: String, mailBody: String)
}
