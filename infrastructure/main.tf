provider "azurerm" {
  version = "1.27.0"
}

locals {
  previewVaultName = "${var.product}-shared-aat"
  nonPreviewVaultName = "${var.product}-shared-${var.env}"
  vaultName = var.env == "preview" ? local.previewVaultName : local.nonPreviewVaultName
  vaultUri = data.azurerm_key_vault.ethos_key_vault.vault_uri
  previewRG = "${var.product}-aat"
  nonPreviewRG = "${var.product}-${var.env}"
  resourceGroup = var.env == "preview" ? local.previewRG : local.nonPreviewRG
  localEnv = var.env == "preview" ? "aat" : var.env
  s2sRG  = "rpe-service-auth-provider-${local.localEnv}"
}

data "azurerm_key_vault" "ethos_key_vault" {
  name                = local.vaultName
  resource_group_name = local.resourceGroup
}

data "azurerm_key_vault" "s2s_key_vault" {
  name                = "s2s-${local.localEnv}"
  resource_group_name = local.s2sRG
}

resource "azurerm_key_vault_secret" "AZURE_APPINSGHTS_KEY" {
  name         = "AppInsightsInstrumentationKey"
  value        = azurerm_application_insights.appinsights.instrumentation_key
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

resource "azurerm_application_insights" "appinsights" {
  name                = "${var.product}-${var.component}-appinsights-${var.env}"
  location            = var.appinsights_location
  resource_group_name = local.resourceGroup
  application_type    = "Web"

  tags = var.common_tags
}

data "azurerm_key_vault_secret" "microservicekey_ecm_consumer" {
  name = "microservicekey-ecm-consumer"
  key_vault_id = data.azurerm_key_vault.s2s_key_vault.id
}

resource "azurerm_key_vault_secret" "ecmConsumerS2sKey" {
  name         = "ecmConsumerS2sKey"
  value        = data.azurerm_key_vault_secret.microservicekey_ecm_consumer.value
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

resource "azurerm_key_vault_secret" "caseworkerUsername" {
  name         = "caseworkerUsername"
  value        = var.caseworker_user_name
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}

resource "azurerm_key_vault_secret" "caseworkerPassword" {
  name         = "caseworkerPassword"
  value        = var.caseworker_password
  key_vault_id = data.azurerm_key_vault.ethos_key_vault.id
}
