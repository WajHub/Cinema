resource "azurerm_container_group" "booking" {
  name                = "cr-cinema-booking-service-dev-pl-01"
  resource_group_name = "rg-cinema-dev-pl"
  location            = "polandcentral"
  ip_address_type     = "Public"
  os_type             = "Linux"
  restart_policy      = "Never"

  exposed_port = [{
    port     = 8082
    protocol = "TCP"
    }, {
    port     = 80
    protocol = "TCP"
  }]

  dns_name_label = "cinema-booking-service-dev"

  container {
    name         = "cr-cinema-booking-service-dev-pl-01"
    image        = "crcinemadevpl01.azurecr.io/cinema-booking-service:latest"
    cpu          = 1
    cpu_limit    = 1
    memory       = 1
    memory_limit = 1

    environment_variables = {
      BOOKING_DB_URL         = "jdbc:postgresql://psql-cinema-dev-pl.postgres.database.azure.com:5432/booking_service"
      BOOKING_DB_USERNAME    = "psqladmin"
      KAFKA_BOOTSTRAP_SERVER = var.kafka_server
      SCHEMA_REGISTRY_URL    = var.schema_registry_url
      PAYMENT_SERVICE_URL    = "http://cinema-payment-service-dev.polandcentral.azurecontainer.io:8084"
    }

    secure_environment_variables = {
      BOOKING_DB_PASSWORD = var.db_password
    }

    ports {
      port     = 8082
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
