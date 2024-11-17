package eb.javafx.klik.login;

import eb.javafx.klik.util.LoggingUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignInController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField unmaskedPasswordField;
    @FXML
    private Button toggleButton;
    @FXML
    private Pane signInPane;
    @FXML
    private Pane signOutPane;
    @FXML
    private Button signInButton;
    @FXML
    private Button signOutButton;
    private boolean isPasswordVisible = false;
    private SignInService signInService;
    private static final Logger logger = LoggingUtil.getLogger();
    private static final PropertyChangeSupport support = new PropertyChangeSupport(SignInController.class);
    public static final String SIGNED_IN_EVENT = "signedIn";

    @FXML
    private void initialize() {
        signInPane.setVisible(!TokenManager.isSignedIn());
        signOutPane.setVisible(TokenManager.isSignedIn());
    }

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @FXML
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            unmaskedPasswordField.setVisible(false);
            passwordField.setVisible(true);
            unmaskedPasswordField.setText(passwordField.getText());
            toggleButton.setText("Show");
            isPasswordVisible = false;
        } else {
            unmaskedPasswordField.setVisible(true);
            passwordField.setVisible(false);
            unmaskedPasswordField.setText(passwordField.getText());
            toggleButton.setText("Hide");
            isPasswordVisible = true;
        }
    }

    @FXML
    private void handleSignIn() {
        String username = usernameField.getText();
        String password = isPasswordVisible ? unmaskedPasswordField.getText() : passwordField.getText();
        this.signInService = new SignInService(username, password);
        signInService.setOnRunning(e -> {
            signInButton.setText("Signing In...");
            signInButton.setDisable(true);
            signOutButton.setCursor(Cursor.WAIT);
        });
        signInService.setOnSucceeded(e -> {
            signInButton.setText("Sign In");
            signInButton.setDisable(false);
            signOutButton.setCursor(Cursor.DEFAULT);
            boolean signedIn = signInService.getValue();
            if (signedIn) {
                signInPane.setVisible(false);
                signOutPane.setVisible(true);
                support.firePropertyChange(SIGNED_IN_EVENT, false, true);
                logger.log(Level.INFO, "Sign-in successful.");
            } else {
                logger.log(Level.WARNING, "Sign-in failed.");
            }
        });
        signInService.setOnFailed(e -> {
            signInButton.setText("Sign In");
            signInButton.setDisable(false);
            signOutButton.setCursor(Cursor.DEFAULT);
            Throwable exception = signInService.getException();
            if (exception instanceof AwsServiceException awsException) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Login failed");
                alert.setHeaderText(awsException.getMessage());
                alert.setContentText(awsException.awsErrorDetails().errorMessage());
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("An error occurred");
                alert.setContentText(exception.getMessage());
                alert.showAndWait();
            }
        });
        signInService.start();
    }

    private static class SignInService extends Service<Boolean> {
        private final String username;
        private final String password;

        public SignInService(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return CognitoAuthenticationUtil.login(username, password);
                }
            };
        }
    }

    @FXML
    private void handleSignOut() {
        support.firePropertyChange(SIGNED_IN_EVENT, true, false);
        TokenManager.clearTokens();
        usernameField.setText("");
        passwordField.setText("");
        unmaskedPasswordField.setText("");
        signInPane.setVisible(true);
        signOutPane.setVisible(false);
    }

    public void dispose() {
        if (signInService != null) {
            signInService.cancel();
        }
    }
}
