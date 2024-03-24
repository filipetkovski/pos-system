package system.pos.spring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import system.pos.spring.enumm.EmployeeStatus;
import system.pos.spring.enumm.UserRole;

import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="employee")
public class Employee {
    @Id
    Long code;
    String name;
    @Enumerated(EnumType.STRING)
    EmployeeStatus status;
    @Enumerated(EnumType.STRING)
    UserRole e_role;
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Order> orders;
}
