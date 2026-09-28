resource "azurerm_container_app" "booking" {
  name                         = "ca-booking-service-dev-pl-01"
  container_app_environment_id = azurerm_container_app_environment.dev.id
  resource_group_name          = azurerm_resource_group.rg.name
  revision_mode                = "Single"

  registry {
    server               = var.acr_server
    username             = var.acr_username
    password_secret_name = "acr-password"
  }

  ingress {
    external_enabled = true
    target_port      = 8082
    traffic_weight {
      percentage      = 100
      latest_revision = true
    }
  }

  secret {
    name  = "acr-password"
    value = var.acr_password
  }

  secret {
    name  = "db-password"
    value = var.db_password
  }

  template {
    min_replicas = 0
    max_replicas = 1

    container {
      name   = "booking-service"
      image  = "crcinemadevpl1.azurecr.io/cinema-booking-service:latest"
      cpu    = 0.5
      memory = "1.0Gi"

      env {
        name  = "BOOKING_DB_URL"
        value = "jdbc:postgresql://psql-cinema-dev-pl.postgres.database.azure.com:5432/booking_service"
      }
      env {
        name  = "BOOKING_DB_USERNAME"
        value = "psqladmin"
      }
      env {
        name        = "BOOKING_DB_PASSWORD"
        secret_name = "db-password"
      }
      env {
        name  = "KAFKA_BOOTSTRAP_SERVER"
        value = var.kafka_server
      }
      env {
        name  = "SCHEMA_REGISTRY_URL"
        value = var.schema_registry_url
      }
      env {
        name  = "PAYMENT_SERVICE_URL"
        value = "https://ca-payment-service-dev-pl-01.${azurerm_container_app_environment.dev.default_domain}"
      }
      env {
        name  = "KAFKA_AUTO_CREATE_TOPICS"
        value = "false"
      }
    }
  }
}
