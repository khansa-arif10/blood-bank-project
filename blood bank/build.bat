@echo off
echo Compiling Blood Bank Management System...
echo.

REM Create bin directory if it doesn't exist
if not exist "bin" mkdir bin

REM Compile all Java files
javac -encoding UTF-8 -d bin -cp "libs/*" src/*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Compilation successful!
    echo ========================================
    echo.
    echo Run 'run.bat' to start the application
) else (
    echo.
    echo ========================================
    echo Compilation failed! Check errors above.
    echo ========================================
)

pause