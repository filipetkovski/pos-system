package system.pos.spring.pipeAndFilter.orderFilters;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import system.pos.spring.enumm.Payment;
import system.pos.spring.enumm.Status;
import system.pos.spring.model.Order;
import system.pos.spring.pipeAndFilter.Filter;

import java.util.List;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class PaymentFilter implements Filter<String, Order> {
    @Override
    public List<Order> execute(String payment, List<Order> dbOrders) {
        if(payment != null) {
            return  dbOrders.stream().filter(db -> db.getStatus().equals(Status.ЗАТВОРЕНА) && db.getPayment_method().equals(Payment.valueOf(payment))).collect(Collectors.toList());
        }
        return dbOrders;
    }
}