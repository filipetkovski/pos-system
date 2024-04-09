package system.pos.javafx.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Component;
import system.pos.javafx.stage.StageListener;
import system.pos.spring.enumm.Status;
import system.pos.spring.enumm.UserRole;
import system.pos.spring.model.*;
import system.pos.spring.service.AddedProductService;
import system.pos.spring.service.OrderService;
import system.pos.spring.service.ProductService;
import system.pos.spring.service.TableService;
import system.pos.spring.utility.CapitalizeFirstLetter;
import system.pos.spring.utility.MessagePrinter;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InsideTableController {
    private final ProductService productService;
    private final PaymentController paymentController;
    private final AuthenticationController authenticationController;
    private final OrderService orderService;
    private final StageListener listener;
    private final TableService tableService;
    private final AddedProductService addedProductService;

    public InsideTableController(ProductService productService, PaymentController paymentController, AuthenticationController controller, OrderService orderService, StageListener listener, TableService tableService, AddedProductService addedProductService) {
        this.productService = productService;
        this.paymentController = paymentController;
        this.authenticationController = controller;
        this.orderService = orderService;
        this.listener = listener;
        this.tableService = tableService;
        this.addedProductService = addedProductService;
    }

    private Tables table;
    private Employee employee;
    private final List<AddedProduct> products = new ArrayList<>();

    @FXML
    private Label tableNumber;
    @FXML
    private Label codeLabel;
    @FXML
    private BorderPane border;
    @FXML
    private ScrollPane scrollPanelProducts;
    @FXML
    private TableView<AddedProduct> tableView;
    @FXML
    private TableColumn<AddedProduct, String> productColumn;
    @FXML
    private TableColumn<AddedProduct, String> descriptionColumn;
    @FXML
    private TableColumn<AddedProduct, String> quantityColumn;
    @FXML
    private TextField searchInput;
    @FXML
    private Button searchButton;
    @FXML
    private Button payButton;
    @FXML
    private Button holdButton;
    @FXML
    private Label employeeName;
    @FXML
    private Label totalPrice;
    @FXML
    private Label dateLabel;
    @FXML
    private VBox categoryContainer;
    @FXML
    private FlowPane listProducts;
    @FXML
    private Button openTableButton;
    @FXML
    private Button closeTableButton;
    @FXML
    private Button exitButton;
    @FXML
    private Button submitProducts;
    @FXML
    private HBox searchHBox;
    @FXML
    private Button deleteOrderButton;
    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        if (table != null) {
            exitButton.setCancelButton(true); //ESC  buttons

            initTable();
            renderInTable(); //Print the products inside the orders in a table view
            isSubmitVisible(); //This is for the submit button, and it's only visible if you have a product in your local product list

            border.setOnKeyPressed(this::handleKeyPressed);
            openTableButton.setVisible(!hasOrder()); //Disable the button ones it's clicked and the table is open
            closeTableButton.setVisible(hasOrder() && employee.getE_role().equals(UserRole.МЕНАЏЕР)); //Visible to those who have opened the tables
            searchHBox.setVisible(hasOrder() && isValidEmployee());
            payButton.setVisible(hasOrder() && isValidEmployee());
            holdButton.setVisible(hasOrder() && isValidEmployee());

            print(); //Print details about table and employee
        }
    }

    public void initData(Tables table, Employee employee) {
        this.table = table;
        this.employee = employee;
    }

    private void handleKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.SHIFT && !products.isEmpty()) {
            saveToOrder();
        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE && !searchInput.getText().isEmpty()) {
            searchInput.setText(searchInput.getText().substring(0, searchInput.getText().length() - 1));
        } else if(keyEvent.getCode() == KeyCode.DIGIT1) {
            showMenu(productService.getTopCategories());
        } else if(keyEvent.getCode() == KeyCode.F1) {
            pay();
        } else if(keyEvent.getCode() == KeyCode.F2) {
            holdOrder();
        } else if(keyEvent.getCode() == KeyCode.F3 && employee.getE_role().equals(UserRole.МЕНАЏЕР)) {
            closeTable();
        } else if(keyEvent.getCode() != KeyCode.ENTER) {
            searchInput.setText(searchInput.getText() + keyEvent.getText());
            searchButton.setDefaultButton(true);
        }
    }

    public void initTable() {
        tableView.getColumns().forEach(column -> {column.setReorderable(false);column.setResizable(false);});
        tableView.setFocusTraversable(false);

        productColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        quantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuantity().toString()));
        productColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {  // Set custom cell factory for the productColumn to change text color for products from the second list
                super.updateItem(item, empty);
                if (!empty) {
                    AddedProduct product = getTableView().getItems().get(getIndex());
                    setTextFill(products.contains(product) ? Color.web("#27ae60") : Color.BLACK);
                    setText(item);
                } else {
                    setText(null);
                }
            }
        });

        editTable();
    }

    public void print() {
        tableNumber.setText("Маса: " + table.getNumber() + " - " + CapitalizeFirstLetter.capitalizeFirstLetter(table.getRegion().toString()));
        if(table.getOrder() == null) {
            employeeName.setText("Ќелнер: " + CapitalizeFirstLetter.capitalizeFirstLetter(employee.getName()));
        } else {
            Order order = table.getOrder();
            Employee tableEmployee = table.getOrder().getEmployee();

            employeeName.setText("Ќелнер: " + tableEmployee.getName());
            codeLabel.setText("Код: " + order.getCode().toString());
            dateLabel.setText("Време: " + order.getCreatedOn().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

            resetPrice();
            showMenu(productService.getTopCategories());
        }
    }


    public void openTable() { //This method will be called when the openTableButton is clicked
        table.setOrder(orderService.createNewOrder(employee, table.getNumber().intValue())); //Creating new Order and saving the order inside the table
        tableService.save(table);

        reload(); //Reload the scene so the new details can be rendered
    }

    public void closeTable() {  //This method will be called when the closeTableButton is clicked, and it will open the authentication fxml
        authenticationController.initData(this,table);
        listener.changeScene("/fxml/authentication.fxml");
    }

    public void cancelOrder() { //Canceling order, removing from the table and setting the Status to ОТКАЖАНА
        orderService.changeStatus(table.getOrder(), Status.ОТКАЖАНА);
        tableService.reset(table);

        listener.changeScene("/fxml/homePage.fxml"); //Go back to the home page
    }

    public void showMenu(List<Category> categories) { // Show the menu
        categoryContainer.getChildren().clear();

        categories.stream().filter(Category::isVisible)
                .forEach(category -> { //Getting the top level categories from the database
                    categoryContainer.getChildren().add(createCategoryButton(category));
                });
    }

    private void showCategories(Category category) {
        List<Category> categories = productService.getSubcategoriesForCategory(category.getId());

        if (categories.isEmpty()) {
            showProducts(category.getProducts());
        } else {
            showMenu(categories);

            Button backButton = new Button("Назад"); // Add a back button to navigate back to the previous categories
            backButton.setId("backButton");
            backButton.setOnAction(event -> showMenu(productService.getTopCategories()));
            categoryContainer.getChildren().add(backButton);

            categories.forEach(cat -> showProducts(cat.getProducts()));
        }
    }

    private void showProducts(List<Product> productList) {
        listProducts.getChildren().clear(); // Clear before rendering

        List<Node> focusableNodes = productList.stream().filter(Product::isVisible)
                .map(product -> {
                    VBox productBox = new VBox();
                    productBox.setId("productBox");
                    productBox.setFocusTraversable(true); // Make the VBox focusable

                    Label productName = new Label(product.getName());
                    productName.setId("productName");

                    ImageView productImage = createImage(product);
                    productBox.getChildren().addAll(productImage, productName);
                    productBox.setStyle("-fx-border-color: transparent; -fx-border-width: 2;");
                    productBox.setUserData(product);

                    productBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        productBox.setStyle(isNowFocused ? "-fx-border-color: #f62b2b; -fx-border-width: 2;" : "-fx-border-color: transparent; -fx-border-width: 2;");
                    });

                    return productBox;
                })
                .collect(Collectors.toList());

        listProducts.getChildren().addAll(focusableNodes); // Add all focusable nodes to the listProducts container

        if (!focusableNodes.isEmpty()) // Set initial focus to the first focusable node
            focusableNodes.get(0).requestFocus();

        listProducts.setOnKeyPressed(event -> { // Set a key event handler for the entire listProducts container
            if (event.getCode() == KeyCode.TAB) {
                Node currentNode = listProducts.getScene().getFocusOwner();
                if (currentNode != null) {
                    int currentIndex = focusableNodes.indexOf(currentNode);
                    if (currentIndex != -1) {
                        int nextIndex = event.isShiftDown() ?
                                (currentIndex - 1 + focusableNodes.size()) % focusableNodes.size() :
                                (currentIndex + 1) % focusableNodes.size();
                        focusableNodes.get(nextIndex).requestFocus();
                        scrollProductIntoView(focusableNodes.get(nextIndex)); // Scroll the focused product into view
                        event.consume(); // Consume the event so it doesn't propagate further
                    }
                }
            } else if (event.getCode() == KeyCode.BACK_QUOTE) {
                Node currentNode = listProducts.getScene().getFocusOwner();
                if (currentNode != null) {
                    int currentIndex = focusableNodes.indexOf(currentNode);
                    if (currentIndex != -1) {
                        int previousIndex = (currentIndex - 1 + focusableNodes.size()) % focusableNodes.size();
                        focusableNodes.get(previousIndex).requestFocus();
                        scrollProductIntoView(focusableNodes.get(previousIndex)); // Scroll the focused product into view
                        event.consume(); // Consume the event so it doesn't propagate further
                    }
                }
            } else if(event.getCode() == KeyCode.SPACE) {
                Node focusedNode = listProducts.getScene().getFocusOwner();
                if (focusableNodes.contains(focusedNode)) {
                    Product focusedProduct = (Product) focusedNode.getUserData();
                    if (focusedProduct != null) {
                        handleItemClick(focusedProduct); // Call the method with the focused product
                    }
                }
                event.consume();
            }
        });
    }

    private void scrollProductIntoView(Node productNode) {
        Bounds bounds = productNode.getBoundsInParent(); // Calculate the bounds of the product node relative to the scroll pane
        double minY = bounds.getMinY();
        double maxY = bounds.getMaxY();

        double viewportHeight = scrollPanelProducts.getViewportBounds().getHeight(); // Get the height of the viewport of the scroll pane
        double scrollY = scrollPanelProducts.getVvalue() * (listProducts.getHeight() - viewportHeight); // Calculate the current visible range in the scroll pane

        double scrollBottom = scrollY + viewportHeight;
        double nodeHeight = maxY - minY; // Calculate the height of the product node

        if (minY < scrollY) // Check if the product node is above the visible area
            scrollPanelProducts.setVvalue((minY - nodeHeight) / (listProducts.getHeight() - viewportHeight));
        else if (maxY > scrollBottom) // Check if the product node is below the visible area
            scrollPanelProducts.setVvalue((maxY - viewportHeight + nodeHeight) / (listProducts.getHeight() - viewportHeight));
    }

    public Button createCategoryButton(Category category) { //Creating category labels
        Button categoryButton = new Button(CapitalizeFirstLetter.capitalizeFirstLetter(category.getName()));
        categoryButton.setId("categoryLabel");
        categoryButton.setOnMouseClicked(event -> showCategories(category));
        return categoryButton;
    }

    private ImageView createImage(Product product) {
        ImageView productImage = new ImageView();

        productImage.setFitWidth(160);
        productImage.setFitHeight(160);
        productImage.setId(product.getCode().toString());
        productImage.setOnMouseClicked(event -> handleItemClick(product));
        productImage.setImage(product.getImage() != null ? new Image(new ByteArrayInputStream(product.getImage())) : null);

        return productImage;
    }

    public void searchProduct() {
        String text = searchInput.getText();
        if(text.isBlank()) {
            printMessage("Не внесовте ништо!",false);
        } else {
            List<Product> productList = productService.findBySearch(text.toLowerCase());
            if(!productList.isEmpty())
                showProducts(productList);
            else
                printMessage("Нема продукт/и со тоа име!",false);
        }
        searchInput.clear();
    }

    private void handleItemClick(Product product) {
        listProducts.getChildren().clear();

        VBox vBox = new VBox(); // Create input fields for quantity and description
        vBox.setStyle("-fx-spacing: 10px");

        TextField quantityInput = new TextField();
        TextField descriptionInput = new TextField();

        quantityInput.setId("quantityInput");
        quantityInput.setPromptText("Внеси количина");
        quantityInput.setText("1");
        quantityInput.setFocusTraversable(false);
        quantityInput.selectAll();

        descriptionInput.setId("descriptionInput");
        descriptionInput.setPromptText("Внеси опис");

        Button submitButton = new Button("Потврди"); // Create submit and back buttons
        searchButton.setDefaultButton(false);
        submitButton.setDefaultButton(true);
        submitButton.setId("submitButton");
        submitButton.setOnAction(event -> {
            String quantityText = quantityInput.getText(); // Retrieve quantity value from input field
            String description = descriptionInput.getText(); // Retrieve description value from input field

            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                printMessage("Невалидна операција! Внеси број.", false);
                return;
            }

            if(quantity > 0) { // Create an AddedProduct instance with the provided quantity, description, and product
                AddedProduct addProduct = products.stream().filter(pr -> pr.getProduct().getName().equals(product.getName())).findFirst().orElse(null);

                if(addProduct != null) {
                    products.remove(addProduct);
                    addProduct = addedProductService.updateLocalList(addProduct, quantity, description);

                } else {
                    addProduct = new AddedProduct(table.getOrder(),quantity,description,product);
                }

                products.add(addProduct); //Add in the local list
                renderInTable(); //Render the products form the local list and the database
                isSubmitVisible(); //Show submit button to save the local products in the database
            }

            exitButton.setCancelButton(true);
            showProducts(product.getCategory().getProducts()); // Navigate back to the previous category
        });

        Button backButton = new Button("Назад");
        backButton.setId("backButton");
        exitButton.setCancelButton(false);
        backButton.setCancelButton(true);
        backButton.setOnAction(event -> { // Show the category products from the open product
            showProducts(product.getCategory().getProducts());
            backButton.setCancelButton(false);
            exitButton.setCancelButton(true);
        });

        vBox.getChildren().addAll(quantityInput, descriptionInput, submitButton, backButton); // Add input fields and buttons to the category container
        listProducts.getChildren().add(vBox);

        Platform.runLater(quantityInput::requestFocus); //Focus on the quantity input
    }

    private void renderInTable() {
        Order tableOrder = table.getOrder();
        tableView.getItems().clear();
        tableView.getItems().addAll(tableOrder != null ? tableOrder.getProducts() : Collections.emptyList());
        tableView.getItems().addAll(products); //Add products from the local list in the table view
    }

    public void saveToOrder() { //Save order in database
        orderService.saveProduct(table.getOrder(), products); //Save order
        table = tableService.save(table); //Save the order in the table
        products.clear(); //Reset the local list

        resetPrice();
        renderInTable();
        isSubmitVisible();
        printMessage("Успешно извршена нарачка.", true);
    }

    public void pay() { //Activate when payButton is clicked
        Order order = table.getOrder();
        if(order != null && order.getPrice() > 0) {
            paymentController.initData(this,order,employee);
            listener.changeScene("/fxml/payment.fxml");
        } else {
            printMessage("Нарачката е празна!", false);
        }
    }

    public void holdOrder() { //Hold the order payment
        Order order = table.getOrder();
        if(order.getPrice() > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Потрвди диалог");
            alert.setHeaderText("Дали сте сигурни да ја ставите нарачката на чекање?");

            alert.showAndWait().ifPresent(response -> { // Handle the user's response
                if (response == ButtonType.OK) {
                    orderService.changeStatus(order, Status.НЕ_ПЛАТЕНА);
                    tableService.reset(table);
                    listener.changeScene("/fxml/homePage.fxml");
                }
            });
        } else {
            printMessage("Нарачката е празна!", false);
        }
    }

    public void deleteOrderTable() {
        TableView.TableViewSelectionModel<AddedProduct> selectionModel = tableView.getSelectionModel();

        if(selectionModel.isEmpty()) {
            printMessage("Мора да селектирате нарачка!", false);
            return;
        }

        ObservableList<Integer> list = selectionModel.getSelectedIndices();
        Integer[] selectedIndices = new Integer[list.size()];
        selectedIndices = list.toArray(selectedIndices);

        Arrays.sort(selectedIndices);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Потврди Диалог");
        alert.setHeaderText("Дали сте сигурни да избришете нарачка?");

        Integer[] finalSelectedIndices = selectedIndices;
        alert.showAndWait().ifPresent(response -> { // Handle the user's response
            if (response == ButtonType.OK) {
                for (Integer finalSelectedIndex : finalSelectedIndices) {
                    selectionModel.clearSelection(finalSelectedIndex);
                    AddedProduct addedProduct = tableView.getItems().get(finalSelectedIndex);
                    if (products.contains(addedProduct)) { //If the selected product is from the local list, remove it
                        products.remove(addedProduct);
                    } else {
                        addedProductService.deleteAddedProduct(addedProduct); //If the selected product is from the database, delete it
                        Order order = orderService.findByCode(table.getOrder().getCode());
                        orderService.resetTotalPrice(order);
                        table.setOrder(order);
                        tableService.save(table);
                        resetPrice();
                    }
                    printMessage("Успешно избришан продукт!", true);
                    tableView.getItems().remove(finalSelectedIndex.intValue()); //Remove from the table view
                }

                isSubmitVisible(); //Show submit button and delete button if the method is true
            }
        });
    }

    public void editTable() {
        if(table.getOrder() != null && isValidEmployee()) {
            quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            quantityColumn.setOnEditCommit(event -> {
                AddedProduct addedProduct = event.getTableView().getItems().get(event.getTablePosition().getRow());

                //Compare the selected product with the local list
                products.stream().filter(product -> product.getProduct().equals(addedProduct.getProduct()))
                        .findFirst()
                        .ifPresent(existingProduct -> {
                            try {
                                addedProduct.setQuantity(Integer.parseInt(event.getNewValue()));
                                addedProduct.setId(existingProduct.getId());
                                products.remove(existingProduct);
                                if(addedProduct.getQuantity() > 0) {
                                    products.add(addedProduct);
                                } else {
                                    isSubmitVisible();
                                }
                            } catch (NumberFormatException e) {
                                printMessage("Невалидна оперцаија! Внеси број.", false);
                            }
                        });

                //Compare the selected product with the products in the database
                table.getOrder().getProducts().stream()
                        .filter(product -> product.getProduct().equals(addedProduct.getProduct()))
                        .findFirst()
                        .ifPresent(existingProduct -> printMessage("Не смеете да правите промена на внесени нарачки!", false));

                renderInTable();
                resetPrice();
            });

            descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            descriptionColumn.setOnEditCommit(event -> {
                AddedProduct addedProduct = event.getTableView().getItems().get(event.getTablePosition().getRow());
                addedProduct.setDescription(event.getNewValue());

                products.stream()
                        .filter(product -> product.getProduct().equals(addedProduct.getProduct()))
                        .findFirst()
                        .ifPresent(existingProduct -> {
                            addedProduct.setId(existingProduct.getId());
                            addedProduct.setDescription(event.getNewValue());
                            products.remove(existingProduct);
                            products.add(addedProduct);
                        });

                table.getOrder().getProducts().stream()
                        .filter(product -> product.getProduct().equals(addedProduct.getProduct()))
                        .findFirst()
                        .ifPresent(existingProduct -> printMessage("Не смеете да правите промена на внесени нарачки!", false));

                renderInTable();
            });
        }
    }

    public void back() { //Go back to the homepage and
        products.clear(); //Reset the local list
        listener.changeScene("/fxml/homePage.fxml");
    }
    public void reload() { //Reload the page after changes
        listener.changeScene("/fxml/insideTable.fxml");
    }
    public boolean hasOrder() { //Check if the table has an order inside
        return table.getOrder() != null;
    }
    public boolean isValidEmployee() { return table.getOrder().getEmployee().getName().equals(employee.getName()) || employee.getE_role().equals(UserRole.МЕНАЏЕР); }
    public void isSubmitVisible() { //If products != null, The submit button will be visible
        submitProducts.setVisible(!products.isEmpty());
        deleteOrderButton.setVisible(table.getOrder() != null && ((!table.getOrder().getProducts().isEmpty() || !products.isEmpty()) && isValidEmployee()));
    }
    public void resetPrice() {
        totalPrice.setText(table.getOrder().getPrice() + "ден");
    }
    public void printMessage(String message, Boolean color) { MessagePrinter.printMessage(messageLabel, message, color); }
}
