package yeoksamstationexit1.usermanage.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.sql.Date;
import java.util.Objects;
import java.util.stream.Stream;

@Data
@Setter(AccessLevel.NONE)
public class UserUpdateDTO {

  private String nickname;
  private Date birthDay;
  private String phoneNum;
  private String home;

  public boolean isUserUpdateEmpty() {

    return Stream.of(nickname, birthDay, phoneNum,home)
        .allMatch(Objects::isNull);
  }
}