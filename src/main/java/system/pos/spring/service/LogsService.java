package system.pos.spring.service;

import system.pos.spring.model.Log;

import java.util.List;

public interface LogsService {
    List<Log> findAll();
}
