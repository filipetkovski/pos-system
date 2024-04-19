package system.pos.spring.pipeAndFilter.orderFilters;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import system.pos.spring.model.Order;
import system.pos.spring.pipeAndFilter.Filter;

import java.util.List;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class PriceFilter implements Filter<String, Order> {
    @Override
    public List<Order> execute(String priceText, List<Order> dbOrders) {
        if(priceText != null) {
            int price = Integer.parseInt(priceText.substring(2, priceText.length() - 3));
            char firstLetter = priceText.charAt(0);
            return  dbOrders.stream().filter(db -> firstLetter == '<' ? db.getPrice() < price : db.getPrice() > price).collect(Collectors.toList());

        }
        return dbOrders;
    }
}
