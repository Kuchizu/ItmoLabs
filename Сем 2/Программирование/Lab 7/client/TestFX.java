import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Авторизация");

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Введите имя пользователя");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль");

        Button loginButton = new Button("Войти");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            // Здесь вы можете проверить имя пользователя и пароль
            System.out.println("Имя пользователя: " + username + ", Пароль: " + password);
        });

        vbox.getChildren().addAll(
                new Label("Имя пользователя:"),
                usernameField,
                new Label("Пароль:"),
                passwordField,
                loginButton
        );

        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}