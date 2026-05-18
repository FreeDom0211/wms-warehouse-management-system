@echo off
chcp 65001 >nul

echo.
echo ================================================
echo          京东仓储管理系统 - 一键启动
echo ================================================
echo.

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot"
set "MAVEN_HOME=D:\毕设\apache-maven-3.8.6"
set "PROJECT_PATH=D:\毕设\demo"
set "JAR_FILE=%PROJECT_PATH%\wms-web\target\wms-web-1.0.0-SNAPSHOT.jar"

echo [1/4] 检查 Java 环境...
set "PATH=%JAVA_HOME%\bin;%PATH%"
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java 环境未找到
    echo 请确认 JAVA_HOME 路径正确: %JAVA_HOME%
    pause
    exit /b 1
)
echo OK - Java 环境正常

echo.
echo [2/4] 启动 Redis 服务...
tasklist /FI "IMAGENAME eq redis-server.exe" 2>NUL | find /I "redis-server.exe" >NUL
if %errorlevel% equ 0 (
    echo OK - Redis 已在运行
    goto :check_redis_end
)

echo 正在查找 Redis...
if exist "redis-server.exe" (
    start "Redis" /min redis-server.exe
    goto :wait_redis
)
if exist "D:\毕设\Redis\redis-server.exe" (
    start "Redis" /min "D:\毕设\Redis\redis-server.exe"
    goto :wait_redis
)
if exist "C:\Program Files\Redis\redis-server.exe" (
    start "Redis" /min "C:\Program Files\Redis\redis-server.exe"
    goto :wait_redis
)
if exist "C:\Program Files (x86)\Redis\redis-server.exe" (
    start "Redis" /min "C:\Program Files (x86)\Redis\redis-server.exe"
    goto :wait_redis
)

echo ERROR: Redis 启动失败
echo 请手动启动 Redis 后再运行此脚本
pause
exit /b 1

:wait_redis
echo 正在启动 Redis...
timeout /t 3 /nobreak >nul
tasklist /FI "IMAGENAME eq redis-server.exe" 2>NUL | find /I "redis-server.exe" >NUL
if %errorlevel% equ 0 (
    echo OK - Redis 启动成功
) else (
    echo WARNING: Redis 可能未成功启动，请检查
)

:check_redis_end

echo.
echo [3/4] 检查项目构建...
if not exist "%JAR_FILE%" (
    echo JAR 文件不存在，开始构建项目...
    if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
        echo ERROR: Maven 未找到: %MAVEN_HOME%
        pause
        exit /b 1
    )
    
    cd /d "%PROJECT_PATH%"
    "%MAVEN_HOME%\bin\mvn.cmd" clean package -DskipTests -pl wms-web -am
    
    if %errorlevel% neq 0 (
        echo ERROR: 项目构建失败
        pause
        exit /b 1
    )
    echo OK - 项目构建成功
) else (
    echo OK - JAR 文件已存在，跳过构建
)

echo.
echo [4/4] 启动 WMS 应用...
echo 启动端口: 8080
echo 访问地址: http://localhost:8080
echo.
echo 启动中，请稍候...
echo.

cd /d "%PROJECT_PATH%"
java -jar "%JAR_FILE%"

pause