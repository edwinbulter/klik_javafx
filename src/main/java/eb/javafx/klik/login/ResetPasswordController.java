package eb.javafx.klik.login;

import eb.javafx.klik.util.LoggingUtil;
import javafx.application.Platform;
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eb.javafx.klik.login.SignInController.SIGNED_IN_EVENT;

public class ResetPasswordController implements PropertyChangeListener {
    @FXML
    private Pane requestCodePane;
    @FXML
    private TextField usernameField;

    @FXML
    private Pane resetPasswordPane;
    @FXML
    private TextField codeField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField unmaskedPasswordField;
    @FXML
    private Button toggleButton;
    @FXML
    private Button sendCodeButton;
    @FXML
    private Button submitPasswordButton;

    @FXML
    private Pane signoutPane;

    private boolean isPasswordVisible = false;
    private SendCodeService sendCodeService;
    private SubmitPasswordService submitPasswordService;
    private static final Logger logger = LoggingUtil.getLogger();


    @FXML
    public void initialize() {
        requestCodePane.setVisible(!TokenManager.isSignedIn());
        signoutPane.setVisible(TokenManager.isSignedIn());
        SignInController.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("ResetPasswordController.propertyChange");
        if (SIGNED_IN_EVENT.equals(evt.getPropertyName())) {
            boolean isSignedIn = (boolean) evt.getNewValue();
            Platform.runLater(() -> {
                requestCodePane.setVisible(!isSignedIn);
                signoutPane.setVisible(isSignedIn);
            });
        }
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
    private void handleSendCode() {
        System.out.println("ResetPasswordController.handleSendCode");
        String username = usernameField.getText();
        this.sendCodeService = new SendCodeService(username);
        sendCodeService.setOnRunning(event -> {
            sendCodeButton.setText("Sending code...");
            sendCodeButton.setDisable(true);
            sendCodeButton.setCursor(Cursor.WAIT);
        });
        sendCodeService.setOnSucceeded(event -> {
            sendCodeButton.setText("Send code");
            sendCodeButton.setDisable(false);
            sendCodeButton.setCursor(Cursor.DEFAULT);
            boolean codeSend = sendCodeService.getValue();
            if (codeSend) {
                requestCodePane.setVisible(false);
                resetPasswordPane.setVisible(true);
                logger.log(Level.INFO, "Code for resetting password send successful");
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Sending code failed");
                alert.setHeaderText("Failed to send code for resetting password");
                alert.showAndWait();
                logger.log(Level.WARNING, "Failed to send code for resetting password");
            }
        });
        sendCodeService.setOnFailed(event -> {
            sendCodeButton.setText("Send code");
            sendCodeButton.setDisable(false);
            sendCodeButton.setCursor(Cursor.DEFAULT);
            Throwable exception = sendCodeService.getException();
            if (exception instanceof AwsServiceException awsServiceException) {
                logger.log(Level.SEVERE, awsServiceException.getMessage(), awsServiceException);
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Sending code failed");
                alert.setContentText(awsServiceException.awsErrorDetails().errorMessage());
                alert.showAndWait();
            } else {
                logger.log(Level.SEVERE, exception.getMessage(), exception);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("An error occurred");
                alert.setContentText(exception.getMessage());
                alert.showAndWait();
            }
        });
        sendCodeService.start();
    }

    @FXML
    private void handleSubmitPassword() {
        String username = usernameField.getText();
        String confirmationCode = codeField.getText();
        String password = isPasswordVisible ? unmaskedPasswordField.getText() : passwordField.getText();
        this.submitPasswordService = new SubmitPasswordService(username, confirmationCode, password);
        submitPasswordService.setOnRunning(event -> {
            submitPasswordButton.setText("Submitting password...");
            submitPasswordButton.setDisable(true);
            submitPasswordButton.setCursor(Cursor.WAIT);
        });
        submitPasswordService.setOnSucceeded(event -> {
            submitPasswordButton.setText("Submit");
            submitPasswordButton.setDisable(false);
            submitPasswordButton.setCursor(Cursor.DEFAULT);

            boolean passwordSubmitted = submitPasswordService.getValue();
            if (passwordSubmitted) {
                logger.log(Level.INFO, "Password changed");
                codeField.setText("");
                usernameField.setText("");
                passwordField.setText("");
                requestCodePane.setVisible(true);
                resetPasswordPane.setVisible(false);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Password changed");
                alert.setHeaderText("You can sign in with the new password");
                alert.showAndWait();
            } else {
                logger.log(Level.WARNING, "Password change failed.");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Password change failed");
                alert.setHeaderText("Failed to change password");
                alert.showAndWait();
            }
        });
        submitPasswordService.setOnFailed(event -> {
            submitPasswordButton.setText("Submit");
            submitPasswordButton.setDisable(false);
            submitPasswordButton.setCursor(Cursor.DEFAULT);
            Throwable exception = submitPasswordService.getException();
            if (exception instanceof AwsServiceException awsServiceException) {
                logger.log(Level.WARNING, awsServiceException.getMessage(), awsServiceException);
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Password change failed");
                alert.setHeaderText(awsServiceException.awsErrorDetails().errorMessage());
                alert.showAndWait();
            } else {
                logger.log(Level.SEVERE, exception.getMessage(), exception);
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Password change failed");
                alert.setHeaderText("Failed to change password");
                alert.setContentText(exception.getMessage());
                alert.showAndWait();
            }
        });
        submitPasswordService.start();
    }

    @FXML
    private void handleCancel() {
        requestCodePane.setVisible(true);
        resetPasswordPane.setVisible(false);
        codeField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }

    private static class SendCodeService extends Service<Boolean> {
        private final String username;

        public SendCodeService(String username) {
            this.username = username;
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return CognitoAuthenticationUtil.initiatePasswordReset(username);
                }
            };
        }
    }

    private static class SubmitPasswordService extends Service<Boolean> {
        private final String username;
        private final String confirmationCode;
        private final String newPassword;

        public SubmitPasswordService(String username, String confirmationCode, String newPassword) {
            this.username = username;
            this.confirmationCode = confirmationCode;
            this.newPassword = newPassword;
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return CognitoAuthenticationUtil.confirmNewPassword(username, confirmationCode, newPassword);
                }
            };
        }
    }

    public void dispose() {
        SignInController.removePropertyChangeListener(this);
        if (sendCodeService != null) {
            sendCodeService.cancel();
        }
        if (submitPasswordService != null) {
            submitPasswordService.cancel();
        }
    }
}
