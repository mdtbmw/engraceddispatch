@echo off
echo ===============================================
echo  ENGRACED DISPATCH - Admin Control Center Build
echo ===============================================
echo.

echo [1/3] Installing dependencies...
call npm install
if %ERRORLEVEL% neq 0 (
    echo FAILED: npm install failed.
    exit /b 1
)
echo Done.
echo.

echo [2/3] Building production bundle...
call npm run build
if %ERRORLEVEL% neq 0 (
    echo FAILED: Build failed.
    exit /b 1
)
echo Done.
echo.

echo [3/3] Starting production server on port 3000...
call npm start
