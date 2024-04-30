package system.pos.javafx.controller.settingsControllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.aspectj.weaver.ast.Or;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import system.pos.javafx.controller.PaymentController;
import system.pos.javafx.stage.PopUpStage;
import system.pos.javafx.stage.StageListener;
import system.pos.spring.enumm.Payment;
import system.pos.spring.enumm.Status;
import system.pos.spring.enumm.UserRole;
import system.pos.spring.exception.SettingsViewException;
import system.pos.spring.model.Employee;
import system.pos.spring.model.Order;
import system.pos.spring.service.OrderService;
import system.pos.spring.utility.MessagePrinter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
public class OrderHistoryController {
    private final OrderService orderService;
    private final PopUpStage popUpStage;
    private final OrderViewController orderViewController;
    private final PaymentController paymentController;
    private final StageListener stageListener;
    private final ApplicationContext applicationContext;

    public OrderHistoryController(OrderService orderService, PopUpStage popUpStage, OrderViewController orderViewController, PaymentController paymentController, StageListener stageListener, ApplicationContext applicationContext) {
        this.orderService = orderService;
        this.popUpStage = popUpStage;
        this.orderViewController = orderViewController;
        this.paymentController = paymentController;
        this.stageListener = stageListener;
        this.applicationContext = applicationContext;
    }

    @FXML
    private Pane pane;
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
    private TextField codeInput;
    @FXML
    private Label messageLabel;
    private Employee employee;
    private List<Order> orders;

    @FXML
    public void initialize() {
        initTable();
        printOrderHistory();
        pane.setOnKeyPressed(this::handleKeyPressed);
    }

    public void initData(Employee employee) {
        this.employee = employee;
    }

    public void initTable() {
        orderTable.getColumns().forEach(column -> {column.setReorderable(false);column.setResizable(false);});
        orderTable.setFocusTraversable(false);

        codeColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getCode()).asObject());
        employeeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmployee().getName()));
        numberColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getNumber_people()).asObject());
        tableColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTable_number()).asObject());
        priceColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPrice()).asObject());
        paymentMethodColumn.setCellValueFactory(cellData -> {
            Payment paymentMethod = cellData.getValue().getPayment_method();
            return new SimpleStringProperty((paymentMethod != null) ? paymentMethod.name().toUpperCase() : "");
        });
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name().toUpperCase()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(item.equalsIgnoreCase("НЕ_ПЛАТЕНА") ? Color.BLUE : (item.equalsIgnoreCase("ОТКАЖАНА") ? Color.RED : Color.BLACK) );
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

    private void handleKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            findOrderByCode();
        }
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

    public void filterOrders() {
        URL url = getClass().getResource("/fxml/orderHistoryFilter.fxml");
        popUpStage.initData(this);
        popUpStage.openPopUP(url);
    }

    public void openOrder() throws SettingsViewException {
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
                    orderViewController.initData(order);
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/orderView.fxml"));
                        loader.setControllerFactory(applicationContext::getBean);
                        Parent newContent = loader.load();
                        pane.getChildren().setAll(newContent);
                    } catch (Exception e) {
                        throw new SettingsViewException("Can't find the view to open.");
                    }
                }
            }
        }
    }

    public void findOrderByCode() {
        String codeText = codeInput.getText();
        if(!codeText.isBlank()) {
            long code;
            try {
                code = Long.parseLong(codeText);
            } catch (Exception e) {
                printMessage("Внеси цел број!",false);
                codeInput.clear();
                return;
            }
            Order order = orderService.findByCode(code);
            if(order == null) {
                printMessage("Не постои нарачка со таков код!",false);
                codeInput.clear();
            } else {
                update(Collections.singletonList(order));
            }
            codeInput.clear();
        }
    }

    public void printInExcel() throws FileNotFoundException {
        Iterable<Order> data = orders;

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        int rowNum = 0;
        for (Order entity : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entity.getCode());
            row.createCell(1).setCellValue(entity.getEmployee().getName());
            row.createCell(2).setCellValue(entity.getNumber_people());
            row.createCell(3).setCellValue(entity.getTable_number());
            row.createCell(4).setCellValue(entity.getPrice());
            row.createCell(5).setCellValue(entity.getStatus().toString());
            row.createCell(6).setCellValue(entity.getPayment_method() != null ? entity.getPayment_method().toString() : null);
            row.createCell(7).setCellValue(entity.getDiscount());
            String date = entity.getCreatedOn().format(DateTimeFormatter.ofPattern("HH:mm-dd/MM/yyyy"));
            row.createCell(8).setCellValue(date);
        }

        // Write workbook to file
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(DateTimeFormatter.ofPattern("ddMMyyyyHHmmss"));
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("нарачки" + formattedDateTime + ".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx"));
        fileChooser.setTitle("Save Excel File");

        // Show save dialog
        FileOutputStream fileOut = new FileOutputStream(fileChooser.showSaveDialog(null));
        try {
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            printMessage("Не успешна операција", false);
            throw new RuntimeException(e);
        }
    }

    public void update(List<Order> data) {
        orders = data;
        orderTable.getItems().clear();
        data.forEach(dt -> orderTable.getItems().add(dt));
    }

    public void printOrderHistory() {
        orders = orderService.findAll();
        orderTable.getItems().clear();
        orderService.findAll().forEach(orderLog -> orderTable.getItems().add(orderLog));
    }

    public void refreshTable() {
        printOrderHistory();
    }

    public void printMessage(String message, Boolean color) {
        MessagePrinter.printMessage(messageLabel, message, color);
    }
}
