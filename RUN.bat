@echo off
chcp 65001 >nul
title TT Talep Destek Sistemi
echo ==========================================
echo   TT TALEP DESTEK SISTEMI BASLATILIYOR...
echo   Ilk calistirma 3-5 dk surebilir, bekle!
echo ==========================================
echo.

where java >nul 2>&1
if errorlevel 1 (
  echo [HATA] Java bulunamadi!
  echo adoptium.net adresinden Temurin JDK 21 kur, sonra tekrar dene.
  pause
  exit /b 1
)

echo 8080 portu kontrol ediliyor...
for /f "tokens=5" %%P in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
  echo Port 8080'i kullanan eski surec PID %%P kapatiliyor...
  taskkill /PID %%P /F >nul 2>&1
)
echo.

set "MAVEN_HOME=%~dp0tools\maven"
set "CWJAR=%MAVEN_HOME%\boot\plexus-classworlds-2.x.jar"

java -Dclassworlds.conf="%MAVEN_HOME%\bin\m2.conf" -Dmaven.home="%MAVEN_HOME%" -Dmaven.multiModuleProjectDirectory="%~dp0." -classpath "%CWJAR%" org.codehaus.plexus.classworlds.launcher.Launcher spring-boot:run -Dspring-boot.run.profiles=h2

pause