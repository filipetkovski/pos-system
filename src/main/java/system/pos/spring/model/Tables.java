package system.pos.spring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import system.pos.spring.enumm.TableRegion;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tables")
public class Tables {
    @Id
    Long number;
    @Enumerated(EnumType.STRING)
    TableRegion region;
    @OneToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    Order order;
}
