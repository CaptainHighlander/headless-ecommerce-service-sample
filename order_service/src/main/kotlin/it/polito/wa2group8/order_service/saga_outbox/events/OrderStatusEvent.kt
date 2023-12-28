package it.polito.wa2group8.order_service.saga_outbox.events

enum class OrderStatusEvent
{
    STARTED,
    FINISHED,
    ACCEPTED,
    REJECTED,
    COMPENSATING,
    COMPENSATED
}
