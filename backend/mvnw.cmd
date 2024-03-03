@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.
@REM Apache Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET ___MVNW_SCRIPT_NAME__=%__MVNW_ARG0_NAME__%

@SET MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
@IF "%MAVEN_PROJECTBASEDIR%"=="" (SET MAVEN_PROJECTBASEDIR=%~dp0)
@IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" (SET MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%)

@SET WRAPPERJAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
@SET WRAPPERPROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties

@IF DEFINED JAVA_HOME (
    @SET JAVACMD=%JAVA_HOME%\bin\java.exe
) ELSE (
    @SET JAVACMD=java.exe
)

@IF NOT EXIST "%WRAPPERJAR%" (
    FOR /F "tokens=2 delims==" %%a IN ('findstr /i wrapperUrl "%WRAPPERPROPERTIES%"') DO SET WRAPPER_URL=%%a
    curl -fsSL -o "%WRAPPERJAR%" "%WRAPPER_URL%"
)

"%JAVACMD%" -classpath "%WRAPPERJAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
