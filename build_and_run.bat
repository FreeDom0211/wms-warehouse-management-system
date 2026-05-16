@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "d:\毕设\demo"
"D:\毕设\apache-maven-3.8.6\bin\mvn.cmd" clean package -DskipTests -pl wms-web -am
pause