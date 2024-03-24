package system.pos.javafx.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.springframework.stereotype.Component;
import system.pos.javafx.stage.StageListener;
import system.pos.spring.enumm.EmployeeStatus;
import system.pos.spring.enumm.SideBar;
import system.pos.spring.enumm.UserRole;
import system.pos.spring.model.Employee;
import system.pos.spring.service.EmployeeService;


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

    public void initData(Object callingController) {
        this.callingController = callingController;
    }

    //Login algorithm
    public void userLogin() {
        if(!codeLabel.getText().isEmpty()) {
            try {
                employee = employeeService.checkLogin(Long.parseLong(codeLabel.getText()));
            } catch (NumberFormatException e) {
                printMessage("Невалидна операција! Внесете број.", false);
            }
            if(employee != null) {
                if(callingController instanceof SidebarController) {
                    if(((SidebarController) callingController).getSideBar().equals(SideBar.SETTINGS) && !employee.getE_role().equals(UserRole.МЕНАЏЕР)) {
                        printMessage("Не сте овластени!", false);
                    } else {
                        returnAuthToken();
                    }
                } else {
                    if(employee.getStatus().equals(EmployeeStatus.НЕАКТИВЕН)) {
                        printMessage("Не сте на смена.", false);
                    } else {
                        returnAuthToken();
                    }
                }
            } else {
                printMessage("Невалиден код.", false);
            }
        } else {
            printMessage("Внесето го вашиот код.", false);
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
        Platform.runLater(() -> {
            if(color) {
                loginMessage.setTextFill(Color.web("#27ae60"));
            } else {
                loginMessage.setTextFill(Color.web("#f62b2b"));
            }
            loginMessage.setText(message);

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
                loginMessage.setText(""); // Clear the message text after 5 seconds
            }));

            timeline.setCycleCount(1);
            timeline.play();
        });
    }
}