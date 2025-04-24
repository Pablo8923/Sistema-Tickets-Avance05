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
        VBox layout = new VBox(10);
        Button ticketsButton = new Button("Ver Tickets");

        ticketsButton.setOnAction(e -> stage.setScene(new Scene(new TicketsView(stage).getView(), 600, 400)));

        layout.getChildren().addAll(new Label("Menú Principal"), ticketsButton);
        return layout;
    }
}