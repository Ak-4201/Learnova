# Start Learnova API on http://localhost:8081 with in-memory H2 (no Aiven required).
# Run from PowerShell: .\start-local.ps1
#
# If port 8081 is already in use (old Spring Boot), we stop that listener so this run succeeds.

$port = 8081
$pids = @(Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique)
foreach ($p in $pids) {
    if ($p -and $p -ne 0) {
        Write-Host "Stopping process $p (was using port $port)..." -ForegroundColor Yellow
        Stop-Process -Id $p -Force -ErrorAction SilentlyContinue
    }
}
Start-Sleep -Seconds 1

$jdk = "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
if (-not (Test-Path $jdk)) {
    $jdk = $env:JAVA_HOME
}
if (-not $jdk -or -not (Test-Path $jdk)) {
    Write-Host "Set JAVA_HOME to your JDK 17+ folder, or install Eclipse Temurin 21." -ForegroundColor Red
    exit 1
}
$env:JAVA_HOME = $jdk
Set-Location $PSScriptRoot
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
