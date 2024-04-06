package system.pos.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import org.springframework.stereotype.Component;
import system.pos.javafx.stage.StageListener;
import system.pos.spring.model.Employee;
import system.pos.spring.model.Tables;
import system.pos.spring.service.TableService;

import java.util.List;

@Component
public class HomePageController {
    private final TableService tableService;
    private final StageListener stageListener;
    private final AuthenticationController authenticationController;
    private final InsideTableController insideTableController;
    private Tables openTable;

    public HomePageController(TableService tableService, AuthenticationController controller, StageListener stageListener, InsideTableController insideTableController) {
        this.tableService = tableService;
        this.authenticationController = controller;
        this.stageListener = stageListener;
        this.insideTableController = insideTableController;
    }

    @FXML
    private FlowPane flowPane;

    @FXML
    public void initialize() {
        createTableButtons(tableService.getAll()); //Render table button
    }

    public void createTableButtons(List<Tables> tables) {
        for (Tables table : tables) {
            Button button = new Button(table.getNumber().toString());
            button.setId(table.getNumber().toString());
            if(table.getOrder() != null) { //Color green to those table-buttons that have order inside
                button.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                button.setText(table.getNumber() + "\n"
                        + table.getOrder().getEmployee().getName());
            }
            button.setOnAction(event -> clickTable(table)); //Activate the method by clicking on a table button
            flowPane.getChildren().add(button);
        }
    }

    private void clickTable(Tables table) { //Open authentication, authenticate, so you can open the table
        openTable = table;
        authenticationController.initData(this,openTable);
        stageListener.changeScene("/fxml/authentication.fxml");
    }

    public void returnToClickTable(Employee employee) { //This method is called when the authentication token is received
        insideTableController.initData(openTable, employee);
        stageListener.changeScene("/fxml/insideTable.fxml");
    }
}
