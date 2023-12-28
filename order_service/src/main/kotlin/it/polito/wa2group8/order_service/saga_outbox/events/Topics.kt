package it.polito.wa2group8.order_service.saga_outbox.events

const val TO_WALLET_TOPIC = "walletRequest"
const val FROM_WALLET_TOPIC = "walletResponse"
const val TO_WAREHOUSE_TOPIC = "warehouseRequest"
const val FROM_WAREHOUSE_TOPIC = "warehouseResponse"
const val TO_CATALOG_TOPIC = "orderStatusChanges"
