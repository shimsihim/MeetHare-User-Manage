package yeoksamstationexit1.usermanage.room;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("api/room")
@RestController
@Tag(name = "약속방", description = "약속방 관련 API 입니다.")
public class RoomController {

    private final RoomService roomService;

    @Operation(description = "방 등록 메서드입니다.")
    @PostMapping()
    public ResponseEntity<String> createRoom(@AuthenticationPrincipal UserDetails token) throws Exception {

        roomService.registRoom(token);

        return ResponseEntity.ok("회원가입 성공!");
    }


}
