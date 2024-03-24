package system.pos.spring.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import system.pos.spring.enumm.Payment;
import system.pos.spring.enumm.Status;
import system.pos.spring.model.AddedProduct;
import system.pos.spring.model.Employee;
import system.pos.spring.model.Order;
import system.pos.spring.repository.OrderRepository;
import system.pos.spring.service.AddedProductService;
import system.pos.spring.service.OrderService;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final AddedProductService addedProductService;

    public OrderServiceImpl(OrderRepository orderRepository, AddedProductService addedProductService) {
        this.orderRepository = orderRepository;
        this.addedProductService = addedProductService;
    }

    @Transactional
    @Override
    public void saveProduct(Order order, List<AddedProduct> products) {
        List<AddedProduct> addedProducts = order.getProducts();
        if(!addedProducts.isEmpty()) {
            for (AddedProduct newProduct : products) {
                boolean found = false;
                for (AddedProduct existingProduct : addedProducts) {
                    //Check if the product already exist in the List
                    if (existingProduct.getProduct().getCode().equals(newProduct.getProduct().getCode())) {
                        // Update quantity and concatenate description if product already exists
                        addedProductService.updateProduct(existingProduct, newProduct);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // Add the new product if it doesn't exist in the addedProducts list
                    addedProducts.add(newProduct);
                    addedProductService.saveProduct(newProduct);
                }
            }
        } else {
            addedProducts = products;
            addedProductService.saveProducts(addedProducts);
        }

        //Get the total price from every product
        Integer totalPrice = addedProducts.stream().mapToInt(added -> added.getQuantity() * added.getProduct().getPrice()).sum();

        //Update products in the order object
        order.setProducts(addedProducts);
        order.setPrice(totalPrice);
        save(order);
    }

    @Override
    public void save(Order order) {
        orderRepository.save(order);
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAllByOrderByCreatedOnDesc();
    }

    @Override
    public void resetTotalPrice(Order order) {
        List<AddedProduct> addedProducts = order.getProducts();
        Integer totalPrice = addedProducts.stream().mapToInt(added -> added.getQuantity() * added.getProduct().getPrice()).sum();
        order.setPrice(totalPrice);
        save(order);
    }

    @Override
    public Order findByCode(Long code) {
        return orderRepository.findById(code).orElse(null);
    }

    @Override
    public void payOrder(Order order, Payment pay, int number) {
        order.setStatus(Status.ЗАТВОРЕНА);
        order.setPayment_method(pay);
        order.setNumber_people(number);
        orderRepository.save(order);
    }

    @Override
    public Order createNewOrder(Employee employee, int tableNumber) {
        Order order = new Order();
        order.setStatus(Status.ОТВОРЕНА);
        order.setPrice(0);
        order.setEmployee(employee);
        order.setProducts(new ArrayList<>());
        order.setNumber_people(0);
        order.setTable_number(tableNumber);
        return orderRepository.save(order);
    }

    @Override
    public void delete(Order order) {
        orderRepository.delete(order);
    }

    @Override
    public void changeStatus(Order order, Status status) {
        order.setStatus(status);
        orderRepository.save(order);
    }
}
