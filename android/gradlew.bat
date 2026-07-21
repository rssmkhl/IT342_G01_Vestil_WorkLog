@echo off
setlocal

set "APP_HOME=%~dp0"
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "GRADLE_VERSION=8.4"
set "GRADLE_DIST_NAME=gradle-%GRADLE_VERSION%-bin"
set "GRADLE_DIST_URL=https://services.gradle.org/distributions/%GRADLE_DIST_NAME%.zip"
set "GRADLE_CACHE_DIR=%USERPROFILE%\.gradle\wrapper\dists\%GRADLE_DIST_NAME%"
set "GRADLE_HOME=%GRADLE_CACHE_DIR%\gradle-%GRADLE_VERSION%"
set "GRADLE_BIN=%GRADLE_HOME%\bin\gradle.bat"

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo Android Studio JBR was not found at "%JAVA_HOME%".
  exit /b 1
)

if not exist "%GRADLE_BIN%" (
  echo Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference='Stop';" ^
    "$zipPath=Join-Path $env:TEMP '%GRADLE_DIST_NAME%.zip';" ^
    "$distDir='%GRADLE_CACHE_DIR%';" ^
    "New-Item -ItemType Directory -Force -Path $distDir | Out-Null;" ^
    "Invoke-WebRequest -Uri '%GRADLE_DIST_URL%' -OutFile $zipPath;" ^
    "Expand-Archive -Path $zipPath -DestinationPath $distDir -Force;"
  if errorlevel 1 exit /b 1
)

call "%GRADLE_BIN%" %*
exit /b %errorlevel%
