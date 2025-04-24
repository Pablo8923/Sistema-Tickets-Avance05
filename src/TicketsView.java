import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class TicketsView {
    private Stage stage;

    public TicketsView(Stage stage) {
        this.stage = stage;
    }

    public VBox getView() {
        VBox layout = new VBox(10);
        Label titleLabel = new Label("Lista de Tickets");
        TableView<Ticket> ticketTable = new TableView<>();
        Button addTicketButton = new Button("Agregar Ticket");
        Button updateTicketButton = new Button("Actualizar Ticket");
        Button deleteTicketButton = new Button("Eliminar Ticket");

        // Configurar columnas de la tabla
        TableColumn<Ticket, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> data.getValue().idProperty().asObject());

        TableColumn<Ticket, String> titleColumn = new TableColumn<>("Título");
        titleColumn.setCellValueFactory(data -> data.getValue().titleProperty());

        TableColumn<Ticket, String> descriptionColumn = new TableColumn<>("Descripción");
        descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());

        TableColumn<Ticket, String> userColumn = new TableColumn<>("Usuario");
        userColumn.setCellValueFactory(data -> data.getValue().userProperty());

        ticketTable.getColumns().addAll(idColumn, titleColumn, descriptionColumn, userColumn);

        // Cargar tickets desde la base de datos
        ObservableList<Ticket> tickets = loadTicketsFromDatabase();
        ticketTable.setItems(tickets);

        // Restricciones según el rol
        if (!LoginView.isAdmin()) {
            deleteTicketButton.setDisable(true); // Solo el administrador puede eliminar tickets
        }

        // Acción para agregar un nuevo ticket
        addTicketButton.setOnAction(e -> {
            TextInputDialog titleDialog = new TextInputDialog();
            titleDialog.setTitle("Nuevo Ticket");
            titleDialog.setHeaderText("Agregar un nuevo ticket");
            titleDialog.setContentText("Título del ticket:");

            titleDialog.showAndWait().ifPresent(title -> {
                TextInputDialog descriptionDialog = new TextInputDialog();
                descriptionDialog.setTitle("Nuevo Ticket");
                descriptionDialog.setHeaderText("Agregar un nuevo ticket");
                descriptionDialog.setContentText("Descripción del ticket:");

                descriptionDialog.showAndWait().ifPresent(description -> {
                    Ticket newTicket = new Ticket(0, title, description, ""); // Usuario se asignará en la base de datos
                    saveTicketToDatabase(newTicket, title, description, LoginView.getLoggedInUserId());
                    tickets.clear();
                    tickets.addAll(loadTicketsFromDatabase());
                });
            });
        });

        // Acción para actualizar un ticket
        updateTicketButton.setOnAction(e -> {
            Ticket selectedTicket = ticketTable.getSelectionModel().getSelectedItem();
            if (selectedTicket != null) {
                if (!LoginView.isAdmin() && !selectedTicket.getUser().equals(getLoggedInUserName())) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "No puedes editar tickets de otros usuarios.");
                    alert.show();
                    return;
                }

                TextInputDialog updateDialog = new TextInputDialog(selectedTicket.getDescription());
                updateDialog.setTitle("Actualizar Ticket");
                updateDialog.setHeaderText("Actualizar descripción del ticket");
                updateDialog.setContentText("Nueva descripción:");

                updateDialog.showAndWait().ifPresent(newDescription -> {
                    updateTicketInDatabase(selectedTicket.getId(), newDescription);
                    tickets.clear();
                    tickets.addAll(loadTicketsFromDatabase());
                });
            }
        });

        // Acción para eliminar un ticket
        deleteTicketButton.setOnAction(e -> {
            Ticket selectedTicket = ticketTable.getSelectionModel().getSelectedItem();
            if (selectedTicket != null) {
                deleteTicketFromDatabase(selectedTicket.getId());
                tickets.remove(selectedTicket);
            }
        });

        layout.getChildren().addAll(titleLabel, ticketTable, addTicketButton, updateTicketButton, deleteTicketButton);
        return layout;
    }

    private ObservableList<Ticket> loadTicketsFromDatabase() {
        ObservableList<Ticket> tickets = FXCollections.observableArrayList();
        String query = LoginView.isAdmin() ?
                "SELECT t.id, t.titulo, t.descripcion, u.nombre AS usuario FROM tickets t JOIN usuarios u ON t.usuario_id = u.id" :
                "SELECT t.id, t.titulo, t.descripcion, u.nombre AS usuario FROM tickets t JOIN usuarios u ON t.usuario_id = u.id WHERE t.usuario_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (!LoginView.isAdmin()) {
                statement.setInt(1, LoginView.getLoggedInUserId());
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("titulo");
                String description = resultSet.getString("descripcion");
                String userName = resultSet.getString("usuario");
                tickets.add(new Ticket(id, title, description, userName));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar los tickets: " + e.getMessage());
        }
        return tickets;
    }

    private void saveTicketToDatabase(Ticket ticket, String title, String description, int userId) {
        String query = "INSERT INTO tickets (titulo, descripcion, estado, usuario_id) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, title);
            statement.setString(2, description);
            statement.setString(3, "Abierto"); // Estado inicial
            statement.setInt(4, userId); // ID del usuario logueado
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al guardar el ticket: " + e.getMessage());
        }
    }

    private void updateTicketInDatabase(int ticketId, String newDescription) {
        String query = "UPDATE tickets SET descripcion = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, newDescription);
            statement.setInt(2, ticketId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar el ticket: " + e.getMessage());
        }
    }

    private void deleteTicketFromDatabase(int ticketId) {
        String query = "DELETE FROM tickets WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, ticketId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar el ticket: " + e.getMessage());
        }
    }

    private String getLoggedInUserName() {
        String query = "SELECT nombre FROM usuarios WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, LoginView.getLoggedInUserId());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nombre");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el nombre del usuario logueado: " + e.getMessage());
        }
        return "";
    }
}