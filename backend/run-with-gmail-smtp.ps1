$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

$sender = (Read-Host 'Gmail address that will send interview invitations').Trim()
if ([string]::IsNullOrWhiteSpace($sender)) {
    throw 'A Gmail sender address is required.'
}

$securePassword = Read-Host 'Gmail App Password' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try {
    $env:MAIL_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer).Replace(' ', '')
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
}

$env:MAIL_ENABLED = 'true'
$env:MAIL_HOST = 'smtp.gmail.com'
$env:MAIL_PORT = '587'
$env:MAIL_USERNAME = $sender
$env:MAIL_FROM = $sender

$mavenExitCode = 0
try {
    & mvn spring-boot:run
    $mavenExitCode = $LASTEXITCODE
} finally {
    Remove-Item Env:MAIL_ENABLED, Env:MAIL_HOST, Env:MAIL_PORT, Env:MAIL_USERNAME, Env:MAIL_PASSWORD, Env:MAIL_FROM -ErrorAction SilentlyContinue
}

exit $mavenExitCode
