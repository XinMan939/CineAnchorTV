@echo off
setlocal
set DIR=%~dp0
if defined GRADLE_HOME (
  "%GRADLE_HOME%\bin\gradle.bat" %*
) else (
  echo GRADLE_HOME not set. set GRADLE_HOME to your gradle install dir.
  exit /b 1
)
