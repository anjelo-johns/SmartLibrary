@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2"
set "CATALINA_HOME=C:\apache-tomcat-9.0.122"
set "CATALINA_BASE=C:\apache-tomcat-9.0.122"
echo Starting SmartLibrary on Tomcat...
echo JAVA_HOME=%JAVA_HOME%
echo CATALINA_HOME=%CATALINA_HOME%
call "%CATALINA_HOME%\bin\catalina.bat" run
pause
