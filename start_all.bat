@echo off
chcp 65001 >nul
title JD WMS System - Startup

echo ================================================
echo          JD WMS System - One Click Start
echo ================================================
echo.

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "PROJECT_PATH=D:\毕设\demo"
set "JAR_FILE=%PROJECT_PATH%\wms-web\target\wms-web-1.0.0-SNAPSHOT.jar"
set "MVN_PATH=D:\毕设\apache-maven-3.8.6\bin\mvn.cmd"

echo [1/4] Checking Java Environment...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java environment not found!
    echo Please check JAVA_HOME: %JAVA_HOME