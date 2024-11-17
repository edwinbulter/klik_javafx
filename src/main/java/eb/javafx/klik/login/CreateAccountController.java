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
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eb.javafx.klik.login.SignInController.SIGNED_IN_EVENT;

public class CreateAccountController implements PropertyChangeListener {
    @FXML
    private Pane createPane;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField unmaskedPasswordField;
    @FXML
    private Button toggleButton;
    @FXML
    private TextField emailField;
    @FXML
    private Button createAccountButton;
    @FXML
    private Pane confirmPane;
    @FXML
    private TextField confirmationCodeField;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Pane signoutPane;

    private boolean isPasswordVisible = false;
    private CreateAccountService createAccountService;
    private ConfirmUserService confirmUserService;
    private DeleteUserService deleteUserService;
    private static final Logger logger = LoggingUtil.getLogger();

    @FXML
    public void initialize() {
        createPane.setVisible(!TokenManager.isSignedIn());
        signoutPane.setVisible(TokenManager.isSignedIn());
        SignInController.addPropertyChangeListener(this);
        emailField.setOnKeyReleased(event -> validateEmail());

    }

    private void validateEmail() {
        String email = emailField.getText();
        if (email.matches("^\\S+@\\S+\\.\\S+$")) {
            emailField.setStyle("-fx-border-color: green;");
        } else {
            emailField.setStyle("-fx-border-color: red;");
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (SIGNED_IN_EVENT.equals(evt.getPropertyName())) {
            boolean isSignedIn = (boolean) evt.getNewValue();
            Platform.runLater(() -> {
                createPane.setVisible(!isSignedIn);
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
    private void handleCreateAccount() {
        String username = usernameField.getText();
        String password = isPasswordVisible ? unmaskedPasswordField.getText() : passwordField.getText();
        String email = emailField.getText();
        this.createAccountService = new CreateAccountService(username, password, email);
        createAccountService.setOnRunning(e -> {
            createAccountButton.setText("Creating Account...");
            createAccountButton.setDisable(true);
            createAccountButton.setCursor(Cursor.WAIT);
        });
        createAccountService.setOnSucceeded(e -> {
            createAccountButton.setText("Create Account");
            createAccountButton.setDisable(false);
            createAccountButton.setCursor(Cursor.DEFAULT);
            boolean accountCreated = createAccountService.getValue();
            if (accountCreated) {
                createPane.setVisible(false);
                confirmPane.setVisible(true);
                logger.log(Level.INFO, "Account creation successful.");
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Account Creation failed");
                alert.setHeaderText("Failed to create account");
                alert.showAndWait();
                logger.log(Level.WARNING, "Account creation failed.");
            }
        });
        createAccountService.setOnFailed(e -> {
            createAccountButton.setText("Create Account");
            createAccountButton.setDisable(false);
            createAccountButton.setCursor(Cursor.DEFAULT);
            Throwable exception = createAccountService.getException();
            if (exception instanceof AwsServiceException awsException) {
                logger.log(Level.SEVERE, awsException.getMessage(), awsException);
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Account Creation failed");
                alert.setContentText(awsException.awsErrorDetails().errorMessage());
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
        createAccountService.start();
    }

    @FXML
    private void handleConfirm() {
        String username = usernameField.getText();
        String confirmationCode = confirmationCodeField.getText();
        this.confirmUserService = new ConfirmUserService(username, confirmationCode);
        confirmUserService.setOnRunning(e -> {
            confirmButton.setText("Confirming...");
            confirmButton.setDisable(true);
            confirmButton.setCursor(Cursor.WAIT);
        });
        confirmUserService.setOnSucceeded(e -> {
            confirmButton.setText("Confirm");
            confirmButton.setDisable(false);
            confirmButton.setCursor(Cursor.DEFAULT);
            boolean userConfirmed = confirmUserService.getValue();
            if (userConfirmed) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("User Confirmation successful");
                alert.setHeaderText("Your account is read to use.");
                alert.showAndWait();
                resetCreateAccount();
                logger.log(Level.INFO, "User Confirmation successful.");
            } else {
                confirmationCodeField.setText("");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("User Confirmation failed");
                alert.setHeaderText("Failed confirmation by user");
                alert.showAndWait();
                logger.log(Level.WARNING, "User Confirmation failed.");
            }
        });
        confirmUserService.setOnFailed(e -> {
            confirmButton.setText("Confirm");
            confirmButton.setDisable(false);
            confirmButton.setCursor(Cursor.DEFAULT);
            Throwable exception = confirmUserService.getException();
            if (exception instanceof AwsServiceException awsException) {
                confirmationCodeField.setText("");
                logger.log(Level.WARNING, awsException.getMessage(), awsException);
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("User Confirmation failed");
                alert.setContentText(awsException.awsErrorDetails().errorMessage());
                alert.showAndWait();
            } else {
                confirmationCodeField.setText("");
                logger.log(Level.SEVERE, exception.getMessage(), exception);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("User Confirmation failed");
                alert.setContentText(exception.getMessage());
                alert.showAndWait();
            }
        });
        confirmUserService.start();
    }

    @FXML
    private void handleCancel() {
        String username = usernameField.getText();
        this.deleteUserService = new DeleteUserService(username);
        deleteUserService.setOnRunning(e -> {
            cancelButton.setText("Deleting unconfirmed User...");
            cancelButton.setDisable(true);
            cancelButton.setCursor(Cursor.WAIT);
        });
        deleteUserService.setOnSucceeded(e -> {
            cancelButton.setText("Cancel");
            cancelButton.setDisable(false);
            cancelButton.setCursor(Cursor.DEFAULT);
            boolean userDeleted = deleteUserService.getValue();
            if (userDeleted) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Unconfirmed User Deleted successful");
                alert.setHeaderText("Unconfirmed User Deleted successful.");
                alert.showAndWait();
                logger.log(Level.INFO, "Unconfirmed User Deleted successful.");
            } else {
                confirmationCodeField.setText("");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Deleting Unconfirmed User failed");
                alert.setHeaderText("Failed to remove the unconfirmed user");
                alert.showAndWait();
                logger.log(Level.WARNING, "Deleting unconfirmed User failed.");
            }
            resetCreateAccount();
        });
        deleteUserService.setOnFailed(e -> {
            cancelButton.setText("Cancel");
            cancelButton.setDisable(false);
            cancelButton.setCursor(Cursor.DEFAULT);
            Throwable exception = deleteUserService.getException();
            if (exception instanceof AwsServiceException awsException) {
                confirmationCodeField.setText("");
                logger.log(Level.WARNING, awsException.getMessage(), awsException);
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Deleting unconfirmed User failed");
                alert.setHeaderText("Failed to remove the unconfirmed user");
                alert.setContentText(awsException.awsErrorDetails().errorMessage());
                alert.showAndWait();
            } else {
                confirmationCodeField.setText("");
                logger.log(Level.SEVERE, exception.getMessage(), exception);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Deleting unconfirmed User failed");
                alert.setHeaderText("Failed to remove the unconfirmed user");
                alert.setContentText(exception.getMessage());
                alert.showAndWait();
            }
            resetCreateAccount();
        });
        deleteUserService.start();
    }

    private void resetCreateAccount() {
        createPane.setVisible(true);
        confirmPane.setVisible(false);
        usernameField.setText("");
        passwordField.setText("");
        unmaskedPasswordField.setText("");
        confirmationCodeField.setText("");
        emailField.setText("");
    }

    private static class CreateAccountService extends Service<Boolean> {
        private final String username;
        private final String password;
        private final String email;

        public CreateAccountService(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return CognitoAuthenticationUtil.createAccount(username, password, email);
                }
            };
        }
    }

    private static class ConfirmUserService extends Service<Boolean> {
        private final String username;
        private final String confirmationCode;

        public ConfirmUserService(String username, String confirmationCode) {
            this.username = username;
            this.confirmationCode = confirmationCode;
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return CognitoAuthenticationUtil.confirmUser(username, confirmationCode);
                }
            };
        }
    }

    private static class DeleteUserService extends Service<Boolean> {
        private final String username;

        public DeleteUserService(String username) {
            this.username = username;
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() throws CognitoIdentityProviderException {
                    return CognitoAuthenticationUtil.deleteUnconfirmedUser(username);
                }
            };
        }
    }

    public void dispose() {
        SignInController.removePropertyChangeListener(this);
        if (createAccountService != null) {
            createAccountService.cancel();
        }
        if (confirmUserService != null) {
            confirmUserService.cancel();
        }
        if (deleteUserService != null) {
            deleteUserService.cancel();
        }
    }
}