# SmartHire Backend Starter Script
Write-Host "=== SmartHire Backend Starter ===" -ForegroundColor Cyan

# Check MySQL
Write-Host "`n[1/2] Checking MySQL on port 3306..." -ForegroundColor Yellow
$mysql = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue
if (-not $mysql.TcpTestSucceeded) {
    Write-Host "ERROR: MySQL is NOT running on port 3306!" -ForegroundColor Red
    Write-Host "  -> Start MySQL from XAMPP/WAMP/MySQL Workbench or run:" -ForegroundColor Yellow
    Write-Host "     net start MySQL80" -ForegroundColor White
    Write-Host "`nPress any key to try to start MySQL80 service, or Ctrl+C to cancel..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    try { net start MySQL80 2>$null } catch {}
    try { net start MySQL 2>$null } catch {}
    Start-Sleep -Seconds 2
    $mysql = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue
    if (-not $mysql.TcpTestSucceeded) {
        Write-Host "MySQL still not reachable. Please start it manually and re-run this script." -ForegroundColor Red
        exit 1
    }
}
Write-Host "MySQL is running!" -ForegroundColor Green

# Start Spring Boot
Write-Host "`n[2/2] Starting Spring Boot backend..." -ForegroundColor Yellow
Write-Host "Backend will be available at: http://localhost:8081" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop.`n"

Set-Location $PSScriptRoot
mvn spring-boot:run
