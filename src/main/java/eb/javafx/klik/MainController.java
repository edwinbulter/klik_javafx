package eb.javafx.klik;

import eb.javafx.klik.login.LoginController;
import eb.javafx.klik.login.SignInController;
import eb.javafx.klik.util.LoggingUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eb.javafx.klik.login.SignInController.SIGNED_IN_EVENT;

public class MainController implements PropertyChangeListener {
    @FXML
    private AnchorPane contentPane;
    @FXML
    private Button aboutButton;
    @FXML
    private Button designButton;
    @FXML
    private Button klikButton;
    @FXML
    private Button usageButton;
    @FXML
    private Button loginButton;

    private UsageController usageController;
    private LoginController loginController;
    private KlikAppController klikAppController;

    private static final Logger logger = LoggingUtil.getLogger();

    @FXML
    public void initialize() {
        showAbout();
        SignInController.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (SIGNED_IN_EVENT.equals(evt.getPropertyName())) {
            boolean isSignedIn = (boolean) evt.getNewValue();
            Platform.runLater(() -> loginButton.setText(isSignedIn ? "Logout" : "Login"));
        }
    }

    private void enableAllButtons() {
        aboutButton.setDisable(false);
        designButton.setDisable(false);
        klikButton.setDisable(false);
        usageButton.setDisable(false);
        loginButton.setDisable(false);

    }

    @FXML
    private void showAbout() {
        try {
            enableAllButtons();
            aboutButton.setDisable(true);
            loadPane(new FXMLLoader().load(getClass().getResourceAsStream("about.fxml")));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load about fxml", e);
        }
    }

    @FXML
    private void showDesign() {
        try {
            enableAllButtons();
            designButton.setDisable(true);
            loadPane(FXMLLoader.load(getClass().getResource("/eb/javafx/klik/design.fxml")));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load design fxml", e);
        }
    }

    @FXML
    private void showUsage() {
        try {
            enableAllButtons();
            usageButton.setDisable(true);
            FXMLLoader loader = new FXMLLoader();
            Pane usagePane = loader.load(getClass().getResourceAsStream("usage.fxml"));
            usageController = loader.getController();
            loadPane(usagePane);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load usage fxml", e);
        }
    }

    @FXML
    private void showLogin() {
        try {
            enableAllButtons();
            loginButton.setDisable(true);
            FXMLLoader loader = new FXMLLoader();
            Node loginPane = loader.load(getClass().getResource("/eb/javafx/klik/login/login.fxml"));
            loginController = loader.getController();
            loadPane(loginPane);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load usage fxml", e);
        }
    }

    @FXML
    private void showKlikApp() {
        try {
            enableAllButtons();
            klikButton.setDisable(true);
            FXMLLoader loader = new FXMLLoader();
            Node klikAppPane = loader.load(getClass().getResource("/eb/javafx/klik/klik-app.fxml"));
            klikAppController = loader.getController();
            loadPane(klikAppPane);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load usage fxml", e);
        }
    }

    private void loadPane(Node pane) {
        contentPane.getChildren().setAll(pane);
    }

    public void dispose() {
        if (usageController != null) {
            usageController.dispose();
        }
        if (loginController != null) {
            loginController.dispose();
        }
        if (klikAppController != null) {
            klikAppController.dispose();
        }
    }
}
