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

  container {
    name         = "cr-cinema-payment-service-dev-pl-01"
    image        = "crcinemadevpl01.azurecr.io/cinema-payment-service:latest"
    cpu          = 1
    cpu_limit    = 1
    memory       = 1.5
    memory_limit = 1.5

    environment_variables = {
      PAYMENT_DB_URL         = "jdbc:postgresql://psql-cinema-dev-pl.postgres.database.azure.com:5432/payment_service"
      PAYMENT_DB_USERNAME    = "psqladmin"
      PAYMENT_DB_PASSWORD    = var.db_password
      KAFKA_BOOTSTRAP_SERVER = var.kafka_server
      SCHEMA_REGISTRY_URL    = var.schema_registry_url
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