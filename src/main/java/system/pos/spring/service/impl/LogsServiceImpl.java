package system.pos.spring.service.impl;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import system.pos.spring.model.Log;
import system.pos.spring.repository.LogsRepository;
import system.pos.spring.service.LogsService;

import java.util.List;

@Service
public class LogsServiceImpl implements LogsService {
    private final LogsRepository orderLogsRepository;

    public LogsServiceImpl(LogsRepository orderLogsRepository) {
        this.orderLogsRepository = orderLogsRepository;
    }

    @Override
    public List<Log> findAll() {
        return orderLogsRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }
}
