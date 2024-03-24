package system.pos.spring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import system.pos.spring.enumm.Payment;
import system.pos.spring.enumm.Status;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long code;
    @CreationTimestamp
    @JoinColumn(name = "created_on")
    LocalDateTime createdOn;
    Integer number_people;
    Integer table_number;
    Integer price;
    @Enumerated(EnumType.STRING)
    Status status;
    @Enumerated(EnumType.STRING)
    Payment payment_method;
    @ManyToOne
    @JoinColumn(name = "employee", referencedColumnName = "code")
    Employee employee;
    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
    List<AddedProduct> products;
}
