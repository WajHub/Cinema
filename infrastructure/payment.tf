resource "azurerm_container_group" "payment" {
  name                = "cr-cinema-payment-service-dev-pl-01"
  location            = "polandcentral"
  resource_group_name = "rg-cinema-dev-pl"
  ip_address_type     = "Public"
  os_type             = "Linux"
  restart_policy      = "Never"
  sku                 = "Standard"

  exposed_port = [{
    port     = 8084
    protocol = "TCP"
    }, {
    port     = 80
    protocol = "TCP"
  }]

  dns_name_label = "cinema-payment-service-dev"

  container {
    name         = "cr-cinema-payment-service-dev-pl-01"
    image        = "crcinemadevpl01.azurecr.io/cinema-payment-service:latest"
    cpu          = 1
    cpu_limit    = 1
    memory       = 1.5
    memory_limit = 1.5

    environment_variables = {
      PAYMENT_DB_URL                    = "jdbc:postgresql://psql-cinema-dev-pl.postgres.database.azure.com:5432/payment_service"
      PAYMENT_DB_USERNAME               = "psqladmin"
      KAFKA_BOOTSTRAP_SERVER            = var.kafka_server
      SCHEMA_REGISTRY_URL               = var.schema_registry_url
      STRIPE_SESSION_EXPIRATION_MINUTES = "30"
      STRIPE_SUCCESS_URL                = "http://cinema-payment-service-dev.polandcentral.azurecontainer.io:8084/api/v1/payments/success?bookingId={BOOKING_ID}"
      STRIPE_CANCEL_URL                 = "http://cinema-payment-service-dev.polandcentral.azurecontainer.io:8084/api/v1/payments/cancel?bookingId={BOOKING_ID}"
    }

    secure_environment_variables = {
      PAYMENT_DB_PASSWORD   = var.db_password
      STRIPE_SECRET_KEY     = var.stripe_secret_key
      STRIPE_WEBHOOK_SECRET = var.stripe_webhook_secret
    }

    ports {
      port     = 8084
      protocol = "TCP"
    }
    ports {
      port     = 80
      protocol = "TCP"
    }
  }

  image_registry_credential {
    server   = var.acr_server
    username = var.acr_username
    password = var.acr_password
  }
}