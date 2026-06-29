@echo off
REM ============================================
REM Engraced Smile Dispatch - Build Script
REM ============================================
echo.
echo === Checking Environment ===

REM 1. Check Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java not found. Install JDK 17+ from:
    echo         https://adoptium.net/
    echo         Or install Android Studio (bundles Java).
    pause
    exit /b 1
)
echo [OK] Java found

REM 2. Check Android SDK path
if not defined ANDROID_HOME (
    if not defined ANDROID_SDK_ROOT (
        if not exist "%USERPROFILE%\AppData\Local\Android\Sdk" (
            echo [WARN] ANDROID_HOME not set. Checking local.properties...
        )
    )
)

REM 3. Check / create local.properties
if not exist local.properties (
    echo sdk.dir=%USERPROFILE%\AppData\Local\Android\Sdk > local.properties
    echo [INFO] Created local.properties
)

REM 4. Check / create debug keystore
if not exist debug.keystore (
    echo [INFO] Creating debug keystore...
    if exist "%USERPROFILE%\.android\debug.keystore" (
        copy "%USERPROFILE%\.android\debug.keystore" debug.keystore >nul
        echo [OK] Copied debug keystore from default location
    ) else (
        echo [INFO] Generating new debug keystore...
        keytool -genkey -v -keystore debug.keystore -storepass android ^
            -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 ^
            -validity 10000 -dname "CN=Android Debug,O=Android,C=US" >nul 2>&1
        if %ERRORLEVEL% EQU 0 (
            echo [OK] Debug keystore created
        ) else (
            echo [WARN] Could not create debug keystore. Build may fail.
        )
    )
)

echo.
echo === Building APK ===
echo.

call gradlew assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo [SUCCESS] APK built!
    echo Location: app\build\outputs\apk\debug\app-debug.apk
    echo ============================================
    echo.
    echo To install on your phone:
    echo   1. Enable USB Debugging in Developer Options
    echo   2. Connect phone via USB
    echo   3. Run: adb install app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo [ERROR] Build failed. Check the output above.
    echo.
    echo Common fixes:
    echo   - Open Android Studio once to let it download SDK components
    echo   - Run: .\gradlew assembleDebug --stacktrace
    echo.
)

pause
