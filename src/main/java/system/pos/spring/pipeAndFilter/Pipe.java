package system.pos.spring.pipeAndFilter;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Pipe<T,U> {
    private List<Filter<T,U>> filters = new ArrayList<>();

    public void addFilter(Filter<T,U> filter){
        filters.add(filter);}

    public List<U> runFilters(List<T> input, List<U> data) {
        List<U> newData = data;
        int counter = -1;
        for (Filter<T,U> filter: filters) {
            counter += 1;
            newData = filter.execute(input.get(counter),newData);
        }
        return newData;
    }
}