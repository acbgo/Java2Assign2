import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Login implements Initializable {
    private static final Logger logger = Logger.getLogger(Login.class.getName());
    @FXML
    private TextField login_username;

    @FXML
    private TextField login_password;

    @FXML
    private Button login_button;

    @FXML
    private Button register_button;

    public void loginButtonClick() {
        logger.log(Level.INFO, "input username" + login_username.getText());
        logger.log(Level.INFO, "input password" + login_password.getText());
        if("nikolazhang".equalsIgnoreCase(login_username.getText())
                && "123654".equalsIgnoreCase(login_password.getText())) {
            logger.log(Level.INFO, "登录成功！");
        } else {
            logger.log(Level.WARNING, "Incorrect user name or password!");
        }
    }

    public void registerButtonClick(){
        Platform.runLater(() -> {
            Stage primaryStage = (Stage) register_button.getScene().getWindow();
            primaryStage.hide();
            try {
                new MainPage().start(primaryStage);
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
