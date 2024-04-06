package system.pos.javafx.controller.settingsControllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.stereotype.Component;
import system.pos.spring.enumm.UserRole;
import system.pos.spring.model.Employee;
import system.pos.spring.service.EmployeeService;
import system.pos.spring.utility.ChoiceBoxTableCellFactory;
import system.pos.spring.utility.MessagePrinter;

import java.util.Arrays;
import java.util.List;

@Component
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private RadioButton adminRadio;
    @FXML
    private RadioButton serverRadio;
    @FXML
    private Button submitButton;
    @FXML
    private TextField nameInput;
    @FXML
    private TextField codeInput;
    @FXML
    private Label messageLabel;
    @FXML
    private TableView<Employee> userTable;
    @FXML
    private TableColumn<Employee, String> nameColumn;
    @FXML
    private TableColumn<Employee, String> codeColumn;
    @FXML
    private TableColumn<Employee, String> roleColumn;

    @FXML
    public void initialize() {
        submitButton.setDefaultButton(true);

        initTable();
        printAllEmployees();
    }

    public void initTable() {
        userTable.getColumns().forEach(column -> {column.setReorderable(false);column.setResizable(false);});
        userTable.setFocusTraversable(false);

        codeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCode().toString().toUpperCase()));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getE_role().toString()));
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        editTable();
    }

    public void printAllEmployees() {
        List<Employee>  employees = employeeService.findAll();

        userTable.getItems().clear();

        employees.forEach(employee -> userTable.getItems().add(employee));
    }

    public void editTable() {
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Employee employee = event.getTableView().getItems().get(event.getTablePosition().getRow());
            Employee isEmployee = employeeService.findByCode(employee.getCode());

            if(isEmployee != null) {
                String name = event.getNewValue();
                if(!name.isBlank()) {
                    employee.setName(name);
                    employeeService.updateEmployee(employee);
                } else {
                    printMessage("Мора да внесете име!",false);
                }
            } else {
                printMessage("Корисникот не е пронајден!", false);
            }

            printAllEmployees();
        });

        roleColumn.setCellFactory(column -> ChoiceBoxTableCellFactory
                .createCellFactory(new ChoiceBox<>(FXCollections.observableList(Arrays.stream(UserRole.values()).map(Enum::name).toList()))));

        roleColumn.setOnEditCommit(event -> {
            Employee employee = event.getRowValue();
            String newRole = event.getNewValue(); // Assuming the new value is a category name
            Employee isEmployee = employeeService.findByCode(employee.getCode());

            if (isEmployee != null) {
                employee.setE_role(UserRole.valueOf(newRole));
                employeeService.save(employee);
            } else {
                printMessage("Корисникот не е пронајден!", false);
            }

            printAllEmployees();
        });
    }

    public void deleteTable() {
        TableView.TableViewSelectionModel<Employee> selectionModel = userTable.getSelectionModel();

        if(selectionModel.isEmpty()) {
            printMessage("Мора да селектирате корисник!", false);
            return;
        }

        ObservableList<Integer> list = selectionModel.getSelectedIndices();
        Integer[] selectedIndices = new Integer[list.size()];
        selectedIndices = list.toArray(selectedIndices);

        Arrays.sort(selectedIndices);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Потрвди диалог");
        alert.setHeaderText("Дали сте сигурен да избришете корисник?");

        Integer[] finalSelectedIndices = selectedIndices;
        alert.showAndWait().ifPresent(response -> { // Handle the user's response
            if (response == ButtonType.OK) {
                for (Integer finalSelectedIndex : finalSelectedIndices) {
                    selectionModel.clearSelection(finalSelectedIndex);
                    Employee employee = userTable.getItems().get(finalSelectedIndex);
                    employeeService.deleteEmployee(employee); //If the selected product is from the database, delete it
                    userTable.getItems().remove(finalSelectedIndex.intValue()); //Remove from the table view
                }
            }
        });
    }

    public void createEmployee() {
        if(nameInput.getText().isBlank() || codeInput.getText().isBlank() ||  toggleGroup.getSelectedToggle() == null) {
            printMessage("Внеси ги сите податоци", false);
        } else {
            String code = codeInput.getText();
            String name = nameInput.getText();
            UserRole role = getSelectedRadioButton();

            long parseCode;
            try {
                parseCode = Long.parseLong(code);
            } catch (NumberFormatException e) {
                printMessage("Невалидна операција! Внеси број.", false);
                return;
            }

            Employee employee = employeeService.isValidCode(parseCode, name, role);
            if(employee != null) {
                printMessage("Успешно додавање на нов корисник!", true);
                codeInput.clear();
                nameInput.clear();

                toggleGroup.getToggles().forEach(toggle -> {
                    if (toggle.isSelected())
                        toggle.setSelected(false);
                });

                userTable.getItems().add(employee);
            } else {
                printMessage("Постои корисник со ист код! Внеси различен код.",false );
                codeInput.clear();
            }
        }
    }

    public UserRole getSelectedRadioButton() {
        Toggle selectedToggle = toggleGroup.getSelectedToggle();
        if (selectedToggle != null)
            return selectedToggle.equals(adminRadio) ? UserRole.МЕНАЏЕР : UserRole.ЌЕЛНЕР;
        return null;
    }

    public void printMessage(String message, Boolean color) {
        MessagePrinter.printMessage(messageLabel, message, color);
    }
}
