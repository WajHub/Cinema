resource "azurerm_container_app" "catalog" {
  name                         = "ca-catalog-service-dev-pl-01"
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
    target_port      = 8083
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
      name   = "catalog-service"
      image  = "crcinemadevpl1.azurecr.io/cinema-catalog-service:latest"
      cpu    = 0.5
      memory = "1.0Gi"
      env {
        name  = "CATALOG_DB_URL"
        value = "jdbc:postgresql://psql-cinema-dev-pl.postgres.database.azure.com:5432/catalog_service"
      }
      env {
        name  = "CATALOG_DB_USERNAME"
        value = "psqladmin"
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
        name  = "KAFKA_AUTO_CREATE_TOPICS"
        value = "false"
      }
      env {
        name        = "CATALOG_DB_PASSWORD"
        secret_name = "db-password"
      }
    }
  }
}
