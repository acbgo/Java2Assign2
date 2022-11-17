import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try {
            new Client();
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getClassLoader().getResource("mainUI.fxml"));
            Pane root = fxmlLoader.load();
            stage.setTitle("Tic Tac Toe Server");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
    public static void main(String[] args) {
        launch(args);
    }
}
