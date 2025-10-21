# PowerShell script to update the app launcher icon
# Place your icon.png in this folder, then run this script from the project root

param(
    [string]$IconPath = ".\app_icon\icon.png"
)

# Check if icon exists
if (!(Test-Path $IconPath)) {
    Write-Host "Error: Icon file not found at $IconPath. Please place your icon.png in the app_icon folder."
    exit 1
}

# Copy to drawable (for adaptive icon foreground)
Copy-Item $IconPath .\app\src\main\res\drawable\ic_launcher_foreground.png -Force

# Copy to all mipmap folders for legacy support
Get-ChildItem .\app\src\main\res\mipmap-* | ForEach-Object {
    Copy-Item $IconPath "$($_)\ic_launcher.png" -Force
    Copy-Item $IconPath "$($_)\ic_launcher_round.png" -Force
}

Write-Host "Icon updated successfully. Run 'bash ./gradlew assembleDebug' to rebuild the APK."