package yeoksamstationexit1.usermanage.user.entity;

import yeoksamstationexit1.usermanage.user.dayType;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class FixCalendarId implements Serializable {

    private Long user;
    private dayType day;


}