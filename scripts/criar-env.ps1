$ErrorActionPreference = "Stop"
$raiz = Split-Path $PSScriptRoot -Parent
$envFile = Join-Path $raiz ".env"
function Segredo([int]$n) { -join ((48..57) + (65..90) + (97..122) | Get-Random -Count $n | ForEach-Object { [char]$_ }) }

if (-not (Test-Path $envFile)) {
  # a senha sempre leva maiúscula, minúscula e número (exigência do SQL Server)
  $linhas = (Get-Content (Join-Path $raiz ".env.example")) `
    -replace "^DB_PASSWORD=.*", "DB_PASSWORD=Pr$(Segredo 30)9" `
    -replace "^JWT_SECRET=.*", "JWT_SECRET=$(Segredo 60)"
  [IO.File]::WriteAllLines($envFile, $linhas)
  Write-Host "Arquivo .env criado com senha do banco e chave do JWT aleatorias."
}

# mantém o perfil do Compose igual ao banco escolhido
$conteudo = Get-Content $envFile
$banco = ($conteudo | Select-String -Pattern '^DB_VENDOR=(.*)').Matches.Groups[1].Value
if (-not $banco) { $banco = "postgresql" }
$conteudo = $conteudo -replace "^COMPOSE_PROFILES=.*", "COMPOSE_PROFILES=$banco"
[IO.File]::WriteAllLines($envFile, $conteudo)
Write-Host "Banco escolhido: $banco"
