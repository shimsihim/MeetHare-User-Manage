package yeoksamstationexit1.usermanage.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/user-manage/user")
@RestController
@Tag(name = "유저", description = "유저 관련 API 입니다.")
public class UserController {

    private final UserService userService;


    /**
     * Member 생성
     *
     * @return
     * @throws ParseException
     */


    @Operation(description = "유저 등록 메서드입니다.")
    @PostMapping("/sign-up")
    public ResponseEntity<String> createUser(@RequestBody UserSignUpDto userSignUpDto) throws Exception {

        userService.signUp(userSignUpDto);

        return ResponseEntity.ok("회원가입 성공!");
    }


    /**
     * Id에 해당하는 Member 조회
     *
     * @param id
     * @return
     */
    @Operation(description = "특정 유저 조회 메서드입니다.")
    @GetMapping("{id}")
    public ResponseEntity<UserEntity> getUser(@PathVariable("email") String email) {

        UserEntity user = userService.getUser(email);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}