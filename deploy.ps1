# ============================================================
# SmartLibrary - One-Click Build, Deploy & Launch Script
# ============================================================

$ErrorActionPreference = "Stop"

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "   SmartLibrary - Build, Deploy & Launch Web App   " -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# 1. Locate Tomcat 9
$TomcatDir = "C:\apache-tomcat-9.0.122"
if (-not (Test-Path $TomcatDir)) {
    Write-Host "Error: Tomcat 9 not found at $TomcatDir" -ForegroundColor Red
    exit 1
}

# 2. Build WAR with Maven
Write-Host "`n[1/4] Building SmartLibrary WAR with Maven..." -ForegroundColor Yellow
Set-Location -Path $PSScriptRoot
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "`nBuild failed! Please check the Maven output above." -ForegroundColor Red
    exit $LASTEXITCODE
}

# 3. Copy WAR to Tomcat webapps
Write-Host "`n[2/4] Deploying SmartLibrary.war to Tomcat webapps..." -ForegroundColor Yellow
$WarSource = Join-Path $PSScriptRoot "target\SmartLibrary.war"
$WarDest = Join-Path $TomcatDir "webapps\SmartLibrary.war"
Copy-Item -Path $WarSource -Destination $WarDest -Force
Write-Host "WAR deployed successfully." -ForegroundColor Green

# 4. Check if Tomcat is running on port 8080
Write-Host "`n[3/4] Verifying Tomcat 9 server status..." -ForegroundColor Yellow
$Port8080 = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue

if (-not $Port8080) {
    Write-Host "Starting Apache Tomcat 9..." -ForegroundColor Yellow
    $StartupBat = Join-Path $TomcatDir "bin\startup.bat"
    Start-Process -FilePath $StartupBat

    Write-Host "Waiting for server initialization..." -ForegroundColor Yellow
    $Retries = 15
    $Ready = $false
    while ($Retries -gt 0 -and -not $Ready) {
        Start-Sleep -Seconds 2
        try {
            $resp = Invoke-WebRequest -Uri "http://localhost:8080/SmartLibrary/" -UseBasicParsing -TimeoutSec 3 -ErrorAction SilentlyContinue
            if ($resp.StatusCode -eq 200) {
                $Ready = $true
            }
        } catch {
            # continue waiting
        }
        $Retries--
    }
} else {
    Write-Host "Tomcat is already running on port 8080." -ForegroundColor Green
    Start-Sleep -Seconds 2
}

# 5. Launch in default browser
Write-Host "`n[4/4] Opening SmartLibrary in your default browser..." -ForegroundColor Cyan
Start-Process "http://localhost:8080/SmartLibrary/"

Write-Host "`n=================================================" -ForegroundColor Green
Write-Host "  Success! Application is live at:               " -ForegroundColor Green
Write-Host "  http://localhost:8080/SmartLibrary/            " -ForegroundColor Yellow
Write-Host "=================================================" -ForegroundColor Green
