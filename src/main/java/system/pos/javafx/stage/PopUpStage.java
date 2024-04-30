package system.pos.javafx.stage;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import system.pos.javafx.controller.settingsControllers.OrderHistoryController;
import system.pos.javafx.controller.settingsControllers.filterControllers.OrderHistoryFilterController;

import java.io.IOException;
import java.net.URL;

@Component
public class PopUpStage {
    private final ApplicationContext applicationContext;
    private final OrderHistoryFilterController orderHistoryFilterController;
    private Object callingController;

    public PopUpStage(ApplicationContext applicationContext, OrderHistoryFilterController orderHistoryFilterController) {
        this.applicationContext = applicationContext;
        this.orderHistoryFilterController = orderHistoryFilterController;
    }

    public void initData(Object callingController) {
        this.callingController = callingController;
    }

    public void openPopUP(URL url) {
        try {
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1206,375);
            scene.getStylesheets().add("path/to/your/style.css");
            stage.setScene(scene);
            stage.setTitle("Филтер");
            initCallingControllerStages(stage);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initCallingControllerStages(Stage stage) {
        if(callingController instanceof OrderHistoryController) {
            orderHistoryFilterController.initStage(stage);
        }
    }
}
