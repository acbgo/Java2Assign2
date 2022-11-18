import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getClassLoader().getResource("login.fxml"));
            stage.setTitle("Login page");
            Pane root = fxmlLoader.load();
//            Controller controller = fxmlLoader.getController();
//            Client client = new Client(controller);
//            stage.setTitle("Tic Tac Toe Server" + client.name);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void goto_main(){

    }

    public static void main(String[] args) {
        launch(args);
    }
}
