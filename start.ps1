# ============================================================
# SmartLibrary - Fast Start & Open Browser (No Rebuild)
# ============================================================

$TomcatDir = "C:\apache-tomcat-9.0.122"
$Port8080 = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue

if (-not $Port8080) {
    Write-Host "Starting Tomcat server..." -ForegroundColor Yellow
    Start-Process (Join-Path $TomcatDir "bin\startup.bat")
    
    # Wait briefly for startup
    $Retries = 10
    $Ready = $false
    while ($Retries -gt 0 -and -not $Ready) {
        Start-Sleep -Seconds 1
        try {
            $resp = Invoke-WebRequest -Uri "http://localhost:8080/SmartLibrary/" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($resp.StatusCode -eq 200) { $Ready = $true }
        } catch {}
        $Retries--
    }
}

Write-Host "Opening SmartLibrary in your browser..." -ForegroundColor Green
Start-Process "http://localhost:8080/SmartLibrary/"
