@echo off
echo Starting Blood Bank Management System...
echo.

REM Check if compiled
if not exist "bin\CodeMain.class" (
    echo ERROR: Application not compiled!
    echo Please run 'build.bat' first.
    pause
    exit /b 1
)

REM Run the application
java -cp "bin;libs/*" CodeMain

pause