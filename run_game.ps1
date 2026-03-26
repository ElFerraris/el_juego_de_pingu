# Script para compilar y ejecutar "El Juego de Pingu"

$JavaFXPath = "d:\Usuarios\toad0064\El juego del pingu\el_juego_de_pingu\javafx-sdk-21.0.10\lib"
$JDBCJar = "d:\Usuarios\toad0064\El juego del pingu\el_juego_de_pingu\lib\ojdbc8.jar"

Write-Host "Compilando el proyecto..." -ForegroundColor Cyan

# Crear carpeta bin si no existe
if (Test-Path bin) { Remove-Item "bin\*" -Recurse -Force } else { New-Item -ItemType Directory -Name bin }

# Compilar todos los archivos .java
javac --module-path "$JavaFXPath" --add-modules javafx.controls,javafx.fxml -cp "$JDBCJar" -d bin src/*.java src/controlador/*.java src/datos/*.java src/modelo/*.java src/util/*.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error en la compilación." -ForegroundColor Red
    exit $LASTEXITCODE
}

# Copiar recursos (en la raíz y subcarpetas)
if (!(Test-Path "bin\vista")) { New-Item -ItemType Directory -Path "bin\vista" }
Copy-Item "src\vista\*.fxml", "src\vista\*.css" "bin\vista\" -ErrorAction SilentlyContinue
Copy-Item -Path "imagenes", "fuentes" -Destination "bin\" -Recurse -ErrorAction SilentlyContinue

Write-Host "Ejecutando el juego..." -ForegroundColor Green

# Ejecutar Main
java --module-path "$JavaFXPath" --add-modules javafx.controls,javafx.fxml -cp "bin;$JDBCJar" Main
