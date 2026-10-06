resource "azurerm_key_vault" "key_vault" {
  name                        = "kv-cinema-pl-dev"
  location                    = azurerm_resource_group.rg.location
  resource_group_name         = azurerm_resource_group.rg.name
  rbac_authorization_enabled  = true
  enabled_for_disk_encryption = true
  purge_protection_enabled    = true
  tenant_id                   = data.azurerm_client_config.current.tenant_id
  soft_delete_retention_days  = 7
  sku_name                    = "standard"
}

resource "azurerm_role_assignment" "role_secret_officer" {
  scope = "/subscriptions/${data.azurerm_client_config.current.subscription_id}/resourceGroups/${azurerm_resource_group.rg.name}"
  # role_definition_name = "Key Vault Secrets Officer"
  role_definition_id = "/subscriptions/${data.azurerm_client_config.current.subscription_id}/providers/Microsoft.Authorization/roleDefinitions/b86a8fe4-44ce-4948-aee5-eccb2c155cd7"
  principal_id       = data.azurerm_client_config.current.object_id
}


resource "azurerm_role_assignment" "role_secret_reader_catalog" {
  scope = azurerm_key_vault.key_vault.id
  # role_definition_name = "Key Vault Secrets User"
  role_definition_id = "/subscriptions/${data.azurerm_client_config.current.subscription_id}/providers/Microsoft.Authorization/roleDefinitions/4633458b-17de-408a-b874-0445c86b69e6"
  principal_id       = azurerm_user_assigned_identity.id_catalog.principal_id
  principal_type     = "ServicePrincipal"
}

resource "azurerm_role_assignment" "role_secret_reader_booking" {
  scope = azurerm_key_vault.key_vault.id
  # role_definition_name = "Key Vault Secrets User"
  role_definition_id = "/subscriptions/${data.azurerm_client_config.current.subscription_id}/providers/Microsoft.Authorization/roleDefinitions/4633458b-17de-408a-b874-0445c86b69e6"
  principal_id       = azurerm_user_assigned_identity.id_booking.principal_id
  principal_type     = "ServicePrincipal"
}

resource "azurerm_role_assignment" "role_secret_reader_payment" {
  scope = azurerm_key_vault.key_vault.id
  # role_definition_name = "Key Vault Secrets User"
  role_definition_id = "/subscriptions/${data.azurerm_client_config.current.subscription_id}/providers/Microsoft.Authorization/roleDefinitions/4633458b-17de-408a-b874-0445c86b69e6"
  principal_id       = azurerm_user_assigned_identity.id_payment.principal_id
  principal_type     = "ServicePrincipal"
}

