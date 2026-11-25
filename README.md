# Gestion de Flota

Aplicacion de escritorio para cargar camiones, camioneros y sus documentos, con aviso de vencimientos al iniciar.

## Stack
- Java 21
- Swing + [FlatLaf](https://www.formdev.com/flatlaf/) (look and feel moderno)
- SQLite (base local embebida, archivo `flota.db`)
- Maven

## Funcionalidades
- [ ] ABM de camiones
- [ ] ABM de camioneros
- [ ] Carga de documentos (VTV, seguro, licencia, psicofisico, etc.) con fecha de vencimiento y archivo adjunto
- [x] Aviso al abrir la app de documentos vencidos o proximos a vencer (30 dias por defecto)
- [ ] Avisos por WhatsApp (extension futura via `Notificador`)

## Ejecutar
```bash
mvn compile exec:java
```

## Generar jar ejecutable
```bash
mvn package
java -jar target/gestion-flota-0.1.0-SNAPSHOT.jar
```

## Estructura
```
com.flota
├── model          Camion, Camionero, Documento
├── repository     Acceso a SQLite
├── service        Logica de alertas de vencimiento
├── notification   Canales de aviso (pantalla, WhatsApp)
└── ui             Ventanas Swing
```
