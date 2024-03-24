package system.pos.javafx.controller.settingsControllers;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Component;
import system.pos.spring.model.AuthLog;
import system.pos.spring.service.AuthLogsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class AuthLogsController {
    private final AuthLogsService authLogsService;

    public AuthLogsController(AuthLogsService authLogsService) {
        this.authLogsService = authLogsService;
    }

    @FXML
    private TableView<AuthLog> shiftTable;
    @FXML
    private TableColumn<AuthLog, Long> idColumn;
    @FXML
    private TableColumn<AuthLog, String> employeeColumn;
    @FXML
    private TableColumn<AuthLog, String> statusColumn;
    @FXML
    private TableColumn<AuthLog, LocalDateTime> dateColumn;

    @FXML
    public void initialize() {
        initTable();
        printOrderLogs();
    }

    public void initTable() {
        shiftTable.getColumns().forEach(column -> {column.setReorderable(false);column.setResizable(false);});
        shiftTable.setFocusTraversable(false);

        idColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getId()).asObject());
        employeeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmployee().toUpperCase()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toUpperCase()));
        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreated_on()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
    }

    public void printOrderLogs() {
        shiftTable.getItems().clear();
        authLogsService.findAll().forEach(log -> shiftTable.getItems().add(log));
    }
}
