package it.polito.wa2group8.wallet_service.saga_outbox.events

enum class OrderStatusEvent
{
    STARTED,
    ACCEPTED,
    REJECTED,
    COMPENSATING,
    COMPENSATED
}
