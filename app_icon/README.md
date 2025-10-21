# App Icon Updater

This folder is for updating the Android app launcher icon.

## Instructions

1. Paste your square PNG image here and rename it to `icon.png` (recommended size: 512x512 or larger).
2. Run the PowerShell script from the project root:
   ```
   .\app_icon\update_icon.ps1
   ```
3. Commit and push the changes:
   ```
   git add -A
   git commit -m "Update app launcher icon"
   git push origin super
   ```
4. Rebuild the APK:
   ```
   bash ./gradlew assembleDebug
   ```

The icon will appear on your device/emulator after installing the new APK.

## Notes

- This updates both adaptive and legacy launcher icons.
- No app functionality is affected - only the visual icon changes.
- If you want to revert, restore the original files from git.