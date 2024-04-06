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
    private final SettingsController settingsController;
    private final StageListener stageListener;
    private final EmployeeService employeeService;
    private final AuthLogsService authLogsService;

    public SidebarController(AuthenticationController authenticationController, SettingsController settingsController, StageListener stageListener, EmployeeService employeeService, AuthLogsService authLogsService) {
        this.authenticationController = authenticationController;
        this.settingsController = settingsController;
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
        listActiveUsers();
    }

    //Open Settings from Authentication view
    public void settingAuthenticator() {
        authenticationController.initData(this,null);
        sideBar = SideBar.SETTINGS;
        stageListener.changeScene("/fxml/authentication.fxml");
    }

    //Change user working status from Authentication view
    public void shiftAuthenticator() {
        authenticationController.initData(this,null);
        sideBar = SideBar.SHIFT;
        stageListener.changeScene("/fxml/authentication.fxml");
    }

    //Open the correct view after authentication
    public void openCorrectFxml(Employee employee) {
        if(sideBar.equals(SideBar.SHIFT)) {
            changeShiftStatus(employee);
        } else {
            settingsController.initData(employee);
            stageListener.changeScene("/fxml/settings.fxml");
        }
    }

    //Change the user working status
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

    //List the active users on the front page
    public void listActiveUsers() {
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

    //Show the time on the front page
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
