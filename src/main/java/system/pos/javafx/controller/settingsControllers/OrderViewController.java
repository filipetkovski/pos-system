package system.pos.javafx.controller.settingsControllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Component;
import system.pos.javafx.controller.SettingsController;
import system.pos.spring.model.AddedProduct;
import system.pos.spring.model.Order;
import system.pos.spring.utility.CapitalizeFirstLetter;

import java.time.format.DateTimeFormatter;


@Component
public class OrderViewController {

    public OrderViewController() {
    }

    @FXML
    private TableView<AddedProduct> addedProductTable;
    @FXML
    private TableColumn<AddedProduct, String> nameColumn;
    @FXML
    private TableColumn<AddedProduct, Integer> quantityColumn;
    @FXML
    private TableColumn<AddedProduct, String> descriptionColumn;
    @FXML
    private Label codeLabel;
    @FXML
    private Label tableLabel;
    @FXML
    private Label employeeLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label paymentLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label numberLabel;
    @FXML
    private Label discountLabel;
    @FXML
    private Label dateLabel;
    private Order order;

    @FXML
    public void initialize() {
        initTable();
        printDetails();
        printOrderHistory();
    }

    public void initData(Order order) {
        this.order = order;
    }

    public void printDetails() {
        codeLabel.setText("Код: " + order.getCode());
        tableLabel.setText(order.getTable_number().toString());
        employeeLabel.setText(CapitalizeFirstLetter.capitalizeFirstLetter(order.getEmployee().getName()));
        priceLabel.setText(order.getPrice() + "ден");
        statusLabel.setText(CapitalizeFirstLetter.capitalizeFirstLetter(order.getStatus().name()));
        discountLabel.setText(order.getDiscount() + "%");
        dateLabel.setText("Датум: " + order.getCreatedOn().format(DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy")));
        if(order.getPayment_method() != null) {
            paymentLabel.setText(CapitalizeFirstLetter.capitalizeFirstLetter(order.getPayment_method().name()));
            numberLabel.setText(order.getNumber_people().toString());
        } else {
            paymentLabel.setVisible(false);
            numberLabel.setVisible(false);
        }
    }

    public void initTable() {
        addedProductTable.getColumns().forEach(column -> {column.setReorderable(false);column.setResizable(false);});
        addedProductTable.setFocusTraversable(false);

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        quantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
    }

    public void printOrderHistory() {
        addedProductTable.getItems().clear();
        order.getProducts().forEach(orderLog -> addedProductTable.getItems().add(orderLog));
    }
}
