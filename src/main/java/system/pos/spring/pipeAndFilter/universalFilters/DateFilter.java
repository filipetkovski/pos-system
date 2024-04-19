package system.pos.spring.pipeAndFilter.universalFilters;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import system.pos.spring.model.Order;
import system.pos.spring.pipeAndFilter.Filter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

@Component
@NoArgsConstructor
public class DateFilter implements Filter<String, Order>  {
    @Override
    public List<Order> execute(String dateText, List<Order> dbOrders) {
        if(dateText != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(dateText, formatter);
            return dbOrders.stream()
                    .filter(db -> !db.getCreatedOn().toLocalDate().isAfter(localDate))
                    .sorted(Comparator.comparing(Order::getCreatedOn).reversed())
                    .collect(Collectors.toList());
        }
        return dbOrders;
    }
}
