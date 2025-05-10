import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://ep-noisy-snow-a4xirpb0-pooler.us-east-1.aws.neon.tech:5432/chinook?sslmode=require";
    private static final String USER = "neondb_owner"; // Usuario de tu base de datos en Neon.tech
    private static final String PASSWORD = "npg_pazkS4OjXuE0"; // Contraseña de tu base de datos en Neon.tech

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Método de prueba para verificar la conexión
    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("Conexión exitosa a la base de datos en Neon.tech");
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }
}