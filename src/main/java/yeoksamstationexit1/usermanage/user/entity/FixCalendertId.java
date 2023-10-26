package yeoksamstationexit1.usermanage.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;

@ToString
@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class FixCalendertId implements Serializable {

    @Column(name = "user_id")
    private Long userId;
    private LocalDate impossibleDate;

}
