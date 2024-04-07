package system.pos.spring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String employee;
    String product;
    Long order_code;
    Integer table_number;
    String status;
    Integer quantity;
    @Column(name = "created_on", updatable = false)
    LocalDateTime created_on;
}
