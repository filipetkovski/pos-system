package system.pos.spring.service;

import system.pos.spring.enumm.Payment;
import system.pos.spring.enumm.Status;
import system.pos.spring.model.AddedProduct;
import system.pos.spring.model.Employee;
import system.pos.spring.model.Order;

import java.util.List;

public interface OrderService {
    void saveProduct(Order order, List<AddedProduct> products);

    Order createNewOrder(Employee employee, int tableNumber);

    void delete(Order order);

    void changeStatus(Order order, Status status);

    void save(Order order);

    List<Order> findAll();

   void resetTotalPrice(Order order);

    Order findByCode(Long code);

    void payOrder(Order order, Payment pay, int number);

    void resetDiscount(Order order);

    void makeDiscount(Order order, Integer price, Integer percent);

    List<Order> findLastHundred();
}
