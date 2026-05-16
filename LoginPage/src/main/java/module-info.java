module loginpage.loginpage {
    requires javafx.controls;
    requires javafx.fxml;


    opens loginpage.loginpage to javafx.fxml;
    exports loginpage.loginpage;
}