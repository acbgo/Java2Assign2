import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        new Server();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
