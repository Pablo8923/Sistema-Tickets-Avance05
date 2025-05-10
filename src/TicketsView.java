import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class TicketsView {
    private Stage stage;

    // Instancias de estructuras dinámicas
    private Queue<Ticket> ticketQueue = new LinkedList<>(); // Cola para tickets pendientes
    private Stack<String> ticketHistory = new Stack<>();    // Pila para historial de cambios

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
        Button processTicketButton = new Button("Procesar Ticket"); // Botón para procesar tickets desde la cola
        Button undoChangeButton = new Button("Deshacer Cambio");   // Botón para deshacer cambios desde la pila
        Button changeStatusButton = new Button("Cambiar Estado");  // Botón para cambiar el estado del ticket
        Button backButton = new Button("Regresar al Menú de Ingreso"); // Botón para regresar al menú de ingreso

        // Configurar columnas de la tabla
        TableColumn<Ticket, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> data.getValue().idProperty().asObject());

        TableColumn<Ticket, String> titleColumn = new TableColumn<>("Título");
        titleColumn.setCellValueFactory(data -> data.getValue().titleProperty());

        TableColumn<Ticket, String> descriptionColumn = new TableColumn<>("Descripción");
        descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());

        TableColumn<Ticket, String> userColumn = new TableColumn<>("Usuario");
        userColumn.setCellValueFactory(data -> data.getValue().userProperty());

        TableColumn<Ticket, String> statusColumn = new TableColumn<>("Estado"); // Nueva columna para el estado
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        ticketTable.getColumns().addAll(idColumn, titleColumn, descriptionColumn, userColumn, statusColumn);

        // Cargar tickets desde la base de datos
        ObservableList<Ticket> tickets = loadTicketsFromDatabase();
        ticketTable.setItems(tickets);

        // Restricciones según el rol
        if (!LoginView.isAdmin()) {
            deleteTicketButton.setDisable(true); // Solo el administrador puede eliminar tickets
            processTicketButton.setDisable(true); // Solo el administrador puede procesar tickets
            undoChangeButton.setDisable(true);   // Solo el administrador puede deshacer cambios
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
                    saveTicketToDatabase(title, description, LoginView.getLoggedInUserId());
                    tickets.clear();
                    tickets.addAll(loadTicketsFromDatabase());

                    // Agregar el ticket a la cola
                    Ticket newTicket = new Ticket(0, title, description, "Usuario actual", "Abierto");
                    ticketQueue.add(newTicket);
                    System.out.println("Ticket agregado a la cola: " + title);
                });
            });
        });

        // Acción para procesar un ticket desde la cola
        processTicketButton.setOnAction(e -> {
            if (!ticketQueue.isEmpty()) {
                Ticket nextTicket = ticketQueue.poll(); // Sacar el primer ticket de la cola
                System.out.println("Procesando ticket: " + nextTicket.getTitle());
            } else {
                System.out.println("No hay tickets en la cola.");
            }
        });

// Acción para actualizar un ticket
updateTicketButton.setOnAction(e -> {
    Ticket selectedTicket = ticketTable.getSelectionModel().getSelectedItem();
    if (selectedTicket != null) {
        // Verificar si el ticket está cerrado
        if (selectedTicket.getStatus().equalsIgnoreCase("Cerrado")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No puedes editar un ticket que ya está cerrado.");
            alert.show();
            return;
        }

        // Verificar si el usuario actual es el propietario del ticket
        if (!LoginView.isAdmin() && !selectedTicket.getUser().equals(getLoggedInUserName())) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No puedes editar tickets de otros usuarios.");
            alert.show();
            return;
        }

        // Mostrar cuadro de diálogo para actualizar la descripción
        TextInputDialog updateDialog = new TextInputDialog(selectedTicket.getDescription());
        updateDialog.setTitle("Actualizar Ticket");
        updateDialog.setHeaderText("Actualizar descripción del ticket");
        updateDialog.setContentText("Nueva descripción:");

        updateDialog.showAndWait().ifPresent(newDescription -> {
            // Registrar el cambio en el historial
            ticketHistory.push("Descripción anterior: " + selectedTicket.getDescription());
            updateTicketInDatabase(selectedTicket.getId(), newDescription);
            tickets.clear();
            tickets.addAll(loadTicketsFromDatabase());
            System.out.println("Cambio registrado en el historial.");
        });
    } else {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Por favor, selecciona un ticket para actualizar.");
        alert.show();
    }
});

        // Acción para cambiar el estado del ticket
        changeStatusButton.setOnAction(e -> {
            Ticket selectedTicket = ticketTable.getSelectionModel().getSelectedItem();
            if (selectedTicket != null) {
                // Mostrar un cuadro de diálogo para seleccionar el nuevo estado
                ChoiceDialog<String> dialog = new ChoiceDialog<>("En Proceso", "Abierto", "En Proceso", "Cerrado");
                dialog.setTitle("Cambiar Estado");
                dialog.setHeaderText("Cambiar el estado del ticket");
                dialog.setContentText("Selecciona el nuevo estado:");

                dialog.showAndWait().ifPresent(newStatus -> {
                    // Actualizar el estado en la base de datos
                    updateTicketStatusInDatabase(selectedTicket.getId(), newStatus);

                    // Actualizar el estado en la tabla
                    selectedTicket.setStatus(newStatus);
                    ticketTable.refresh(); // Refrescar la tabla para mostrar el cambio
                    System.out.println("Estado del ticket actualizado a: " + newStatus);
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Por favor, selecciona un ticket para cambiar su estado.");
                alert.show();
            }
        });

        // Acción para deshacer el último cambio
        undoChangeButton.setOnAction(e -> {
            if (!ticketHistory.isEmpty()) {
                String lastChange = ticketHistory.pop(); // Sacar el último cambio de la pila
                System.out.println("Cambio deshecho: " + lastChange);
            } else {
                System.out.println("No hay cambios para deshacer.");
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

        // Acción para regresar al menú de ingreso
        backButton.setOnAction(e -> {
            stage.setScene(new Scene(new LoginView(stage).getView(), 400, 300));
        });

        layout.getChildren().addAll(titleLabel, ticketTable, addTicketButton, updateTicketButton, deleteTicketButton, processTicketButton, undoChangeButton, changeStatusButton, backButton);
        return layout;
    }

    private ObservableList<Ticket> loadTicketsFromDatabase() {
        ObservableList<Ticket> tickets = FXCollections.observableArrayList();
        String query = LoginView.isAdmin() ?
                "SELECT t.id, t.titulo, t.descripcion, u.nombre AS usuario, t.estado FROM tickets t JOIN usuarios u ON t.usuario_id = u.id" :
                "SELECT t.id, t.titulo, t.descripcion, u.nombre AS usuario, t.estado FROM tickets t JOIN usuarios u ON t.usuario_id = u.id WHERE t.usuario_id = ?";

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
                String status = resultSet.getString("estado"); // Cargar el estado del ticket
                tickets.add(new Ticket(id, title, description, userName, status));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar los tickets: " + e.getMessage());
        }
        return tickets;
    }

    private void saveTicketToDatabase(String title, String description, int userId) {
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

    private void updateTicketStatusInDatabase(int ticketId, String newStatus) {
        String query = "UPDATE tickets SET estado = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, newStatus);
            statement.setInt(2, ticketId);
            statement.executeUpdate();
            System.out.println("Estado del ticket actualizado en la base de datos.");
        } catch (SQLException e) {
            System.err.println("Error al actualizar el estado del ticket: " + e.getMessage());
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