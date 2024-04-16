package system.pos.javafx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.springframework.stereotype.Component;
import system.pos.javafx.controller.settingsControllers.OrderHistoryController;
import system.pos.javafx.stage.StageListener;
import system.pos.spring.enumm.Payment;
import system.pos.spring.enumm.UserRole;
import system.pos.spring.model.Employee;
import system.pos.spring.model.Order;
import system.pos.spring.service.OrderService;
import system.pos.spring.service.TableService;
import system.pos.spring.utility.MessagePrinter;

@Component
public class PaymentController {
    private final StageListener stageListener;
    private final OrderService orderService;
    private final TableService tableService;
    private Object callingController;

    public PaymentController(StageListener stageListener, OrderService orderService, TableService tableService) {
        this.stageListener = stageListener;
        this.orderService = orderService;
        this.tableService = tableService;
    }

    @FXML
    private BorderPane border;
    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private Label messageLabel;
    @FXML
    private TextField numberLabel;
    @FXML
    private RadioButton cashRadio;
    @FXML
    private RadioButton cardRadio;
    @FXML
    private RadioButton invoiceRadio;
    @FXML
    private Button submitButton;
    @FXML
    private Label codeLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label nmTableLabel;
    @FXML
    private TextField discountField;
    @FXML
    private Button discountButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button backButton;
    @FXML
    private TextField receiveField;
    @FXML
    private Label changeLabel;

    private Order order;
    private Employee employee;

    @FXML
    public void initialize() {
        backButton.setCancelButton(true);
        submitButton.setDefaultButton(true);
        border.setOnKeyPressed(this::handleKeyPressed);
        Platform.runLater(numberLabel::requestFocus);

        renderData();
    }

    public void initData(Object callingController, Order order, Employee employee) {
        this.callingController = callingController;
        this.order = order;
        this.employee = employee;
    }

    private void handleKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.F1) {
            cashRadio.setSelected(true);
        } else if (keyEvent.getCode() == KeyCode.F2) {
            cardRadio.setSelected(true);
        } else if (keyEvent.getCode() == KeyCode.F3) {
            invoiceRadio.setSelected(true);
        } else if(keyEvent.getCode() == KeyCode.F4) {
            numberLabel.requestFocus();
        } else if(keyEvent.getCode() == KeyCode.F5) {
            receiveField.requestFocus();
        } else if(keyEvent.getCode() == KeyCode.F6 && employee.getE_role().equals(UserRole.МЕНАЏЕР)) {
            discountField.requestFocus();
        } else if(keyEvent.getCode() == KeyCode.F7 && employee.getE_role().equals(UserRole.МЕНАЏЕР)) {
            makeDiscount();
        } else if(keyEvent.getCode() == KeyCode.F8 && employee.getE_role().equals(UserRole.МЕНАЏЕР)) {
            resetDiscount();
        } else if(keyEvent.getCode() == KeyCode.F9) {
            //PRINT PAYCHECK
        }
    }

    public void renderData() {
        UserRole role = employee.getE_role();

        codeLabel.setText(order.getCode().toString());
        nmTableLabel.setText("Маса: " + order.getTable_number());
        setPriceLabel();
        discountField.setVisible(role.equals(UserRole.МЕНАЏЕР));
        discountButton.setVisible(role.equals(UserRole.МЕНАЏЕР));
        resetButton.setVisible(role.equals(UserRole.МЕНАЏЕР));
    }

    public void calculateChange() {
        String receive = receiveField.getText();
        if(receive.isBlank()) {
            changeLabel.setText("Кусур: 0ден");
        } else {
            try {
                changeLabel.setText("Кусур: " + (Integer.parseInt(receive) - (order.getPrice()) + "ден"));
            } catch (NumberFormatException e) {
                printMessage("Невалидна операција! Внеси број.", false);
            }
        }
    }

    public void finishPayment() {
        Payment pay = getSelectedRadioButton();
        String numberOfPeople = numberLabel.getText();

        if(pay != null && !numberOfPeople.isBlank()) {
            try {
                orderService.payOrder(order, pay, Integer.parseInt(numberOfPeople));
            } catch (NumberFormatException e) {
                printMessage("Невалидна операција! Внеси број.", false);
                return;
            }

            tableService.reset(tableService.findByNumber(order.getTable_number().longValue()));
            if (callingController instanceof InsideTableController)
                stageListener.changeScene("/fxml/homePage.fxml");
            else
                returnBack();

        } else {
            printMessage("Внеси ги сите податоци!", false);
        }
    }

    public void makeDiscount() {
        String percentDiscount = discountField.getText();
        if(percentDiscount.isBlank()) {
            printMessage("Внеси процент за попуст!", false);
        } else {
            Integer price = order.getPrice();
            Integer percent;
            try {
                percent = Integer.parseInt(percentDiscount);
            } catch (NumberFormatException e) {
                printMessage("Невалидна оперцаија! Внеси број.", false);
                return;
            }

            if(percent < 0 || percent > 100) {
                printMessage("Внесете вредност од (0 - 100)",false);
            } else {
                orderService.makeDiscount(order,price, percent);
                printMessage("Успешно направен попуст",true);
                setPriceLabel();
            }
        }
    }

    public void resetDiscount() {
        if(order.getDiscount() == null || order.getDiscount() == 0) {
            printMessage("Нарачката нема попуст!",false);
        } else {
            orderService.resetDiscount(order);
            printMessage("Успешно ресетиран попуст",true);
            setPriceLabel();
        }
    }

    public void setPriceLabel() {
        calculateChange();
        discountField.setText("");
        priceLabel.setText("Вкупно: " + order.getPrice());
    }

    public void returnBack() {
        if(callingController instanceof OrderHistoryController) {
            stageListener.changeScene("/fxml/settings.fxml");
        } else if (callingController instanceof InsideTableController) {
            stageListener.changeScene("/fxml/insideTable.fxml");
        }
    }

    public Payment getSelectedRadioButton() {
        Toggle selectedToggle = toggleGroup.getSelectedToggle();

        if (selectedToggle == null)
            return null;
        else if (selectedToggle.equals(cashRadio))
            return Payment.ГОТОВИНА;
        else if (selectedToggle.equals(cardRadio))
            return Payment.КАРТИЧКА;
        else
            return Payment.ФАКТУРА;
    }

    public void printMessage(String message, Boolean color) {
        MessagePrinter.printMessage(messageLabel, message, color);
    }
}
