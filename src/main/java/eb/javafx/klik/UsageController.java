package eb.javafx.klik;

import eb.javafx.klik.api.KlikClient;
import eb.javafx.klik.model.UserCounter;
import eb.javafx.klik.util.LoggingUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsageController {
    @FXML
    private TableView<UserCounter> usageTable;
    @FXML
    private TableColumn<UserCounter, String> userColumn;
    @FXML
    private TableColumn<UserCounter, Integer> clickColumn;
    @FXML
    private TableColumn<UserCounter, String> lastClickedColumn;
    @FXML
    private Button loadDataButton;
    @FXML
    private Label statusLabel;
    private LoadDataService service;
    private static final Logger logger = LoggingUtil.getLogger();


    @FXML
    public void initialize() {
        usageTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        clickColumn.setCellValueFactory(new PropertyValueFactory<>("clickCount"));
        lastClickedColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
        loadDataButton.setOnAction(event -> loadData());
        loadData();
    }

    public void loadData() {
        this.service = new LoadDataService();
        service.start();

        service.setOnRunning(e -> {
                    statusLabel.setText("Status: Loading...");
                    loadDataButton.setDisable(true);
                    loadDataButton.setText("Loading...");
                    loadDataButton.setCursor(Cursor.WAIT);
                }
        );
        service.setOnSucceeded(e -> {
            usageTable.setItems(service.getValue());
            statusLabel.setText("Status: Loaded");
            loadDataButton.setDisable(false);
            loadDataButton.setText("Refresh");
            loadDataButton.setCursor(Cursor.DEFAULT);
        });
        service.setOnFailed(e -> {
            statusLabel.setText("Status: Failed to load");
            logger.log(Level.SEVERE, "Failed to load UserCounter data", e);
            loadDataButton.setDisable(false);
            loadDataButton.setText("Refresh");
            loadDataButton.setCursor(Cursor.DEFAULT);
        });
    }

    private static class LoadDataService extends Service<ObservableList<UserCounter>> {
        @Override
        protected Task<ObservableList<UserCounter>> createTask() {
            return new Task<>() {
                @Override
                protected ObservableList<UserCounter> call() throws Exception {
                    List<UserCounter> result = new KlikClient().getAllCounters();
                    return FXCollections.observableArrayList(result);
                }
            };
        }
    }

    public void dispose() {
        if (service != null) {
            service.cancel();
        }
    }

}
