package system.pos.spring.service.impl;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import system.pos.spring.enumm.TableRegion;
import system.pos.spring.model.Tables;
import system.pos.spring.repository.TableRepository;
import system.pos.spring.service.TableService;

import java.util.List;

@Service
public class TableServiceImpl implements TableService {
    private final TableRepository tableRepository;

    public TableServiceImpl(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    @Override
    public List<Tables> getAll() {
        return tableRepository.findAll(Sort.by(Sort.Direction.ASC, "number"));
    }

    @Override
    public void reset(Tables table) {
        table.setOrder(null);
        save(table);
    }

    @Override
    public Tables isValidCode(Long number, TableRegion role) {
        if(!tableRepository.existsByNumber(number))
            return save(new Tables(number,role,null));
        return null;
    }

    @Override
    public Tables findByNumber(Long number) {
        return tableRepository.findById(number).orElse(null);
    }

    @Override
    public Tables save(Tables table) {
        return tableRepository.save(table);
    }

    @Override
    public void delete(Tables tables) {
        tableRepository.delete(tables);
    }

}
