# Creating resource group
resource "azurerm_resource_group" "rg" {
  name     = "rg-cinema-dev-pl"
  location = "polandcentral"
}

# creating log analytics workspace
resource "azurerm_log_analytics_workspace" "law" {
  name                = "law-cinema-dev-pl"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  sku                 = "PerGB2018"
  retention_in_days   = 30
}

# creating aca environment
resource "azurerm_container_app_environment" "dev" {
  name                       = "dev"
  location                   = azurerm_resource_group.rg.location
  resource_group_name        = azurerm_resource_group.rg.name
  logs_destination           = "log-analytics"
  log_analytics_workspace_id = azurerm_log_analytics_workspace.law.id
}

# creating acr
resource "azurerm_container_registry" "acr" {
  name                = "crcinemadevpl1"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  sku                 = "Basic"
  admin_enabled       = true
}
