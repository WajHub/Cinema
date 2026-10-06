# creating user-assigned identity
resource "azurerm_user_assigned_identity" "id_payment" {
  name                = "id-payment-service-dev"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
}

resource "azurerm_container_app" "payment" {
  name                         = "ca-payment-service-dev-pl-01"
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
    target_port      = 8084
    traffic_weight {
      percentage      = 100
      latest_revision = true
    }
  }

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.id_payment.id]
  }

  secret {
    name                = "acr-password"
    identity            = azurerm_user_assigned_identity.id_payment.id
    key_vault_secret_id = "${azurerm_key_vault.key_vault.vault_uri}secrets/acr-password"
  }

  secret {
    name                = "db-password"
    identity            = azurerm_user_assigned_identity.id_payment.id
    key_vault_secret_id = "${azurerm_key_vault.key_vault.vault_uri}secrets/db-password"
  }

  secret {
    name                = "stripe-secret-key"
    identity            = azurerm_user_assigned_identity.id_payment.id
    key_vault_secret_id = "${azurerm_key_vault.key_vault.vault_uri}secrets/stripe-secret-key"
  }

  secret {
    name                = "stripe-webhook-secret"
    identity            = azurerm_user_assigned_identity.id_payment.id
    key_vault_secret_id = "${azurerm_key_vault.key_vault.vault_uri}secrets/stripe-webhook-secret"
  }

  template {
    min_replicas = 0
    max_replicas = 1

    container {
      name   = "payment-service"
      image  = "crcinemadevpl1.azurecr.io/cinema-payment-service:latest"
      cpu    = 0.5
      memory = "1Gi"

      env {
        name  = "PAYMENT_DB_URL"
        value = "jdbc:postgresql://psql-cinema-dev-pl.postgres.database.azure.com:5432/payment_service"
      }
      env {
        name  = "PAYMENT_DB_USERNAME"
        value = "psqladmin"
      }
      env {
        name        = "PAYMENT_DB_PASSWORD"
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
        name        = "STRIPE_SECRET_KEY"
        secret_name = "stripe-secret-key"
      }
      env {
        name        = "STRIPE_WEBHOOK_SECRET"
        secret_name = "stripe-webhook-secret"
      }
      env {
        name  = "STRIPE_SESSION_EXPIRATION_MINUTES"
        value = "30"
      }
      env {
        name  = "STRIPE_SUCCESS_URL"
        value = "https://ca-payment-service-dev-pl-01.${azurerm_container_app_environment.dev.default_domain}/api/v1/payments/success?bookingId={BOOKING_ID}"
      }
      env {
        name  = "STRIPE_CANCEL_URL"
        value = "https://ca-payment-service-dev-pl-01.${azurerm_container_app_environment.dev.default_domain}/api/v1/payments/cancel?bookingId={BOOKING_ID}"
      }
      env {
        name  = "KAFKA_AUTO_CREATE_TOPICS"
        value = "false"
      }
    }
  }
}

