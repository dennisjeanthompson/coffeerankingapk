param(
    [string]$IconPath = ".\app_icon\icon.png"
)

if (!(Test-Path $IconPath)) {
    Write-Host "Error: Icon file not found at $IconPath. Place your square PNG as icon.png in the app_icon folder." -ForegroundColor Red
    exit 1
}

$projectRes = ".\app\src\main\res"
$drawablePath = Join-Path $projectRes "drawable"
$mipmapDirs = Get-ChildItem -Path $projectRes -Directory -Filter "mipmap-*"
$anydpiDir = Join-Path $projectRes "mipmap-anydpi-v26"
$colorsFile = Join-Path $projectRes "values\colors.xml"

New-Item -ItemType Directory -Path $drawablePath -Force | Out-Null
New-Item -ItemType Directory -Path $anydpiDir -Force | Out-Null

# copy icon to drawable (used by the adaptive foreground wrapper)
Copy-Item $IconPath (Join-Path $drawablePath "icon.png") -Force

# copy icon into mipmap folders for legacy launchers
if ($mipmapDirs -and $mipmapDirs.Count -gt 0) {
    foreach ($d in $mipmapDirs) {
        Copy-Item $IconPath (Join-Path $d.FullName "ic_launcher.png") -Force
        Copy-Item $IconPath (Join-Path $d.FullName "ic_launcher_round.png") -Force
    }
} else {
    $defaultMipmap = Join-Path $projectRes "mipmap-mdpi"
    New-Item -ItemType Directory -Path $defaultMipmap -Force | Out-Null
    Copy-Item $IconPath (Join-Path $defaultMipmap "ic_launcher.png") -Force
    Copy-Item $IconPath (Join-Path $defaultMipmap "ic_launcher_round.png") -Force
}

# ensure adaptive icon xml exists and points to ic_launcher_foreground (which uses the PNG)
$icLauncherXml = Join-Path $anydpiDir "ic_launcher.xml"
$icLauncherXmlContent = @"
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
  <background android:drawable="@color/ic_launcher_background"/>
  <foreground android:drawable="@drawable/icon"/>
</adaptive-icon>
"@
Set-Content -Path $icLauncherXml -Value $icLauncherXmlContent -Encoding UTF8

# ensure colors.xml has ic_launcher_background
if (!(Test-Path $colorsFile)) {
    New-Item -ItemType Directory -Path (Split-Path $colorsFile) -Force | Out-Null
    $colorsContent = @"
<?xml version="1.0" encoding="utf-8"?>
<resources>
  <color name="ic_launcher_background">#FFFFFF</color>
</resources>
"@
    Set-Content -Path $colorsFile -Value $colorsContent -Encoding UTF8
} else {
    $existing = Get-Content $colorsFile -Raw
    if ($existing -notmatch "ic_launcher_background") {
        $updated = $existing -replace "(</resources>)", "  <color name=`"ic_launcher_background`">#FFFFFF</color>`n`$1"
        Set-Content -Path $colorsFile -Value $updated -Encoding UTF8
    }
}

Write-Host "Icon files updated. Now run: bash ./gradlew assembleDebug" -ForegroundColor Green