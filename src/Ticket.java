import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

public class Ticket {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty title;
    private final SimpleStringProperty description;
    private final SimpleStringProperty user;
    private final SimpleStringProperty status; // Nueva propiedad para el estado

    // Constructor actualizado
    public Ticket(int id, String title, String description, String user, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.user = new SimpleStringProperty(user);
        this.status = new SimpleStringProperty(status);
    }

    // Getters para las propiedades
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty userProperty() {
        return user;
    }

    public StringProperty statusProperty() {
        return status; // Getter para el estado
    }

    // Métodos para obtener los valores directamente
    public int getId() {
        return id.get();
    }

    public String getTitle() {
        return title.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getUser() {
        return user.get();
    }

    public String getStatus() {
        return status.get();
    }

    // Métodos para establecer valores
    public void setId(int id) {
        this.id.set(id);
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setUser(String user) {
        this.user.set(user);
    }

    public void setStatus(String status) {
        this.status.set(status); // Setter para el estado
    }
}