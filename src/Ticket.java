import javafx.beans.property.*;

public class Ticket {
    private final IntegerProperty id;
    private final StringProperty title;
    private final StringProperty description;
    private final StringProperty user;

    public Ticket(int id, String title, String description, String user) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.user = new SimpleStringProperty(user);
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

    // Getters para los valores
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

    // Setters para los valores
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
}