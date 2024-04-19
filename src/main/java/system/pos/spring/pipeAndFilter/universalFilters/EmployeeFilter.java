package system.pos.spring.pipeAndFilter.universalFilters;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import system.pos.spring.pipeAndFilter.Filter;
import system.pos.spring.model.Order;

import java.util.List;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class EmployeeFilter implements Filter<String, Order> {

    @Override
    public List<Order> execute(String name, List<Order> dbOrders) {
        if(name != null) {
            return  dbOrders.stream().filter(db -> db.getEmployee().getName().equals(name)).collect(Collectors.toList());
        }
        return dbOrders;
    }
}