package system.pos.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import system.pos.javafx.controller.settingsControllers.OrderHistoryController;
import system.pos.javafx.stage.StageListener;
import system.pos.spring.model.Employee;

@Component
public class SettingsController {
    private final StageListener stageListener;
    private OrderHistoryController orderHistoryController;
    private final ApplicationContext applicationContext;

    public SettingsController(StageListener stageListener, OrderHistoryController orderHistoryController, ApplicationContext applicationContext) {
        this.stageListener = stageListener;
        this.orderHistoryController = orderHistoryController;
        this.applicationContext = applicationContext;
    }

    private String fxmlPath = "/fxml/newEmployee.fxml";
    private Employee employee;


    @FXML
    private Button backButton;
    @FXML
    private AnchorPane showSetting;

    @FXML
    public void initialize() {
        backButton.setCancelButton(true);
        changeIncludeSource(fxmlPath);
    }

    //Return button
    public void returnBack() {
        stageListener.changeScene( "/fxml/homePage.fxml");
    }

    public void openNewUser() {
        fxmlPath = "/fxml/newEmployee.fxml";
        changeIncludeSource(fxmlPath);
    }

    public void viewOrderHistory() {
        orderHistoryController.initData(employee);
        fxmlPath = "/fxml/orderHistory.fxml";
        changeIncludeSource(fxmlPath);
    }

    public void viewShiftLogs() {
        fxmlPath = "/fxml/shiftLogs.fxml";
        changeIncludeSource(fxmlPath);
    }

    public void viewOrderLogs() {
        fxmlPath = "/fxml/orderLogs.fxml";
        changeIncludeSource(fxmlPath);
    }

    public void openNewCategory() {
        fxmlPath = "/fxml/newCategory.fxml";
        changeIncludeSource(fxmlPath);
    }

    public void openNewProduct() {
        fxmlPath = "/fxml/newProduct.fxml";
        changeIncludeSource(fxmlPath);
    }

    public void openNewTable() {
        fxmlPath = "/fxml/newTable.fxml";
        changeIncludeSource(fxmlPath);
    }

    //Change hte inside view
    public void changeIncludeSource(String newFXMLPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(newFXMLPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent newContent = loader.load();
            showSetting.getChildren().setAll(newContent);
        } catch (Exception e) {
            System.out.println("Error");
        }
    }

    public void initData(Employee employee) {
        this.employee = employee;
    }
}
