package system.pos.spring.service.pipeServices.pipeImpl;

import org.springframework.stereotype.Service;
import system.pos.spring.pipeAndFilter.orderFilters.NumberFilter;
import system.pos.spring.pipeAndFilter.orderFilters.PaymentFilter;
import system.pos.spring.pipeAndFilter.orderFilters.PriceFilter;
import system.pos.spring.pipeAndFilter.orderFilters.StatusFilter;
import system.pos.spring.pipeAndFilter.Pipe;
import system.pos.spring.model.Order;
import system.pos.spring.pipeAndFilter.universalFilters.DateAfterFilter;
import system.pos.spring.pipeAndFilter.universalFilters.DateBeforeFilter;
import system.pos.spring.pipeAndFilter.universalFilters.EmployeeFilter;
import system.pos.spring.pipeAndFilter.universalFilters.TableFilter;
import system.pos.spring.service.OrderService;
import system.pos.spring.service.pipeServices.PipeOrderHistoryService;

import java.util.ArrayList;
import java.util.List;

@Service
public class PipeOrderHistoryServiceImpl implements PipeOrderHistoryService {
    private final Pipe<String, Order> pipeOrders;
    private final EmployeeFilter employeeFilter;
    private final TableFilter tableFilter;
    private final PriceFilter priceFilter;
    private final StatusFilter statusFilter;
    private final PaymentFilter paymentFilter;
    private final NumberFilter numberFilter;
    private final DateBeforeFilter dateBeforeFilter;
    private final DateAfterFilter dateAfterFilter;
    private final OrderService orderService;

    public PipeOrderHistoryServiceImpl(OrderService orderService) {
        this.pipeOrders = new Pipe<>();
        this.employeeFilter = new EmployeeFilter();
        this.tableFilter = new TableFilter();
        this.priceFilter = new PriceFilter();
        this.paymentFilter = new PaymentFilter();
        this.statusFilter = new StatusFilter();
        this.numberFilter = new NumberFilter();
        this.dateAfterFilter = new DateAfterFilter();
        this.dateBeforeFilter = new DateBeforeFilter();
        pipeOrders.addFilter(employeeFilter);
        pipeOrders.addFilter(numberFilter);
        pipeOrders.addFilter(tableFilter);
        pipeOrders.addFilter(priceFilter);
        pipeOrders.addFilter(statusFilter);
        pipeOrders.addFilter(paymentFilter);
        pipeOrders.addFilter(dateAfterFilter);
        pipeOrders.addFilter(dateBeforeFilter);
        this.orderService = orderService;
    }

    @Override
    public List<Order> filter(String name, String number, String tableNumber, String price, String status, String payment,String dateAfter, String dateBefore) {
        List<String> stringList = new ArrayList<>();
        stringList.add(name);
        stringList.add(number);
        stringList.add(tableNumber);
        stringList.add(price);
        stringList.add(status);
        stringList.add(payment);
        stringList.add(dateAfter);
        stringList.add(dateBefore);
        List<Order> orders = orderService.findAll();
        return pipeOrders.runFilters(stringList, orders);
    }
}
