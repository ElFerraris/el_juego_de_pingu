@echo off
set JAVAFX_PATH=lib
java --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.base -Djava.library.path=%JAVAFX_PATH% -cp bin aplicacion.Launcher
pause
