package system.pos.javafx.controller.settingsControllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.springframework.stereotype.Component;
import system.pos.javafx.controller.PaymentController;
import system.pos.javafx.stage.StageListener;
import system.pos.spring.enumm.Payment;
import system.pos.spring.enumm.Status;
import system.pos.spring.model.Employee;
import system.pos.spring.model.Order;
import system.pos.spring.service.OrderService;
import system.pos.spring.utility.MessagePrinter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class OrderHistoryController {
    private final OrderService orderService;
    private final PaymentController paymentController;
    private final StageListener stageListener;

    public OrderHistoryController(OrderService orderService, PaymentController paymentController, StageListener stageListener) {
        this.orderService = orderService;
        this.paymentController = paymentController;
        this.stageListener = stageListener;
    }

    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, Long> codeColumn;
    @FXML
    private TableColumn<Order, String> employeeColumn;
    @FXML
    private TableColumn<Order, Integer> numberColumn;
    @FXML
    private TableColumn<Order, Integer> tableColumn;
    @FXML
    private TableColumn<Order, Integer> priceColumn;
    @FXML
    private TableColumn<Order, String> statusColumn;
    @FXML
    private TableColumn<Order, String> paymentMethodColumn;
    @FXML
    private TableColumn<Order, LocalDateTime> dateColumn;
    @FXML
    private Label messageLabel;
    private Employee employee;

    @FXML
    public void initialize() {
        initTable();
        printOrderHistory();
    }

    public void initData(Employee employee) {
        this.employee = employee;
    }

    public void initTable() {
        orderTable.getColumns().forEach(column -> {column.setReorderable(false);column.setResizable(false);});
        orderTable.setFocusTraversable(false);

        codeColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getCode()).asObject());
        employeeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmployee().getName().toUpperCase()));
        numberColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getNumber_people()).asObject());
        tableColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTable_number()).asObject());
        priceColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPrice()).asObject());
        paymentMethodColumn.setCellValueFactory(cellData -> {
            Payment paymentMethod = cellData.getValue().getPayment_method();
            return new SimpleStringProperty((paymentMethod != null) ? paymentMethod.name().toUpperCase() : "");
        });
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name().toUpperCase()));
        statusColumn.setCellFactory(column -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(item.equalsIgnoreCase("НЕ_ПЛАТЕНА") ? Color.PURPLE : (item.equalsIgnoreCase("ОТКАЖАНА") ? Color.RED : Color.BLACK) );
                }
            }
        });
        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedOn()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.format(item));
            }
        });
    }

    public void payOrder() {
        TableView.TableViewSelectionModel<Order> selectionModel = orderTable.getSelectionModel();

        if(selectionModel.isEmpty()) {
            printMessage("Мора да селектирате нарачка!", false);
        } else {
            ObservableList<Integer> list = selectionModel.getSelectedIndices();
            Integer[] selectedIndices = new Integer[list.size()];
            selectedIndices = list.toArray(selectedIndices);

            Integer[] finalSelectedIndices = selectedIndices;
            for (Integer finalSelectedIndex : finalSelectedIndices) {
                selectionModel.clearSelection(finalSelectedIndex);
                Order order = orderTable.getItems().get(finalSelectedIndex);

                if(order == null) {
                    printMessage("Нарачката не е пронајдена!", false);
                } else {
                    if(order.getStatus().equals(Status.НЕ_ПЛАТЕНА)) {
                        paymentController.initData(this,order,employee);
                        stageListener.changeScene("/fxml/payment.fxml");
                    } else if(order.getStatus().equals(Status.ОТКАЖАНА)) {
                        printMessage("Нарачката е веќе откажана!", false);
                    } else if(order.getStatus().equals(Status.ОТВОРЕНА)) {
                        printMessage("Нарачката е во активна состојба!", false);
                    } else {
                        printMessage("Нарачката  е веќе платена!", false);
                    }
                }
            }
        }
    }

    public void printOrderHistory() {
        orderTable.getItems().clear();
        orderService.findAll().forEach(orderLog -> orderTable.getItems().add(orderLog));
    }

    public void printMessage(String message, Boolean color) {
        MessagePrinter.printMessage(messageLabel, message, color);
    }
}
