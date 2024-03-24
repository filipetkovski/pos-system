package system.pos.spring.service;

import system.pos.spring.enumm.TableRegion;
import system.pos.spring.model.Tables;

import java.util.List;

public interface TableService {
    List<Tables> getAll();

    Tables save(Tables table);

    void reset(Tables table);

    Tables isValidCode(Long number, TableRegion role);

    void delete(Tables tables);

    Tables findByNumber(Long number);
}
