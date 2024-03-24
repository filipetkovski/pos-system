package system.pos.javafx.controller.settingsControllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.springframework.stereotype.Component;
import system.pos.spring.enumm.Status;
import system.pos.spring.enumm.TableRegion;
import system.pos.spring.model.Order;
import system.pos.spring.model.Tables;
import system.pos.spring.service.OrderService;
import system.pos.spring.service.TableService;

import java.util.Arrays;
import java.util.List;

@Component
public class TableFormController {
    private final TableService tableService;
    private final OrderService orderService;

    public TableFormController(TableService tableService, OrderService orderService) {
        this.tableService = tableService;
        this.orderService = orderService;
    }

    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private RadioButton nadvorRadio;
    @FXML
    private RadioButton vnatreRadio;
    @FXML
    private RadioButton terasaRadio;
    @FXML
    private RadioButton shankRadio;
    @FXML
    private TextField numberInput;
    @FXML
    private Button submitButton;
    @FXML
    private Label messageLabel;
    @FXML
    private TableView<Tables> tableTable;
    @FXML
    private TableColumn<Tables, String> regionColumn;
    @FXML
    private TableColumn<Tables, Long> numberColumn;

    @FXML
    public void initialize() {
        submitButton.setDefaultButton(true);
        initTable();
        renderTables();
        editTable();
    }

    public void initTable() {
        tableTable.getColumns().forEach(column -> {column.setReorderable(false);column.setResizable(false);});
        tableTable.setFocusTraversable(false);

        numberColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getNumber()).asObject());
        regionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRegion().toString()));
    }

    public void renderTables() {
        List<Tables> tablesList = tableService.getAll();

        tableTable.getItems().clear();

        tablesList.forEach(employee -> tableTable.getItems().add(employee));
    }

    public void createTable() {
        String number = numberInput.getText();
        if(number.isBlank() ||  toggleGroup.getSelectedToggle() == null) {
            printMessage("Внеси ги сите податоци!", false);
        } else {

            long parseNumber;
            try {
                parseNumber = Long.parseLong(number);
            } catch (NumberFormatException e) {
                printMessage("Невалидна операција! Внеси број.", false);
                return;
            }

            TableRegion role = getSelectedRadioButton();

            Tables table = tableService.isValidCode(parseNumber, role);
            if(table != null) {
                printMessage("Успешно внесена маса!", true);
                messageLabel.setTextFill(Color.web("#27ae60"));
                numberInput.clear();

                toggleGroup.getToggles().forEach(toggle -> {
                    if (toggle.isSelected())
                        toggle.setSelected(false);
                });

                tableTable.getItems().add(table);
            } else {
                printMessage("Постои маса со ист број! Внеси различен број.", false);
                numberInput.clear();
            }
        }
    }

    public void deleteTable() {
        TableView.TableViewSelectionModel<Tables> selectionModel = tableTable.getSelectionModel();

        if(selectionModel.isEmpty()) {
            printMessage("Мора да селектирате маса!", false);
            return;
        }

        ObservableList<Integer> list = selectionModel.getSelectedIndices();
        Integer[] selectedIndices = new Integer[list.size()];
        selectedIndices = list.toArray(selectedIndices);

        Arrays.sort(selectedIndices);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Потврди диалог");
        alert.setHeaderText("Дали сте сигурен да избришете маса?");

        Integer[] finalSelectedIndices = selectedIndices;
        alert.showAndWait().ifPresent(response -> { // Handle the user's response
            if (response == ButtonType.OK) {
                for (Integer finalSelectedIndex : finalSelectedIndices) {
                    selectionModel.clearSelection(finalSelectedIndex);
                    Tables tables = tableTable.getItems().get(finalSelectedIndex);
                    Order order = tables.getOrder();
                    if(order != null) {
                        orderService.changeStatus(order, Status.ОТКАЖАНА);
                    }
                    tableService.delete(tables); //If the selected table is from the database, delete it
                    tableTable.getItems().remove(finalSelectedIndex.intValue()); //Remove from the table view
                }
            }
        });
    }
    public void editTable() {
        regionColumn.setCellFactory(column -> {
            TableCell<Tables, String> cell = new TableCell<>() {
                private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

                {
                    choiceBox.getItems().addAll(Arrays.stream(TableRegion.values()).map(Enum::name).toList()); // Assuming getSecondLevelCategories() returns a List<String> of categories
                    choiceBox.setOnAction(event -> {
                        if (isEditing()) {
                            commitEdit(choiceBox.getValue());
                        }
                    });
                }

                @Override
                public void startEdit() {
                    super.startEdit();
                    setText(null);
                    setGraphic(choiceBox);

                    getTableView().edit(getIndex(), getTableColumn());


                    // Use Platform.runLater() to ensure proper initialization
                    Platform.runLater(() -> {
                        choiceBox.show();
                        choiceBox.requestFocus();
                    });
                }

                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (isEditing()) {
                            setGraphic(choiceBox);
                            setText(null);
                        } else {
                            setText(item);
                            setGraphic(null);
                        }
                    }
                }
            };
            return cell;
        });

        regionColumn.setOnEditCommit(event -> {
            Tables tables = event.getRowValue();
            String newRegion = event.getNewValue(); // Assuming the new value is a category name
            Tables isTables = tableService.findByNumber(tables.getNumber());

            if (isTables != null) {
                tables.setRegion(TableRegion.valueOf(newRegion));
                tableService.save(tables);
            } else {
                printMessage("Масата не е пронајдена!", false);
            }

            renderTables();
        });
    }

    public TableRegion getSelectedRadioButton() {
        Toggle selectedToggle = toggleGroup.getSelectedToggle();

        if (selectedToggle == null)
            return null;

        if (selectedToggle.equals(nadvorRadio)) {
            return TableRegion.НАДВОР;
        } else if (selectedToggle.equals(vnatreRadio)) {
            return TableRegion.ВНАТРЕ;
        } else if(selectedToggle.equals(terasaRadio)) {
            return TableRegion.ТЕРАСА;
        } else if(selectedToggle.equals(shankRadio)) {
            return TableRegion.ШАНК;
        }

        return null;
    }

    public void printMessage(String message, Boolean color) {
        Platform.runLater(() -> {
            if(color) {
                messageLabel.setTextFill(Color.web("#27ae60"));
            } else {
                messageLabel.setTextFill(Color.web("#f62b2b"));
            }
            messageLabel.setText(message);

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
                messageLabel.setText(""); // Clear the message text after 5 seconds
            }));

            timeline.setCycleCount(1);
            timeline.play();
        });
    }
}
