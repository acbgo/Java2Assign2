import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class Login implements Initializable {
    Statement statement;
    Connection con;
    {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tic_tac_toe?characterEncoding=UTF8&autoReconnect=true&useSSL=false");
            statement = con.createStatement();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @FXML
    private TextField login_username;

    @FXML
    private TextField login_password;

    @FXML
    private Button login_button;

    @FXML
    private Button register_button;

    public String user_name;
    public String password;

    public void loginButtonClick() throws SQLException {
        user_name = login_username.getText();
        password = login_password.getText();
        String sql = "select * from users where name = '" + user_name + "';";
        ResultSet resultSet = statement.executeQuery(sql);
        resultSet.last();
        int len = resultSet.getRow();
        if (len == 0){
            System.out.println("user does not exist");
        } else {
            resultSet.beforeFirst();
            while (resultSet.next()){
                String tmp_pass = resultSet.getString("password");
                if (password.equals(tmp_pass)){
                    System.out.println("Log in successfully");
                    goto_main();
                } else {
                    System.out.println("Incorrect user name or password!");
                }
            }
        }

    }

    public void registerButtonClick() throws SQLException {
        user_name = login_username.getText();
        password = login_password.getText();
        String sql = "select * from users where name = '" + user_name + "';";
        ResultSet resultSet = statement.executeQuery(sql);
        resultSet.last();
        int len = resultSet.getRow();
        if (len != 0){
            System.out.println("The user name already exists");
        } else {
            sql = "insert into users value ('" + user_name + "', '" + password + "');";
            statement.execute(sql);
            login_username.setText("");
            login_password.setText("");
            sql = "insert into record value ('" + user_name +  "', 0, 0, 0, 0);";
            statement.execute(sql);
        }

    }

    public void goto_main(){
        Platform.runLater(() -> {
            Stage primaryStage = (Stage) register_button.getScene().getWindow();
            primaryStage.hide();
            try {
                MainPage page = new MainPage();
                page.setName(user_name);
                page.start(primaryStage);
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
