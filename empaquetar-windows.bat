@echo off
REM ------------------------------------------------------------------
REM  Genera la aplicacion lista para usar en Windows:
REM    dist\GestionFlota\GestionFlota.exe
REM  Incluye su propio Java, asi que los usuarios no necesitan instalar nada.
REM  Requisito para EMPAQUETAR (solo en esta PC): JDK 21 instalado.
REM ------------------------------------------------------------------
setlocal
cd /d "%~dp0"

set "JPACKAGE=jpackage"
if defined JAVA_HOME set "JPACKAGE=%JAVA_HOME%\bin\jpackage"

echo.
echo [1/3] Compilando y probando la aplicacion...
call mvnw.cmd -q clean package
if errorlevel 1 goto error

echo [2/3] Preparando archivos...
if exist dist\GestionFlota rmdir /s /q dist\GestionFlota
if exist target\jpackage-input rmdir /s /q target\jpackage-input
mkdir target\jpackage-input
copy /y target\gestion-flota.jar target\jpackage-input\ >nul
if errorlevel 1 goto error

echo [3/3] Armando el ejecutable (puede tardar un minuto)...
"%JPACKAGE%" --type app-image --name GestionFlota ^
  --input target\jpackage-input --main-jar gestion-flota.jar --main-class com.flota.App ^
  --dest dist --app-version 1.0.0 --vendor "Gestion de Flota" ^
  --java-options "-Dfile.encoding=UTF-8"
if errorlevel 1 goto error

echo.
echo Listo. La aplicacion esta en: %cd%\dist\GestionFlota\GestionFlota.exe
echo Crea un acceso directo a ese archivo en el Escritorio de cada usuario.
echo Los datos se guardan aparte (carpeta GestionFlota del usuario), no se pierden al volver a empaquetar.
goto fin

:error
echo.
echo ERROR: no se pudo generar la aplicacion. Revisa los mensajes de arriba.
echo Verifica que este instalado el JDK 21 (comando: java -version).
exit /b 1

:fin
endlocal
