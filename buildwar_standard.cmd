set PATH=%JAVA_8_HOME%\bin;%PATH%
set JAVA_HOME=%JAVA_8_HOME%

mvnw.cmd clean install -s settings.xml -P remote -D skipTests
