package system.pos.spring.pipeAndFilter;

import java.util.List;

public interface Filter<T,U> {
    List<U> execute(T input, List<U> data);
}