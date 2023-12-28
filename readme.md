# Distributed headless eCommerce Service

**Disclaimer:** This is a sample eCommerce application designed for educational purposes. It is not intended for production use.

## Welcome

Welcome to the Distributed eCommerce Service project! This headless eCommerce application is built with a microservices architecture using Spring, Kotlin, Docker, Apache Kafka, and Debezium.

## Project Overview

The Distributed eCommerce Service comprises four microservices:

- **CatalogService:** Handles authentication and customer interactions, allowing users to list products, view features, and place orders.

- **OrderService:** Core service responsible for storing and managing orders. Ensures transactional consistency and handles order-related operations.

- **WalletService:** Manages customer wallets, allowing users to query balances, view transaction history, and add new transactions.

- **WarehouseService:** Manages the list of products stored in warehouses, handling product quantities, loading/unloading items, and updating alarms.

Microservices communication is both synchronous (using REST and GraphQL) and asynchronous (via Kafka).
Each microservice has its SQL database (Maria DB).
Services are deployed as separate runtime processes, preferably in Docker containers, to host microservices inside a single operating system. All containers are interconnected leveraging Docker’s internal network, enabling communication. Additionally, the CatalogService is exposed to the external network to be reachable from outside. The DB instance runs in its container.

Debezium is utilized for Change Data Capture (CDC).
