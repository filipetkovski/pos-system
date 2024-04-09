package system.pos.javafx.controller.settingsControllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;
import system.pos.spring.enumm.ProductType;
import system.pos.spring.model.Category;
import system.pos.spring.model.Product;
import system.pos.spring.service.ProductService;
import system.pos.spring.utility.CapitalizeFirstLetter;
import system.pos.spring.utility.ChoiceBoxTableCellFactory;
import system.pos.spring.utility.MessagePrinter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private RadioButton kujnaRadio;
    @FXML
    private TextField nameInput;
    @FXML
    private TextField priceInput;
    @FXML
    private ChoiceBox<String> categoryChoice;
    @FXML
    private Button submitButton;
    @FXML
    private Label messageLabel;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> priceColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;
    @FXML
    private TableColumn<Product, String> visibleColumn;
    @FXML
    private TableColumn<Product, Button> imageColumn;
    @FXML
    private TableColumn<Product, String> typeColumn;
    @FXML
    private Label imageLabel;
    private byte[] image;
    private String imgName;

    @FXML
    public void initialize() {
        submitButton.setDefaultButton(true);

        categoryChoice.getItems().addAll(productService.getSecondLevelCategories().stream().map(Category::getName).toList());

        initTable();
        renderTables();
    }

    public void initTable() {
        productTable.getColumns().forEach(column -> {column.setReorderable(false);column.setResizable(false);});
        productTable.setFocusTraversable(false);

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory().getName()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().toString()));
        visibleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isVisible() ? "TRUE" : "FALSE"));
        priceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrice().toString()));
        imageColumn.setCellFactory(param -> new TableCell<>() {
            private final Button button = new Button("Add");


            {
                // Set action handler for the button
                button.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    byte[] image = choseImage();
                    if (image != null) {
                        product.setImage(image);
                        productService.addProduct(product);
                        messageLabel.setTextFill(Color.web("#27ae60"));
                        printMessage("Успешно променета слика.", true);
                    } else {
                        printMessage("Неуспешна промена на слика!", false);
                    }
                });
            }

            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product product = getTableView().getItems().get(getIndex());
                    button.setStyle(product.getImage() != null ? "-fx-background-color: #27ae60; -fx-text-fill: white;" : "-fx-background-color: #f62b2b; -fx-text-fill: white;");
                    setGraphic(button);
                }
            }
        });

        editTable();
    }

    public void renderTables() {
        productTable.getItems().clear();
        productService.findAll().forEach(product -> productTable.getItems().add(product));
    }

    public void createProduct() {
        if (nameInput.getText().isBlank() || priceInput.getText().isBlank() || categoryChoice.getValue() == null || toggleGroup.getSelectedToggle() == null) {
            printMessage("Потполни ги сите податоци!", false);
        } else {
            String name = nameInput.getText();
            String priceText = priceInput.getText();

            int price;
            try {
                price = Integer.parseInt(priceText);
            } catch (NumberFormatException e) {
                printMessage("Невалидна операција! Внеси број.", false);
                priceInput.clear();
                return;
            }

            ProductType type = getSelectedRadioButton();
            String category = categoryChoice.getValue();

            Product product = productService.isValidName(name, price, type, category, image);
            if (product != null) {
                printMessage("Успешно додаден производ.", true);
                image = null;
                imageLabel.setText("");
                messageLabel.setTextFill(Color.web("#27ae60"));
                nameInput.clear();
                priceInput.clear();
                categoryChoice.setValue(null);

                toggleGroup.getToggles().forEach(toggle -> {
                    if (toggle.isSelected())
                        toggle.setSelected(false);
                });

                productTable.getItems().add(product);
            } else {
                printMessage("Постои продукт со исто име!", false);
                nameInput.clear();
            }
        }
    }

    public void deleteProduct() {
        TableView.TableViewSelectionModel<Product> selectionModel = productTable.getSelectionModel();

        if(selectionModel.isEmpty()) {
            printMessage("Мора да селектирате производ!", false);
            return;
        }

        ObservableList<Integer> list = selectionModel.getSelectedIndices();
        Integer[] selectedIndices = new Integer[list.size()];
        selectedIndices = list.toArray(selectedIndices);

        Arrays.sort(selectedIndices);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Потврди диалог");
        alert.setHeaderText("Дали сте сигурен да го избришете производот?");

        Integer[] finalSelectedIndices = selectedIndices;
        alert.showAndWait().ifPresent(response -> { // Handle the user's response
            if (response == ButtonType.OK) {
                for (Integer finalSelectedIndex : finalSelectedIndices) {
                    selectionModel.clearSelection(finalSelectedIndex);
                    Product product = productTable.getItems().get(finalSelectedIndex);
                    productService.delete(product); //If the selected table is from the database, delete it
                    productTable.getItems().remove(finalSelectedIndex.intValue()); //Remove from the table view
                }
            }
        });
    }

    public void editTable() {
        //Edit NAME column
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            List<Product> products = productService.findAll();
            Product product = event.getTableView().getItems().get(event.getTablePosition().getRow());

            products.stream()
                    .filter(element -> element.getCode().equals(product.getCode()))
                    .findFirst()
                    .ifPresent(existingProduct -> {
                        String name = CapitalizeFirstLetter.capitalizeFirstLetter(event.getNewValue());
                        if(!name.isBlank()) {
                            if(!productService.findByProductName(name)) {
                                product.setName(name);
                                productService.addProduct(product);
                            } else {
                                printMessage("Постои продукт со исто име!", false);
                            }
                        } else {
                            printMessage("Внеси име на производот!", false);
                        }
                    });

            renderTables();
        });

        //Edit PRICE column
        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        priceColumn.setOnEditCommit(event -> {
            List<Product> products = productService.findAll();
            Product product = event.getTableView().getItems().get(event.getTablePosition().getRow());

            products.stream() //Delete a row in the tables table
                    .filter(element -> element.getCode().equals(product.getCode()))
                    .findFirst()
                    .ifPresent(existingProduct -> {
                        String priceText = event.getNewValue();

                        int price;
                        if(!priceText.isBlank()) {
                            try {
                                price = Integer.parseInt(priceText);
                            } catch (NumberFormatException e) {
                                printMessage("Невалидна операција! Внеси број.", false);
                                return;
                            }

                            product.setPrice(price);
                            productService.addProduct(product);
                        } else {
                            printMessage("Цената не смее да е празна!", false);
                        }
                    });

            renderTables();
        });

        //Edit Category column
        categoryColumn.setCellFactory(column -> ChoiceBoxTableCellFactory
                .createCellFactory(new ChoiceBox<>(FXCollections.observableList(productService.getSecondLevelCategories().stream().map(Category::getName).toList()))));

        categoryColumn.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            Product isProduct = productService.findByCode(product.getCode());

            if (isProduct != null) {
                product.setCategory(productService.findCategoryByName(event.getNewValue())); // Assuming the new value is a category name
                productService.addProduct(product);
            } else {
                printMessage("Категоријата не е пронајдена!", false);
            }

            renderTables();
        });

        typeColumn.setCellFactory(column -> ChoiceBoxTableCellFactory
                .createCellFactory(new ChoiceBox<>(FXCollections.observableList(Arrays.stream(ProductType.values()).map(Enum::name).toList()))));

        typeColumn.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            Product isProduct = productService.findByCode(product.getCode());

            if (isProduct != null) {
                product.setType(ProductType.valueOf(event.getNewValue())); // Assuming the new value is a category name
                productService.addProduct(product);
            } else {
                printMessage("Категоријата не е пронајдена!", false);
            }

            renderTables();
        });

        visibleColumn.setCellFactory(column -> ChoiceBoxTableCellFactory
                .createCellFactory(new ChoiceBox<>(FXCollections.observableList(List.of("Да", "Не")))));

        visibleColumn.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            Product isProduct = productService.findByCode(product.getCode());

            if (isProduct != null) {
                product.setVisible(event.getNewValue().equals("Да")); // Assuming the new value is a category name
                productService.addProduct(product);
            } else {
                printMessage("Продуктот не е пронајден!", false);
            }

            renderTables();
        });
    }

    public void addImageButton() {
        image = choseImage();
        imageLabel.setText(imgName);
        imgName = "";
    }

    public byte[] choseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Избери слика");
        File selectedFile = fileChooser.showOpenDialog(null); // Show the file chooser dialog
        if (selectedFile != null) {
            try {
                FileInputStream fis = new FileInputStream(selectedFile);
                imgName = selectedFile.getName();
                byte[] imageData = new byte[(int) selectedFile.length()];
                fis.read(imageData);
                fis.close();

                return imageData;
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        return null;
    }

    public ProductType getSelectedRadioButton() {
        Toggle selectedToggle = toggleGroup.getSelectedToggle();
        if (selectedToggle != null)
            return selectedToggle.equals(kujnaRadio) ? ProductType.КУЈНА : ProductType.БАР;
        return null;
    }

    public void printMessage(String message, Boolean color) {
        MessagePrinter.printMessage(messageLabel, message, color);
    }
}
