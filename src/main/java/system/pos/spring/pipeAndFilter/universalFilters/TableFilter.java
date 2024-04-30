package system.pos.spring.pipeAndFilter.universalFilters;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import system.pos.spring.model.Order;
import system.pos.spring.pipeAndFilter.Filter;

import java.util.List;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class TableFilter implements Filter<String, Order> {
    @Override
    public List<Order> execute(String table, List<Order> dbOrders) {
        if(table != null) {
            return  dbOrders.stream().filter(db -> db.getTable_number() == Integer.parseInt(table)).collect(Collectors.toList());
        }
        return dbOrders;
    }
}
