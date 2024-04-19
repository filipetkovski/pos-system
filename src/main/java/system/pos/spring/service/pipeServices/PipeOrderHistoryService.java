package system.pos.spring.service.pipeServices;

import system.pos.spring.model.Order;

import java.util.List;

public interface PipeOrderHistoryService {
    List<Order> filter(String name, String tableNumber, String number, String price, String status, String payment,  String date);
}
