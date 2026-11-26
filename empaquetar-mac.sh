#!/bin/sh
# ------------------------------------------------------------------
#  Genera la aplicacion lista para usar en macOS: dist/GestionFlota.app
#  Incluye su propio Java, asi que los usuarios no necesitan instalar nada.
#  Requisito para EMPAQUETAR (solo en esta computadora): JDK 21 instalado.
# ------------------------------------------------------------------
set -e
cd "$(dirname "$0")"

JPACKAGE=jpackage
if [ -n "$JAVA_HOME" ]; then JPACKAGE="$JAVA_HOME/bin/jpackage"; fi

echo "[1/3] Compilando y probando la aplicacion..."
./mvnw -q clean package

echo "[2/3] Preparando archivos..."
rm -rf dist/GestionFlota.app target/jpackage-input
mkdir -p target/jpackage-input
cp target/gestion-flota.jar target/jpackage-input/

echo "[3/3] Armando la aplicacion (puede tardar un minuto)..."
"$JPACKAGE" --type app-image --name GestionFlota \
  --input target/jpackage-input --main-jar gestion-flota.jar --main-class com.flota.App \
  --dest dist --app-version 1.0.0 --vendor "Gestion de Flota" \
  --java-options "-Dfile.encoding=UTF-8"

echo
echo "Listo. La aplicacion esta en: $(pwd)/dist/GestionFlota.app"
echo "Podes arrastrarla a la carpeta Aplicaciones."
