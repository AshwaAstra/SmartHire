$ErrorActionPreference = 'Stop'
$secureKey = Read-Host 'Enter Gemini API key to save for your Windows user' -AsSecureString
if ($secureKey.Length -eq 0) {
    throw 'Gemini API key cannot be empty.'
}

$pointer = [IntPtr]::Zero
try {
    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureKey)
    $apiKey = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer).Trim()
    if ([string]::IsNullOrWhiteSpace($apiKey)) {
        throw 'Gemini API key cannot be empty.'
    }
    [Environment]::SetEnvironmentVariable('GEMINI_API_KEY', $apiKey, 'User')
    $env:GEMINI_API_KEY = $apiKey
    Write-Host 'Gemini API key saved for your Windows user. It was not printed or written to the project.'
} finally {
    if ($pointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    }
    $secureKey.Dispose()
}
