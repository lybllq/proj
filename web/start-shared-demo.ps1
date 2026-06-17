param(
  [int]$BackendPort = 3001,
  [int]$WebPort = 8080,
  [switch]$NoBrowser,
  [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Write-Step {
  param([string]$Message)
  Write-Host "[shared-demo] $Message"
}

function Test-CommandExists {
  param([string]$Name)
  return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

function Test-LocalPortInUse {
  param([int]$Port)

  $client = [System.Net.Sockets.TcpClient]::new()
  try {
    $connect = $client.ConnectAsync("127.0.0.1", $Port)
    return $connect.Wait(250) -and $client.Connected
  } catch {
    return $false
  } finally {
    $client.Dispose()
  }
}

function Test-SharedBackendRunning {
  param([int]$Port)

  try {
    $response = Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:$Port/api/foods" -TimeoutSec 3
    return $response.StatusCode -eq 200
  } catch {
    return $false
  }
}

function Get-DesktopSiblingPath {
  param([string]$RelativePath)
  return Join-Path (Split-Path -Parent $PSScriptRoot) $RelativePath
}

function Start-StackProcess {
  param(
    [string]$Title,
    [string]$WorkingDirectory,
    [string]$Command
  )

  Write-Step "Starting $Title in $WorkingDirectory"

  if ($DryRun) {
    Write-Step "Dry run: powershell -NoExit -Command Set-Location '$WorkingDirectory'; $Command"
    return
  }

  Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-Command",
    "& { Set-Location -LiteralPath '$WorkingDirectory'; $Command }"
  )
}

function Enable-AdbReverse {
  param([int]$Port)

  if ($DryRun) {
    Write-Step "Dry run: skipping Android USB reverse setup."
    return
  }

  if (-not (Test-CommandExists "adb")) {
    Write-Step "adb is not available; skipping Android USB reverse setup."
    return
  }

  try {
    $deviceLines = adb devices | Select-Object -Skip 1
    foreach ($line in $deviceLines) {
      $parts = $line.Trim() -split "\s+"
      if ($parts.Count -ge 2 -and $parts[1] -eq "device") {
        $device = $parts[0]
        adb -s $device reverse "tcp:$Port" "tcp:$Port" | Out-Null
        Write-Step "Android device $device can reach the shared backend through adb reverse on port $Port."
      }
    }
  } catch {
    Write-Step "adb reverse setup skipped: $($_.Exception.Message)"
  }
}

$backendDir = Get-DesktopSiblingPath "kcsj\backend"
$webDir = $PSScriptRoot
$backendPackage = Join-Path $backendDir "package.json"
$backendServer = Join-Path $backendDir "server.js"
$webIndex = Join-Path $webDir "index.html"
$webUrl = "http://127.0.0.1:$WebPort"

Write-Step "Backend: $backendDir"
Write-Step "Web: $webDir"

if (-not (Test-Path $backendDir)) {
  throw "Cannot find backend directory: $backendDir"
}

if (-not (Test-Path $backendPackage) -or -not (Test-Path $backendServer)) {
  throw "Backend directory must contain package.json and server.js: $backendDir"
}

if (-not (Test-Path $webIndex)) {
  throw "Cannot find web entry file: $webIndex"
}

if (-not (Test-CommandExists "node")) {
  throw "Node.js is required to start kcsj/backend."
}

if (-not (Test-CommandExists "npm")) {
  throw "npm is required to start kcsj/backend."
}

if (-not (Test-CommandExists "python")) {
  throw "Python is required to serve the static kcskwy web files."
}

$backendNodeModules = Join-Path $backendDir "node_modules"
$backendCommand = if (Test-Path $backendNodeModules) {
  "`$env:PORT=$BackendPort; npm start"
} else {
  "`$env:PORT=$BackendPort; npm install; npm start"
}

$webCommand = "python -m http.server $WebPort --bind 127.0.0.1"

if (Test-LocalPortInUse $BackendPort) {
  if (Test-SharedBackendRunning $BackendPort) {
    Write-Step "Shared backend is already running on port $BackendPort."
  } else {
    throw "Port $BackendPort is in use, but it does not look like the shared Node backend. Stop that process and run this script again."
  }
} else {
  Start-StackProcess "shared Node backend on port $BackendPort" $backendDir $backendCommand
}

if (Test-LocalPortInUse $WebPort) {
  Write-Step "Port $WebPort is already in use; skipping web server start."
} else {
  Start-StackProcess "kcskwy web server on port $WebPort" $webDir $webCommand
}

Enable-AdbReverse $BackendPort

if (-not $DryRun -and -not $NoBrowser) {
  Start-Sleep -Seconds 2
  Start-Process $webUrl
}

Write-Step "Web URL: $webUrl"
Write-Step "API URL: http://127.0.0.1:$BackendPort/api"
Write-Step "Android emulator API host: http://10.0.2.2:$BackendPort"
Write-Step "The Android emulator app is configured to use the same shared backend automatically."
