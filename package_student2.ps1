$ProgressPreference = 'SilentlyContinue'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$pkgDir = "F:\Team Java\student2_handover_package"
if (Test-Path $pkgDir) { Remove-Item -Recurse -Force $pkgDir }
New-Item -ItemType Directory -Path $pkgDir | Out-Null

$filesToCopy = @(
    "QUICK_START_STUDENT2.txt",
    "smart_traffic_schema.sql",
    "seed_intersections.sql",
    "verify_database.sql",
    "setup_oci_database_host.sh",
    "setup_windows_database_host.bat",
    "test_remote_db.bat",
    "ARCHITECTURE_VERIFICATION_DOCUMENT.md",
    "STUDENT2_SUBMISSION_REPORT.md",
    "STUDENT2_HANDOVER_SPECIFICATION.md",
    "STUDENT2_PROMPT_AND_INSTRUCTIONS.md",
    "README.md",
    "emergency_gps.csv",
    ".env.example",
    "import_database.bat",
    "import_database.sh",
    "show_my_wifi_ip.bat",
    "connect_wifi_student2.bat",
    "pom.xml"
)

foreach ($f in $filesToCopy) {
    $srcPath = Join-Path "F:\Team Java" $f
    if (Test-Path $srcPath) {
        Copy-Item $srcPath -Destination $pkgDir
    }
}

Copy-Item "F:\Team Java\src" -Destination $pkgDir -Recurse

$zipPath = "F:\Team Java\student2_handover_package.zip"
if (Test-Path $zipPath) { Remove-Item -Force $zipPath }

[System.IO.Compression.ZipFile]::CreateFromDirectory($pkgDir, $zipPath, [System.IO.Compression.CompressionLevel]::Optimal, $false)

Write-Host "ZIP Created Successfully at $zipPath"
Get-Item $zipPath | Select-Object Name, Length, LastWriteTime
