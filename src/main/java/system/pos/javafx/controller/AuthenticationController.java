package system.pos.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
import system.pos.javafx.stage.StageListener;
import system.pos.spring.enumm.EmployeeStatus;
import system.pos.spring.enumm.SideBar;
import system.pos.spring.enumm.UserRole;
import system.pos.spring.model.Employee;
import system.pos.spring.model.Tables;
import system.pos.spring.service.EmployeeService;
import system.pos.spring.utility.MessagePrinter;


@Component
public class AuthenticationController {
    private final EmployeeService employeeService;
    private final StageListener stageListener;
    private Object callingController;


    public AuthenticationController(EmployeeService employeeService, StageListener stageListener) {
        this.employeeService = employeeService;
        this.stageListener = stageListener;
    }

    private Employee employee;
    private Tables openTable;

    @FXML
    private Button loginButton;
    @FXML
    private Label loginMessage;
    @FXML
    private TextField codeLabel;
    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        codeLabel.requestFocus();
        loginButton.setDefaultButton(true);
        backButton.setCancelButton(true);
    }

    public void initData(Object callingController, Tables openTable) {
        this.callingController = callingController;
        this.openTable = openTable;
    }

    //Login algorithm
    public void userLogin() {
        String code = codeLabel.getText();
        if(code.isEmpty()) {
            printMessage("Внесето го вашиот код.", false);
        } else {
            try {
                employee = employeeService.checkLogin(Long.parseLong(code));
            } catch (NumberFormatException e) {
                printMessage("Невалидна операција! Внесете број.", false);
            }

            if(employee == null) {
                printMessage("Невалиден код.", false);
            } else {
                if(callingController instanceof SidebarController) {
                    if(((SidebarController) callingController).getSideBar().equals(SideBar.SETTINGS) && !employee.getE_role().equals(UserRole.МЕНАЏЕР)) {
                        printMessage("Не сте овластени!", false);
                    } else {
                        returnAuthToken();
                    }
                } if(callingController instanceof InsideTableController) {
                    if(!employee.getE_role().equals(UserRole.МЕНАЏЕР)) {
                        printMessage("Не сте овластени!", false);
                    } else {
                        returnAuthToken();
                    }
                } else {
                    if(employee.getStatus().equals(EmployeeStatus.НЕАКТИВЕН)) {
                        printMessage("Не сте на смена.", false);
                    } else {
                        if(openTable.getOrder() == null || employee.getCode().equals(openTable.getOrder().getEmployee().getCode()) || employee.getE_role().equals(UserRole.МЕНАЏЕР)) {
                            returnAuthToken();
                        } else {
                            printMessage("Ќелнер: " + openTable.getOrder().getEmployee().getName(), false);
                        }
                    }
                }
            }
        }

        codeLabel.clear();
    }

    public void returnBack() { //Clicking on ESC button will back you to the previous scene
        if (callingController instanceof HomePageController) {
            stageListener.changeScene("/fxml/homePage.fxml");
        } else if(callingController instanceof InsideTableController) {
            stageListener.changeScene("/fxml/insideTable.fxml");
        } else if(callingController instanceof SidebarController) {
            stageListener.changeScene("/fxml/homePage.fxml");
        }
    }

    public void returnAuthToken() { //Return the authToken to the right controller
        if (callingController instanceof HomePageController) {
            ((HomePageController) callingController).returnToClickTable(employee);
        } else if(callingController instanceof InsideTableController) {
            ((InsideTableController) callingController).cancelOrder();
        } else if(callingController instanceof SidebarController) {
            ((SidebarController) callingController).openCorrectFxml(employee);
        }
    }

    public void printMessage(String message, Boolean color) {
        MessagePrinter.printMessage(loginMessage, message, color);
    }
}
