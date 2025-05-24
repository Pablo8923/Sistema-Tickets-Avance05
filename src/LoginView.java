import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoginView {
    private Stage stage;
    private static int loggedInUserId; // ID del usuario que inició sesión
    private static boolean isAdmin; // Indica si el usuario es administrador

    public LoginView(Stage stage) {
        this.stage = stage;
    }

    public VBox getView() {
        VBox layout = new VBox(10);
        Label titleLabel = new Label("Iniciar Sesión");
        ComboBox<String> userComboBox = new ComboBox<>();
        userComboBox.setPromptText("Seleccionar Usuario");
        List<String> users = loadUsersFromDatabase();
        userComboBox.getItems().addAll(users);

        Button loginButton = new Button("Iniciar Sesión");
        Button newUserButton = new Button("Nuevo Usuario");

        // Acción para iniciar sesión
        loginButton.setOnAction(e -> {
            String selectedUser = userComboBox.getValue();
            if (selectedUser == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Por favor, selecciona un usuario.");
                alert.show();
                return;
            }

            loggedInUserId = Integer.parseInt(selectedUser.split(" - ")[0]); // Extraer el ID del usuario
            isAdmin = checkIfAdmin(loggedInUserId); // Verificar si es administrador

            if (isAdmin) {
                // Pedir contraseña para el administrador
                TextInputDialog passwordDialog = new TextInputDialog();
                passwordDialog.setTitle("Contraseña de Administrador");
                passwordDialog.setHeaderText("Ingrese la contraseña de administrador:");
                passwordDialog.setContentText("Contraseña:");

                passwordDialog.showAndWait().ifPresent(password -> {
                    if (validateAdminPassword(password)) {
                        new Alert(Alert.AlertType.INFORMATION, "¡Bienvenido, administrador!").show();
                        stage.setScene(new Scene(new DashboardView(stage).getView(), 600, 400));
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta.");
                        alert.show();
                    }
                });
            } else {
                // Iniciar sesión como usuario normal
                new Alert(Alert.AlertType.INFORMATION, "¡Bienvenido! Has iniciado sesión correctamente.").show();
                stage.setScene(new Scene(new DashboardView(stage).getView(), 600, 400));
            }
        });

        // Acción para agregar un nuevo usuario
        newUserButton.setOnAction(e -> {
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Nuevo Usuario");
            nameDialog.setHeaderText("Agregar un nuevo usuario");
            nameDialog.setContentText("Nombre del usuario:");

            nameDialog.showAndWait().ifPresent(name -> {
                if (name.trim().isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "El nombre no puede estar vacío.").show();
                    return;
                }
                TextInputDialog emailDialog = new TextInputDialog();
                emailDialog.setTitle("Nuevo Usuario");
                emailDialog.setHeaderText("Agregar un nuevo usuario");
                emailDialog.setContentText("Correo del usuario:");

                emailDialog.showAndWait().ifPresent(email -> {
                    if (email.trim().isEmpty()) {
                        new Alert(Alert.AlertType.WARNING, "El correo no puede estar vacío.").show();
                        return;
                    }
                    addNewUserToDatabase(name, email);
                    userComboBox.getItems().clear();
                    userComboBox.getItems().addAll(loadUsersFromDatabase());
                    new Alert(Alert.AlertType.INFORMATION, "Usuario agregado correctamente.").show();
                });
            });
        });

        layout.getChildren().addAll(titleLabel, userComboBox, loginButton, newUserButton);
        return layout;
    }

    private List<String> loadUsersFromDatabase() {
        List<String> users = new ArrayList<>();
        String query = "SELECT id, nombre FROM usuarios";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("nombre");
                users.add(id + " - " + name); // Formato: "1 - Usuario"
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar los usuarios: " + e.getMessage());
        }
        return users;
    }

    private boolean checkIfAdmin(int userId) {
        String query = "SELECT es_admin FROM usuarios WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("es_admin");
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar si el usuario es administrador: " + e.getMessage());
        }
        return false;
    }

    private boolean validateAdminPassword(String password) {
        String query = "SELECT password FROM usuarios WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, loggedInUserId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                return storedPassword != null && storedPassword.equals(password);
            }
        } catch (SQLException e) {
            System.err.println("Error al validar la contraseña del administrador: " + e.getMessage());
        }
        return false;
    }

    private void addNewUserToDatabase(String name, String email) {
        String query = "INSERT INTO usuarios (nombre, correo, es_admin) VALUES (?, ?, FALSE)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al agregar un nuevo usuario: " + e.getMessage());
        }
    }

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }

    public static boolean isAdmin() {
        return isAdmin;
    }
}