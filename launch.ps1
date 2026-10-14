# ============================================================
#  SmartLibrary - ALL-IN-ONE AUTOMATED LAUNCHER
#  One command runs everything simultaneously:
#  1) Copies URL to clipboard
#  2) Opens project folder in Windows Explorer
#  3) Deploys WAR & Starts Tomcat in its own window
#  4) Opens browser automatically
# ============================================================

$JAVA_HOME     = "C:\Program Files\Java\jdk-26.0.2"
$CATALINA_HOME = "C:\apache-tomcat-9.0.122"
$PROJECT_DIR   = $PSScriptRoot
$APP_URL       = "http://localhost:8080/SmartLibrary/"

$env:JAVA_HOME     = $JAVA_HOME
$env:CATALINA_HOME = $CATALINA_HOME

Clear-Host
Write-Host ""
Write-Host "  ============================================" -ForegroundColor Cyan
Write-Host "         SmartLibrary - All-In-One Launch     " -ForegroundColor Cyan
Write-Host "  ============================================" -ForegroundColor Cyan
Write-Host ""

# 1. Copy URL to clipboard immediately
Write-Host "  [+] Copying localhost URL to clipboard..." -ForegroundColor Yellow
try {
    Set-Clipboard -Value $APP_URL
    Write-Host "  [OK] URL '$APP_URL' copied to clipboard!" -ForegroundColor Green
} catch {
    Write-Host "  [!] Could not access clipboard." -ForegroundColor DarkGray
}

# 2. Open project folder in Windows Explorer immediately
Write-Host "  [+] Opening project folder in Explorer..." -ForegroundColor Yellow
Start-Process explorer.exe $PROJECT_DIR

# 3. Check / build package if needed
$warFile = Join-Path $PROJECT_DIR "target\SmartLibrary.war"
if (-not (Test-Path $warFile)) {
    Write-Host "  [+] Packaging project with Maven..." -ForegroundColor Yellow
    & mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  [ERR] Maven build failed!" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
} else {
    Write-Host "  [OK] Found WAR package." -ForegroundColor Green
}

# 4. Deploy WAR to Tomcat webapps
Write-Host "  [+] Deploying WAR to Tomcat webapps..." -ForegroundColor Yellow
$warDest = Join-Path $CATALINA_HOME "webapps\SmartLibrary.war"
Copy-Item -Path $warFile -Destination $warDest -Force
Write-Host "  [OK] Deployed successfully!" -ForegroundColor Green

# 5. Check if Tomcat is already running on port 8080
$port8080 = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if (-not $port8080) {
    Write-Host "  [+] Starting Apache Tomcat in dedicated window..." -ForegroundColor Yellow
    Start-Process -FilePath "cmd.exe" `
        -ArgumentList "/k", "title SmartLibrary Tomcat Server && set JAVA_HOME=C:\Program Files\Java\jdk-26.0.2&& set CATALINA_HOME=C:\apache-tomcat-9.0.122&& ""C:\apache-tomcat-9.0.122\bin\catalina.bat"" run" `
        -WorkingDirectory "C:\apache-tomcat-9.0.122" `
        -WindowStyle Normal

    # Wait for Tomcat port 8080
    Write-Host "  [+] Waiting for Tomcat to respond..." -ForegroundColor Yellow
    $ready = $false
    $retries = 20
    while ($retries -gt 0 -and -not $ready) {
        Start-Sleep -Seconds 1
        $conn = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
        if ($conn) { $ready = $true }
        $retries--
    }
} else {
    Write-Host "  [OK] Tomcat is already running on port 8080." -ForegroundColor Green
}

# 6. Open browser
Write-Host "  [+] Opening browser at $APP_URL ..." -ForegroundColor Green
Start-Process $APP_URL

Write-Host ""
Write-Host "  ============================================" -ForegroundColor Green
Write-Host "   SUCCESS! All tasks completed!              " -ForegroundColor Green
Write-Host "   URL: $APP_URL                              " -ForegroundColor Cyan
Write-Host "  ============================================" -ForegroundColor Green
Write-Host ""
