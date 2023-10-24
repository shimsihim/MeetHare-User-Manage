package yeoksamstationexit1.usermanage.room;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yeoksamstationexit1.usermanage.user.UserEntity;
import yeoksamstationexit1.usermanage.user.UserRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {
    private final UserRepository userRepository;


    public void registRoom(UserDetails token){
        UserEntity existUser = userRepository.findByEmail(token.getUsername()).get();

    }

}
