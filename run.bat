@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo Java JDK 17 or newer is required. Add the JDK bin folder to PATH.
    exit /b 1
)

if not exist build mkdir build
javac --release 17 -encoding UTF-8 -d build src\*.java
if errorlevel 1 (
    echo Compilation failed. Fix the errors shown above and run again.
    exit /b 1
)

java -cp build Main
