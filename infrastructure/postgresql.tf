
resource "azurerm_postgresql_flexible_server" "postgresql" {
  administrator_login               = "psqladmin"
  administrator_password            = var.db_password
  auto_grow_enabled                 = false
  backup_retention_days             = 7
  geo_redundant_backup_enabled      = false
  location                          = "polandcentral"
  name                              = "psql-cinema-dev-pl-test"
  public_network_access_enabled     = true
  resource_group_name               = "rg-cinema-dev-pl"
  sku_name                          = "B_Standard_B1ms"
  storage_mb                        = 32768
  storage_tier                      = "P4"
  tags                              = {}
  version                           = "18"
  zone                              = "3"
  authentication {
    active_directory_auth_enabled = true
    password_auth_enabled         = true
    tenant_id                     = "86760356-0022-486f-b793-a2d470bba5a5"
  }
}

resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_my_ip" {
  name             = "allow-developer-ip"
  server_id        = azurerm_postgresql_flexible_server.postgresql.id
  start_ip_address = var.my_ip
  end_ip_address   = var.my_ip
}

resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_azure_services" {
  name             = "allow-azure-services"
  server_id        = azurerm_postgresql_flexible_server.postgresql.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
  depends_on = [azurerm_postgresql_flexible_server_firewall_rule.allow_my_ip]
}

resource "azurerm_postgresql_flexible_server_database" "catalog_service" {
  name      = "catalog_service"
  server_id = azurerm_postgresql_flexible_server.postgresql.id
  charset   = "UTF8"
  collation = "en_US.utf8"
  depends_on = [azurerm_postgresql_flexible_server_firewall_rule.allow_azure_services]
}

resource "azurerm_postgresql_flexible_server_database" "booking_service" {
  name      = "booking_service"
  server_id = azurerm_postgresql_flexible_server.postgresql.id
  charset   = "UTF8"
  collation = "en_US.utf8"
  depends_on = [azurerm_postgresql_flexible_server_database.catalog_service]
}

resource "azurerm_postgresql_flexible_server_database" "payment_service" {
  name      = "payment_service"
  server_id = azurerm_postgresql_flexible_server.postgresql.id
  charset   = "UTF8"
  collation = "en_US.utf8"
  depends_on = [azurerm_postgresql_flexible_server_database.booking_service]
}