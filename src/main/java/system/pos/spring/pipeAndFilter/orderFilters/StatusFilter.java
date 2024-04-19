package system.pos.spring.pipeAndFilter.orderFilters;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import system.pos.spring.enumm.Status;
import system.pos.spring.model.Order;
import system.pos.spring.pipeAndFilter.Filter;

import java.util.List;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class StatusFilter implements Filter<String, Order> {
    @Override
    public List<Order> execute(String status, List<Order> dbOrders) {
        if(status != null) {
            return  dbOrders.stream().filter(db -> db.getStatus().equals(Status.valueOf(status))).collect(Collectors.toList());
        }
        return dbOrders;
    }
}
