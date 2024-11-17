package eb.javafx.klik;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private MainController mainController;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResourceAsStream("/eb/javafx/klik/main-view.fxml"));
        this.mainController = loader.getController();
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/eb/javafx/klik/styles.css").toExternalForm());
        stage.setTitle("KLiK");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> handleExit());
        stage.show();
    }

    private void handleExit() {
        this.mainController.dispose();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
