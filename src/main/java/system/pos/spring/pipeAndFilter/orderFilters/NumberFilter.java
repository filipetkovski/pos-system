package system.pos.spring.pipeAndFilter.orderFilters;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import system.pos.spring.model.Order;
import system.pos.spring.pipeAndFilter.Filter;

import java.util.List;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class NumberFilter implements Filter<String, Order> {
    @Override
    public List<Order> execute(String numberText, List<Order> dbOrders) {
        if(numberText != null) {
            int number = Integer.parseInt(numberText.substring(2));
            char firstLetter = numberText.charAt(0);
            return  dbOrders.stream().filter(db -> firstLetter == '<' ? db.getNumber_people() < number : db.getNumber_people() > number).collect(Collectors.toList());
        }
        return dbOrders;
    }
}
