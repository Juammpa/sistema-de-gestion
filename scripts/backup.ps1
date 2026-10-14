#Requires -Version 5.1
<#
.SYNOPSIS
  Dumps the sistema_gestion and keycloak databases from the running postgres container.
.EXAMPLE
  .\scripts\backup.ps1
#>
[CmdletBinding()]
param(
    [string]$OutDir = 'backups',
    [int]$KeepLast = 14
)

$ErrorActionPreference = 'Stop'

# Run from the repo root, where compose.yaml lives.
Set-Location (Split-Path -Parent $PSScriptRoot)

$outPath = [System.IO.Path]::GetFullPath((Join-Path (Get-Location) $OutDir))
New-Item -ItemType Directory -Force -Path $outPath | Out-Null

$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'

foreach ($db in 'sistema_gestion', 'keycloak') {
    $tmp = "/tmp/$db-$stamp.dump"
    $dest = Join-Path $outPath "$db-$stamp.dump"

    # Dump inside the container (custom format) and check it can be read back.
    $cmd = 'pg_dump -U $POSTGRES_USER -Fc -f {0} {1} && pg_restore -l {0} > /dev/null' -f $tmp, $db
    docker compose exec -T postgres sh -c $cmd
    if ($LASTEXITCODE -ne 0) { throw "Backup of '$db' failed. Is the postgres container running?" }

    # Copy with docker (never redirect binary output through PowerShell).
    docker compose cp "postgres:$tmp" $dest
    if ($LASTEXITCODE -ne 0) { throw "Copying the '$db' dump to the host failed." }

    docker compose exec -T postgres rm -f $tmp | Out-Null

    $size = [math]::Round((Get-Item $dest).Length / 1KB)
    Write-Host "OK  $dest ($size KB)"

    # Retention: keep only the newest $KeepLast timestamped dumps of this database.
    Get-ChildItem $outPath -Filter "$db-*.dump" |
        Sort-Object LastWriteTime -Descending |
        Select-Object -Skip $KeepLast |
        Remove-Item -Verbose
}