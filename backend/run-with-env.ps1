# Run Learnova backend with DB password from env (no secrets in repo)
# Usage: .\run-with-env.ps1
# Or set password once in this session: $env:SPRING_DATASOURCE_PASSWORD = "your-aiven-password"

if (-not $env:SPRING_DATASOURCE_PASSWORD) {
    Write-Host "Set SPRING_DATASOURCE_PASSWORD first, e.g.:" -ForegroundColor Yellow
    Write-Host '  $env:SPRING_DATASOURCE_PASSWORD = "your-aiven-password"' -ForegroundColor Cyan
    exit 1
}

if (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
}

# Optional: reduce memory if needed
# $env:MAVEN_OPTS = "-Xmx384m -Xms128m"

& "$PSScriptRoot\mvnw.cmd" spring-boot:run
