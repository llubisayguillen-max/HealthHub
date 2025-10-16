package dll;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL = "jdbc:mysql://localhost:3306/sistema_turnos";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection conect;
    private static Conexion instance;

    private Conexion() {
        try {
            conect = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Se conectó correctamente a la base de datos.");
        } catch (SQLException e) {
            System.err.println("No se conectó: " + e.getMessage());
        }
    }

    public static Conexion getInstance() {
        if (instance == null) {
            instance = new Conexion();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (conect == null || conect.isClosed()) {
                // Reabrir si la conexión está cerrada
                conect = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Conexión reabierta a la base de datos.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo abrir conexión", e);
        }
        return conect;
    }
}
