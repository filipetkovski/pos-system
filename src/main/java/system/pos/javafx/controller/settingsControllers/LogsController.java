package system.pos.javafx.controller.settingsControllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
import system.pos.spring.model.Log;
import system.pos.spring.service.LogsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LogsController {
    private final LogsService logService;

    public LogsController(LogsService orderLogsService) {
        this.logService = orderLogsService;
    }

    @FXML
    private TableView<Log> logTable;
    @FXML
    private TableColumn<Log, Long> idColumn;
    @FXML
    private TableColumn<Log, String> employeeColumn;
    @FXML
    private TableColumn<Log, String> productColumn;
    @FXML
    private TableColumn<Log, Long> orderColumn;
    @FXML
    private TableColumn<Log, Integer> tableColumn;
    @FXML
    private TableColumn<Log, String> statusColumn;
    @FXML
    private TableColumn<Log, LocalDateTime> dateColumn;

    @FXML
    public void initialize() {
        initTable();
        printOrderLogs();
    }

    public void initTable() {
        logTable.getColumns().forEach(column -> {column.setReorderable(false);column.setResizable(false);});
        logTable.setFocusTraversable(false);

        idColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getId()).asObject());
        employeeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmployee().toUpperCase()));
        productColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().toUpperCase()));
        orderColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getOrder_code()).asObject());
        tableColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTable_number()).asObject());
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toUpperCase()));
        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreated_on()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.format(item));
            }
        });
    }

    public void printOrderLogs() {
        logTable.getItems().clear();
        logService.findAll().forEach(log -> logTable.getItems().add(log));
    }

}
