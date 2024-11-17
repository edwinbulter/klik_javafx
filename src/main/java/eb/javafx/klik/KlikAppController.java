package eb.javafx.klik;

import eb.javafx.klik.api.KlikClient;
import eb.javafx.klik.login.TokenManager;
import eb.javafx.klik.model.UserCount;
import eb.javafx.klik.util.LoggingUtil;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eb.javafx.klik.login.SignInController.SIGNED_IN_EVENT;

public class KlikAppController implements PropertyChangeListener {
    @FXML
    private Pane signedInPane;
    @FXML
    private Text greetingText;
    @FXML
    private Button klikButton;
    @FXML
    private Label klikCount;
    @FXML
    private Label totalCount;
    @FXML
    private Pane signedOutPane;

    private FetchUserCountService fetchUserCountService;
    private IncrementUserCounterService incrementUserCounterService;
    private static final Logger logger = LoggingUtil.getLogger();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (SIGNED_IN_EVENT.equals(evt.getPropertyName())) {
            boolean isSignedIn = (boolean) evt.getNewValue();
            Platform.runLater(() -> {
                System.out.println("KlikAppController.propertyChange");
                signedOutPane.setVisible(!isSignedIn);
                signedInPane.setVisible(isSignedIn);
                showGreeting();
                showUserCounters();
            });
        }
    }

    @FXML
    public void initialize() {
        showGreeting();
        showUserCounters();
        signedOutPane.setVisible(!TokenManager.isSignedIn());
        signedInPane.setVisible(TokenManager.isSignedIn());
    }

    private void showGreeting() {
        String userName = TokenManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            String prettyUserName = capitalizeFirstLetter(userName);
            greetingText.setText("Hello " + prettyUserName + "!");
        }
    }

    private void showUserCounters() {
        String userName = TokenManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            this.fetchUserCountService = new FetchUserCountService(userName);
            fetchUserCountService.setOnRunning(event -> {
                klikButton.setText("Fetching current counts...");
                klikButton.setDisable(true);
                klikButton.setCursor(Cursor.WAIT);
            });
            fetchUserCountService.setOnSucceeded(event -> {
                UserCount userCount = fetchUserCountService.getValue();
                klikCount.setText("" + userCount.user_counter());
                totalCount.setText("" + userCount.total_counter());
                klikButton.setText("KLiK");
                klikButton.setDisable(false);
                klikButton.setCursor(Cursor.DEFAULT);
            });
            fetchUserCountService.setOnFailed(event -> {
                Throwable exception = fetchUserCountService.getException();
                if (exception instanceof AwsServiceException awsServiceException) {
                    logger.log(Level.WARNING, awsServiceException.getMessage(), awsServiceException);
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Fetching Counters Failed");
                    alert.setHeaderText(awsServiceException.awsErrorDetails().errorMessage());
                    alert.showAndWait();
                } else {
                    logger.log(Level.SEVERE, exception.getMessage(), exception);
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Fetching Counters Failed");
                    alert.setHeaderText("Failed to Fetch Counters");
                    alert.setContentText(exception.getMessage());
                    alert.showAndWait();
                }
            });
            fetchUserCountService.start();
        }
    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() +
                input.substring(1).toLowerCase();
    }

    @FXML
    private void handleKlik() {
        String userName = TokenManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            this.incrementUserCounterService = new IncrementUserCounterService(userName);
            incrementUserCounterService.setOnRunning(event -> {
                klikButton.setText("Fetching current counts...");
                klikButton.setDisable(true);
                klikButton.setCursor(Cursor.WAIT);
            });
            incrementUserCounterService.setOnSucceeded(event -> {
                UserCount userCount = incrementUserCounterService.getValue();
                klikCount.setText("" + userCount.user_counter());
                totalCount.setText("" + userCount.total_counter());
                klikButton.setText("KLiK");
                klikButton.setDisable(false);
                klikButton.setCursor(Cursor.DEFAULT);
            });
            incrementUserCounterService.setOnFailed(event -> {
                Throwable exception = incrementUserCounterService.getException();
                if (exception instanceof AwsServiceException awsServiceException) {
                    logger.log(Level.WARNING, awsServiceException.getMessage(), awsServiceException);
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Increment UserCounter Failed");
                    alert.setHeaderText(awsServiceException.awsErrorDetails().errorMessage());
                    alert.showAndWait();
                } else {
                    logger.log(Level.SEVERE, exception.getMessage(), exception);
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Increment UserCounter Failed");
                    alert.setHeaderText("Failed to Increment UserCounter");
                    alert.setContentText(exception.getMessage());
                    alert.showAndWait();
                }
            });
            incrementUserCounterService.start();
        }
    }

    public void dispose() {
        if (fetchUserCountService != null) {
            fetchUserCountService.cancel();
        }
        if (incrementUserCounterService != null) {
            incrementUserCounterService.cancel();
        }
    }

    private static class FetchUserCountService extends Service<UserCount> {
        private final String userName;

        public FetchUserCountService(String userName) {
            this.userName = userName;
        }

        @Override
        protected Task<UserCount> createTask() {
            return new Task<>() {
                @Override
                protected UserCount call() throws Exception {
                    return new KlikClient().getCounter(userName);
                }
            };
        }
    }

    private static class IncrementUserCounterService extends Service<UserCount> {
        private final String userName;

        public IncrementUserCounterService(String userName) {
            this.userName = userName;
        }

        @Override
        protected Task<UserCount> createTask() {
            return new Task<>() {
                @Override
                protected UserCount call() throws Exception {
                    return new KlikClient().incrementCounter(userName);
                }
            };
        }
    }
}
