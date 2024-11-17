package eb.javafx.klik.login;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static eb.javafx.klik.login.SignInController.SIGNED_IN_EVENT;

public class LoginController implements PropertyChangeListener {

    @FXML
    private SignInController signInController;
    @FXML
    private CreateAccountController createAccountController;
    @FXML
    private ResetPasswordController resetPasswordController;
    @FXML
    private Tab signInTab;

    @FXML
    private void initialize() {
        SignInController.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("LoginController.propertyChange");
        if (SIGNED_IN_EVENT.equals(evt.getPropertyName())) {
            boolean isSignedIn = (boolean) evt.getNewValue();
            Platform.runLater(() -> signInTab.setText(isSignedIn ? "Sign Out" : "Sign In"));
        }
    }

    public void dispose() {
        SignInController.removePropertyChangeListener(this);
        signInController.dispose();
        createAccountController.dispose();
        resetPasswordController.dispose();
    }
}
