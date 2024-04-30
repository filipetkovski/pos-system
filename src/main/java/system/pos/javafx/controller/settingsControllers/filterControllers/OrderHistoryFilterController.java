package system.pos.javafx.controller.settingsControllers.filterControllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import system.pos.javafx.controller.settingsControllers.OrderHistoryController;
import system.pos.spring.enumm.Payment;
import system.pos.spring.enumm.Status;
import system.pos.spring.model.Employee;
import system.pos.spring.model.Order;
import system.pos.spring.model.Tables;
import system.pos.spring.service.EmployeeService;
import system.pos.spring.service.TableService;
import system.pos.spring.service.pipeServices.pipeImpl.PipeOrderHistoryServiceImpl;
import system.pos.spring.utility.MessagePrinter;

import java.util.List;

@Component
public class OrderHistoryFilterController {
    private final ApplicationContext applicationContext;
    private final TableService tableService;
    private final EmployeeService employeeService;
    private final PipeOrderHistoryServiceImpl pipeOrderHistoryService;
    private Stage stage;
    @FXML
    private Label printMessage;
    @FXML
    private ChoiceBox<String> employeeChoice;
    @FXML
    private ChoiceBox<String> numberChoice;
    @FXML
    private ChoiceBox<Long> tableChoice;
    @FXML
    private ChoiceBox<String> priceChoice;
    @FXML
    private ChoiceBox<Status> statusChoice;
    @FXML
    private ChoiceBox<Payment> paymentChoice;
    @FXML
    private DatePicker dateBeforePicker;
    @FXML
    private DatePicker dateAfterPicker;
    @FXML
    private Button backButton;
    @FXML
    private Button submitButton;

    public OrderHistoryFilterController(ApplicationContext applicationContext, TableService tableService, EmployeeService employeeService, PipeOrderHistoryServiceImpl pipeOrderHistoryService) {
        this.applicationContext = applicationContext;
        this.tableService = tableService;
        this.employeeService = employeeService;
        this.pipeOrderHistoryService = pipeOrderHistoryService;
    }

    public void initialize() {
        submitButton.setDefaultButton(true);
        backButton.setCancelButton(true);
        employeeChoice.getItems().add(null);
        employeeChoice.getItems().addAll(employeeService.findAll().stream().map(Employee::getName).toList());
        tableChoice.getItems().add(null);
        tableChoice.getItems().addAll(tableService.getAll().stream().map(Tables::getNumber).toList());
        numberChoice.getItems().addAll(null,"< 5", "< 10", "> 10");
        priceChoice.getItems().addAll(null,"< 500ден", "< 1000ден", "< 2500ден", "< 5000ден", "< 7500ден", "< 10000ден", "< 1500ден", "> 30000ден");
        statusChoice.getItems().add(null);
        statusChoice.getItems().addAll(Status.values());
        paymentChoice.getItems().add(null);
        paymentChoice.getItems().addAll(Payment.values());
    }

    public void initStage(Stage stage) {
        this.stage = stage;
    }

    public void submitFilter() throws Exception {
        String name = employeeChoice.getValue();
        String tableNumber = tableChoice.getValue() != null ? tableChoice.getValue().toString() : null;
        String price = priceChoice.getValue() != null ? priceChoice.getValue() : null;
        String number = numberChoice.getValue() != null ? numberChoice.getValue(): null;
        String status = statusChoice.getValue() != null ? statusChoice.getValue().toString() : null;
        String payment = paymentChoice.getValue() != null ? paymentChoice.getValue().toString() : null;
        String dateAfter = dateAfterPicker.getValue() != null ? dateAfterPicker.getValue().toString() : null;
        String dateBefore = dateBeforePicker.getValue() != null ? dateBeforePicker.getValue().toString() : null;

        if (!isInvalidFormat(dateAfter))
            printMessage("Внесете валиден формат на датум од (мм/дд/гггг)", false);
        else if (!isInvalidFormat(dateBefore))
            printMessage("Внесете валиден формат на датум до (мм/дд/гггг)", false);
        else
            sendNewDataToOrderHistoryController(pipeOrderHistoryService.filter(name, number, tableNumber, price, status, payment, dateAfter, dateBefore));

    }

    private boolean isInvalidFormat(String date) {
        String dateFormatPattern = "\\d{2}/\\d{2}/\\d{4}";
        if(date != null)
            return !date.matches(dateFormatPattern);
        else
            return true;
    }

    public void sendNewDataToOrderHistoryController(List<Order> orders) throws Exception {
        applicationContext.getBean(OrderHistoryController.class).renderTable(orders);
        closeStage();
    }

    public void closeStage() throws Exception {
        if (stage != null)
            stage.close();
        else
            throw new Exception("Stage not found");
    }

    public void printMessage(String message, Boolean color) { MessagePrinter.printMessage(printMessage, message, color); }

}
