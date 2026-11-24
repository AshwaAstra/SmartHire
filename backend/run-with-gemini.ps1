$ErrorActionPreference = 'Stop'

$apiKey = [Environment]::GetEnvironmentVariable('GEMINI_API_KEY', 'Process')
if ([string]::IsNullOrWhiteSpace($apiKey)) {
    $apiKey = [Environment]::GetEnvironmentVariable('GEMINI_API_KEY', 'User')
}

$secureKey = $null
$pointer = [IntPtr]::Zero
if ([string]::IsNullOrWhiteSpace($apiKey)) {
    $secureKey = Read-Host 'Enter Gemini API key (or run save-gemini-key.ps1 once)' -AsSecureString
    if ($secureKey.Length -eq 0) {
        throw 'Gemini API key cannot be empty.'
    }
    try {
        $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureKey)
        $apiKey = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer).Trim()
    } finally {
        if ($pointer -ne [IntPtr]::Zero) {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
        }
        $secureKey.Dispose()
    }
}

if ([string]::IsNullOrWhiteSpace($apiKey)) {
    throw 'Gemini API key cannot be empty.'
}

$env:GEMINI_API_KEY = $apiKey.Trim()
Push-Location $PSScriptRoot
try {
    & mvn spring-boot:run
    if ($LASTEXITCODE -ne 0) {
        throw "Maven exited with code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}
