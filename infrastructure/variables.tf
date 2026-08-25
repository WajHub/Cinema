variable "kafka_server" {
  type    = string
  default = "141.144.247.230:9094"
}

variable "schema_registry_url" {
  type    = string
  default = "http://141.144.247.230:8081"
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "acr_server" {
  type    = string
  default = "crcinemadevpl01.azurecr.io"
}

variable "acr_username" {
  type    = string
  default = "crcinemadevpl01"
}

variable "acr_password" {
  type      = string
  sensitive = true
}

variable "my_ip" {
  type      = string
  sensitive = true
}
