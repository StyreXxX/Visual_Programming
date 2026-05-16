package loginpage.loginpage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class LoginController {

    @FXML
    private void handleLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
            Main.primaryStage.setScene(new Scene(root, 600, 400));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Register.fxml"));
            Main.primaryStage.setScene(new Scene(root, 600, 400));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}