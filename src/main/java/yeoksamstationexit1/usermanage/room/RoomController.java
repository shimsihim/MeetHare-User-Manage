package yeoksamstationexit1.usermanage.room;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import yeoksamstationexit1.usermanage.room.participant.dto.ChangeLocalStartRequestDTO;
import yeoksamstationexit1.usermanage.room.roomDTO.request.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("api/room")
@RestController
@Tag(name = "약속방", description = "약속방 관련 API 입니다.")
public class RoomController {

    private final RoomService roomService;
    private final RoomGetInService roomGetInService;


    @Operation(description = "방 등록 메서드입니다.")
    @PostMapping()
    public ResponseEntity<?> createRoom(@AuthenticationPrincipal UserDetails token, @RequestBody CreateRoomDTO createRoomDTO) throws Exception {
        Long roomId = roomService.registRoom(token, createRoomDTO);

        // roomId를 ResponseEntity에 추가하여 응답
        return new ResponseEntity<>(roomId, HttpStatus.OK);
//        return ResponseEntity.status(HttpStatus.OK).body(roomId);
    }

    @GetMapping("/findmyroom")
    public ResponseEntity<?> findMyRoom(@AuthenticationPrincipal UserDetails token){
        ResponseEntity<?> response =  roomService.findPersonalRoom(token);
        return response;
    }

    //기존 방에 입장한 인원이면 방에 입장한 인원정보와 roomId 반환
    // 처음 입장 시 participant등록 후 본인의 기간 내의 불가능한 날짜 반환. -> 프론트에서 다시 방의 기간 내에 불가능한 시간 받기
    @GetMapping("/enter/{roomId}")
    public ResponseEntity<?> enterRoom(@AuthenticationPrincipal UserDetails token,@PathVariable(value = "roomId") Long roomId) throws Exception {
        System.out.println("들어옴");
        if(token == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("로그인이 필요합니다");
        }
        System.out.println("리턴 지나침");
        ResponseEntity<?> status  =  roomGetInService.getIn(token,roomId);

        return status;
    }

    @PostMapping("/submittime") // 본인 불가능한 날짜 제출
    public ResponseEntity<Void> submitMyImpossibleTime(@AuthenticationPrincipal UserDetails token, @RequestBody addDeleteDayListDTO dayList){
        //개인의 특정 방의 출발지 변경
        ResponseEntity<Void> response = roomService.updateMyImpossibleTime(token, dayList);

        return response;
    }

    @PostMapping("/getallroomtime") // 본인 불가능한 날짜 제출
    public ResponseEntity<Map<String,List<String>>> getAllRoomTime(@AuthenticationPrincipal UserDetails token,@RequestBody GetAllTimeInRoomDTO getAllTimeInRoomDTO){
        //개인의 특정 방의 출발지 변경


        ResponseEntity<Map<String, List<String>>> response = roomService.getAllImpossTime(token,getAllTimeInRoomDTO);

        return response;
    }


    @PostMapping("sendFixDate")
    public ResponseEntity<Void> sendFixDate(@RequestBody @Valid FixDateDTO fixDate){
        //개인의 특정 방의 출발지 변경
        ResponseEntity<Void> response = roomService.saveFixDate(fixDate);

        return response;
    }

    @PostMapping("/changestartpoint") // 해당 약속의 출발지점 수정
    public ResponseEntity<String> changeSpecificStartPoint(@AuthenticationPrincipal UserDetails token, @Valid @RequestBody ChangeLocalStartRequestDTO changeLocalStartRequestDTO){
        //개인의 특정 방의 출발지 변경
        ResponseEntity<String> response = roomService.changeLocalStartPoint(token, changeLocalStartRequestDTO);

        return response;
    }


    //강제로 다음으로 넘어가는 것
    //테스트 완료
    //최적화 미완료X
    @PostMapping("/nextforce") // 해당 약속의 강제 다음
    public ResponseEntity<?> nextForce(@AuthenticationPrincipal UserDetails token,@RequestBody RoomIdDTO roomIdDTO){
        System.out.println(roomIdDTO.getRoomId());
        //개인의 특정 방의 출발지 변경
        ResponseEntity<?> response = roomService.nextClick(roomIdDTO.getRoomId());

        return response;
    }






}
