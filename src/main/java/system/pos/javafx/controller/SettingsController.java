package system.pos.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import system.pos.javafx.stage.StageListener;

@Component
public class SettingsController {
    private final StageListener stageListener;
    private final ApplicationContext applicationContext;

    public SettingsController(StageListener stageListener, ApplicationContext applicationContext) {
        this.stageListener = stageListener;
        this.applicationContext = applicationContext;
    }

    private String fxmlPath = "/fxml/newEmployee.fxml";


    @FXML
    private Button backButton;
    @FXML
    private AnchorPane showSetting;

    @FXML
    public void initialize() {
        backButton.setCancelButton(true);
        changeIncludeSource(fxmlPath);
    }

    public void returnBack() {
        stageListener.changeScene( "/fxml/homePage.fxml");
    }

    public void openNewUser() {
        fxmlPath = "/fxml/newEmployee.fxml";
        changeIncludeSource(fxmlPath);
    }

    public void viewOrderHistory() {
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
}
