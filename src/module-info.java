module el_juego_de_pingu {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires transitive java.sql; 
    requires com.oracle.database.jdbc; // <--- DESCOMENTA ESTA LÍNEA (ya no debería dar error)

    opens aplicacion to javafx.graphics, javafx.fxml;
    opens vista to javafx.fxml;
    opens controlador to javafx.fxml;

    exports aplicacion;
    exports vista;
    exports controlador;
    exports modelo;
    exports datos;
}
