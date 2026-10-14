@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2"
set "CATALINA_HOME=C:\apache-tomcat-9.0.122"
echo Stopping Apache Tomcat...
call "%CATALINA_HOME%\bin\shutdown.bat"
echo Tomcat stopped.
