package system.pos.javafx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Data;
import org.springframework.stereotype.Component;
import system.pos.javafx.stage.StageListener;
import system.pos.spring.enumm.EmployeeStatus;
import system.pos.spring.enumm.SideBar;
import system.pos.spring.model.Employee;
import system.pos.spring.service.AuthLogsService;
import system.pos.spring.service.EmployeeService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
@Data
public class SidebarController {
    private final AuthenticationController authenticationController;
    private final StageListener stageListener;
    private final EmployeeService employeeService;
    private final AuthLogsService authLogsService;

    public SidebarController(AuthenticationController authenticationController, StageListener stageListener, EmployeeService employeeService, AuthLogsService authLogsService) {
        this.authenticationController = authenticationController;
        this.stageListener = stageListener;
        this.employeeService = employeeService;
        this.authLogsService = authLogsService;
    }

    @FXML
    private VBox usersListBox;
    @FXML
    private Label timeLabel;
    private volatile boolean stop = false;

    private SideBar sideBar;

    @FXML
    public void initialize()  {
        timeNow();
        printActiveUsers();
    }

    public void settingAuthenticator() {
        authenticationController.initData(this);
        sideBar = SideBar.SETTINGS;
        stageListener.changeScene("/fxml/authentication.fxml");
    }

    public void shiftAuthenticator() {
        authenticationController.initData(this);
        sideBar = SideBar.SHIFT;
        stageListener.changeScene("/fxml/authentication.fxml");
    }

    public void openCorrectFxml(Employee employee) {
        if(sideBar.equals(SideBar.SHIFT)) {
            changeShiftStatus(employee);
        } else {
          stageListener.changeScene("/fxml/settings.fxml");
        }
    }

    public void changeShiftStatus(Employee employee) {
        String status;
        if(employee.getStatus().equals(EmployeeStatus.АКТИВЕН)) {
            status = "ОДЈАВА";
            employee.setStatus(EmployeeStatus.НЕАКТИВЕН);
        } else {
            status = "НАЈАВА";
            employee.setStatus(EmployeeStatus.АКТИВЕН);
        }
        authLogsService.save(employee,status);
        employeeService.updateEmployee(employee);
        stageListener.changeScene("/fxml/homePage.fxml");
    }

    public void printActiveUsers() {
        List<Employee> employees = employeeService.findActiveEmployees();

        if(employees.isEmpty()) {
            Label label = new Label("No active users");
            label.setId("activeUsersLabel");
            usersListBox.getChildren().add(label);
        } else {
            employees.forEach(employee -> {
                Label label = new Label(employee.getName());
                label.setId("activeUsersLabel");
                usersListBox.getChildren().add(label);
            });
        }
    }

    private void timeNow() {
        Thread thread = new Thread(() -> {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
            while (!stop) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println(e);
                    return;
                }

                final String timeNow = simpleDateFormat.format(new Date());
                Platform.runLater(()-> timeLabel.setText(timeNow));
            }
        });
        thread.start();
    }

}
