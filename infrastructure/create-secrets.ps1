$vaultName = "kv-cinema-pl-dev"

Get-Content .env-dev | ForEach-Object {
    if ($_ -and -not $_.StartsWith("#")) {
        $name, $value = $_ -split '=', 2
        az keyvault secret set `
        --vault-name $vaultName `
        --name $name.Trim() `
        --value $value.Trim()
    }
}
