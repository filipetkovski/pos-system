package system.pos.javafx.controller.settingsControllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.springframework.stereotype.Component;
import system.pos.spring.model.Category;
import system.pos.spring.service.ProductService;

import java.util.Arrays;
import java.util.List;

@Component
public class CategoryController {
    private final ProductService productService;

    public CategoryController(ProductService productService) {
        this.productService = productService;
    }

    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private RadioButton foodRadio;
    @FXML
    private RadioButton drinkRadio;
    @FXML
    private TextField nameInput;
    @FXML
    private Button submitButton;
    @FXML
    private Label messageLabel;
    @FXML
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, String> nameColumn;
    @FXML
    private TableColumn<Category, String> supCatColumn;
    @FXML
    private TableColumn<Category, String> visibleColumn;

    @FXML
    public void initialize() {
        submitButton.setDefaultButton(true);
        initTable();
        renderTables();
    }

    public void initTable() {
        categoryTable.getColumns().forEach(column -> {column.setReorderable(false);column.setResizable(false);});
        categoryTable.setFocusTraversable(false);
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        visibleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isVisible() ? "True" : "False"));
        supCatColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupercategory().getName()));

        editTable();
    }

    public void renderTables() {
        List<Category> categories = productService.getSecondLevelCategories();

        categoryTable.getItems().clear();

        categories.forEach(cat -> categoryTable.getItems().add(cat));
    }

    public void createCategory() {
        if(nameInput.getText().isBlank() ||  toggleGroup.getSelectedToggle() == null) {
            printMessage("Внеси ги сите податоци.",false);
        } else {
            String name = capitalizeFirstLetter(nameInput.getText());

            String supercategory = getSelectedRadioButton();

            Category category = productService.isValidCategory(name, supercategory);
            if(category != null) {
                printMessage("Успешно внесена категорија!", true);
                nameInput.clear();

                toggleGroup.getToggles().forEach(toggle -> {
                    if (toggle.isSelected())
                        toggle.setSelected(false);
                });

                productService.addCategory(category);
                categoryTable.getItems().add(category);
            } else {
                printMessage("Постои категорија со исто име!",false);
            }
        }
    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String firstLetter = input.substring(0, 1).toUpperCase();
        String restOfLetters = input.substring(1).toLowerCase();
        return firstLetter + restOfLetters;
    }

    public void deleteCategory() {
        TableView.TableViewSelectionModel<Category> selectionModel = categoryTable.getSelectionModel();

        if(selectionModel.isEmpty()) {
            printMessage("Мора да селектирате категорија!",false);
            return;
        }

        ObservableList<Integer> list = selectionModel.getSelectedIndices();
        Integer[] selectedIndices = new Integer[list.size()];
        selectedIndices = list.toArray(selectedIndices);

        Arrays.sort(selectedIndices);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Потврди диалог");
        alert.setHeaderText("Дали сте сигурен да избришете категорија?");

        Integer[] finalSelectedIndices = selectedIndices;
        alert.showAndWait().ifPresent(response -> { // Handle the user's response
            if (response == ButtonType.OK) {
                for (Integer finalSelectedIndex : finalSelectedIndices) {
                    selectionModel.clearSelection(finalSelectedIndex);
                    Category category = categoryTable.getItems().get(finalSelectedIndex);
                    productService.changeProductsCategory(category.getProducts());
                    category.setProducts(null);
                    productService.deleteCategory(category); //If the selected category is from the database, delete it
                    categoryTable.getItems().remove(finalSelectedIndex.intValue()); //Remove from the table view
                }
            }
        });
    }

    public String getSelectedRadioButton() {
        Toggle selectedToggle = toggleGroup.getSelectedToggle();

        if (selectedToggle != null) {
            if (selectedToggle.equals(foodRadio))
                return "Храна";
            else if (selectedToggle.equals(drinkRadio))
                return "Пијалоци";
        }

        return null;
    }

    public void editTable() {
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Category category = event.getTableView().getItems().get(event.getTablePosition().getRow());
            Category isCategory = productService.findCategoryByName(category.getName());

            if(isCategory != null) {
                String name = capitalizeFirstLetter(event.getNewValue());
                if(!name.isBlank()) {
                    category.setName(name);
                    productService.addCategory(category);
                } else {
                    printMessage("Името не смее да е празно!", false);
                }
            } else {
                printMessage("Категоријата не е пронајдена!", false);
            }

            renderTables();
        });

        supCatColumn.setCellFactory(column -> {
            TableCell<Category, String> cell = new TableCell<>() {
                private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

                {
                    choiceBox.getItems().addAll("Пијалоци","Храна","Скриена"); // Assuming getSecondLevelCategories() returns a List<String> of categories
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

        supCatColumn.setOnEditCommit(event -> {
            Category category = event.getTableView().getItems().get(event.getTablePosition().getRow());

            if (category != null) {
                category.setSupercategory(productService.findCategoryByName(event.getNewValue()));
                productService.addCategory(category);
                printMessage("Успешно променета надкатегорија", true);
            } else {
                printMessage("Категоријата не е пронајдена!", false);
            }

            renderTables();
        });

        visibleColumn.setCellFactory(column -> {
            TableCell<Category, String> cell = new TableCell<>() {
                private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

                {
                    choiceBox.getItems().addAll("Да","Не"); // Assuming getSecondLevelCategories() returns a List<String> of categories
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

        visibleColumn.setOnEditCommit(event -> {
            Category category = event.getRowValue();
            String newVisibility = event.getNewValue(); // Assuming the new value is a category name
            Category isCategory = productService.findCategoryByName(category.getName());

            if (isCategory != null) {
                if(newVisibility.equals("Да")) {
                    category.setVisible(true);
                } else {
                    category.setVisible(false);
                }
                productService.addCategory(category);
            } else {
                printMessage("Категоријата не е пронајдена!", false);
            }

            renderTables();
        });
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
