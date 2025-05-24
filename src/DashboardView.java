import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardView {
    private Stage stage;

    public DashboardView(Stage stage) {
        this.stage = stage;
    }

    public VBox getView() {
        VBox layout = new VBox(20); // Más espacio entre elementos
        layout.setAlignment(Pos.CENTER); // Centrar contenido

        Label titleLabel = new Label("Menú Principal");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button ticketsButton = new Button("Ver Tickets");
        ticketsButton.setPrefWidth(200);

        // Botón para cerrar sesión (opcional)
        Button logoutButton = new Button("Cerrar Sesión");
        logoutButton.setPrefWidth(200);
        logoutButton.setOnAction(e -> {
            stage.setScene(new Scene(new LoginView(stage).getView(), 400, 300));
        });

        ticketsButton.setOnAction(e -> 
            stage.setScene(new Scene(new TicketsView(stage).getView(), 600, 400))
        );

        layout.getChildren().addAll(titleLabel, ticketsButton, logoutButton);
        return layout;
    }
}