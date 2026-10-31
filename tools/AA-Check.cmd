@echo off
setlocal
rem CarHub / Android Auto check.
rem Put this file in the "platform-tools" folder (next to adb.exe) and double-click it.
rem It only READS information from the phone. It changes nothing.

cd /d "%~dp0"
if not exist adb.exe (
  echo adb.exe was not found next to this file.
  echo Move AA-Check.cmd into the platform-tools folder and run it again.
  pause
  exit /b 1
)

set REPORT=%~dp0aa-report.txt

echo Checking the phone connection...
adb start-server >nul 2>&1
adb get-state 1>nul 2>nul
if errorlevel 1 (
  echo.
  echo The phone is not connected or USB debugging was not allowed.
  echo  1. Turn on USB debugging in Developer options.
  echo  2. Connect the phone with a USB data cable.
  echo  3. Tap "Allow" on the phone's USB debugging prompt.
  echo Then run this file again.
  pause
  exit /b 1
)

adb logcat -c

echo.
echo ================================================================
echo  Now on the PHONE:
echo   1. Settings ^> Apps ^> Android Auto ^> Force stop
echo   2. Open Android Auto settings ^> Customize launcher
echo   3. Wait 10 seconds
echo ================================================================
echo.
echo Press any key here when you have done that...
pause >nul

echo Collecting...
> "%REPORT%" echo CarHub / Android Auto check
>> "%REPORT%" echo.
>> "%REPORT%" echo === Phone ===
adb shell getprop ro.product.model >> "%REPORT%"
adb shell getprop ro.build.version.release >> "%REPORT%"
>> "%REPORT%" echo.
>> "%REPORT%" echo === Android Auto version ===
adb shell dumpsys package com.google.android.projection.gearhead | findstr /i "versionName" >> "%REPORT%"
>> "%REPORT%" echo.
>> "%REPORT%" echo === CarHub install info ===
adb shell pm list packages -i com.personal.carhub >> "%REPORT%"
adb shell dumpsys package com.personal.carhub | findstr /i "versionName installerPackageName initiatingPackageName originatingPackageName" >> "%REPORT%"
>> "%REPORT%" echo.
>> "%REPORT%" echo === Media services Android can see (CarHub should be listed) ===
adb shell cmd package query-services -a android.media.browse.MediaBrowserService | findstr /i "packageName=" >> "%REPORT%"
>> "%REPORT%" echo.
>> "%REPORT%" echo === Android Auto decisions ===
adb logcat -d | findstr /i "VALIDATOR CAR.AUTH carhub isPackageAllowed DENIED" >> "%REPORT%"

echo.
echo Done. The report is saved as aa-report.txt and will open now.
echo Copy everything in it and paste it into the chat.
echo You can turn off USB debugging afterwards.
start "" notepad "%REPORT%"
pause
