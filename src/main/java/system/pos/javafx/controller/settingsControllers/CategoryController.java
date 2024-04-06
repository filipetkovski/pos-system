package system.pos.javafx.controller.settingsControllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.stereotype.Component;
import system.pos.spring.model.Category;
import system.pos.spring.service.ProductService;
import system.pos.spring.utility.CapitalizeFirstLetter;
import system.pos.spring.utility.ChoiceBoxTableCellFactory;
import system.pos.spring.utility.MessagePrinter;

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
        if (nameInput.getText().isBlank() || toggleGroup.getSelectedToggle() == null) {
            printMessage("Внеси ги сите податоци.", false);
        } else {
            String name = CapitalizeFirstLetter.capitalizeFirstLetter(nameInput.getText());

            String supercategory = getSelectedRadioButton();

            Category category = productService.isValidCategory(name, supercategory);
            if (category != null) {
                printMessage("Успешно внесена категорија!", true);
                nameInput.clear();

                toggleGroup.getToggles().forEach(toggle -> {
                    if (toggle.isSelected())
                        toggle.setSelected(false);
                });

                productService.addCategory(category);
                categoryTable.getItems().add(category);
            } else {
                printMessage("Постои категорија со исто име!", false);
            }
        }
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

    public void editTable() {
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Category category = event.getTableView().getItems().get(event.getTablePosition().getRow());
            Category isCategory = productService.findCategoryByName(category.getName());

            if(isCategory != null) {
                String name = CapitalizeFirstLetter.capitalizeFirstLetter(event.getNewValue());
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

        supCatColumn.setCellFactory(column -> ChoiceBoxTableCellFactory
                .createCellFactory(new ChoiceBox<>(FXCollections.observableList(List.of("Пијалоци", "Храна", "Скриена")))));

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

        visibleColumn.setCellFactory(column -> ChoiceBoxTableCellFactory
                .createCellFactory(new ChoiceBox<>(FXCollections.observableList(List.of("Да", "Не")))));

        visibleColumn.setOnEditCommit(event -> {
            Category category = event.getRowValue();
            String newVisibility = event.getNewValue(); // Assuming the new value is a category name
            Category isCategory = productService.findCategoryByName(category.getName());

            if (isCategory != null) {
                category.setVisible(newVisibility.equals("Да"));
                productService.addCategory(category);
            } else {
                printMessage("Категоријата не е пронајдена!", false);
            }

            renderTables();
        });
    }

    public String getSelectedRadioButton() {
        Toggle selectedToggle = toggleGroup.getSelectedToggle();
        if (selectedToggle != null)
            return selectedToggle.equals(foodRadio) ? "Храна" : "Пијалоци";
        return null;
    }

    public void printMessage(String message, Boolean color) {
        MessagePrinter.printMessage(messageLabel, message, color);
    }
}
