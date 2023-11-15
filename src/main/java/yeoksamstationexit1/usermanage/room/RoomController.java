package yeoksamstationexit1.usermanage.room;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yeoksamstationexit1.usermanage.global.util.ErrorHandler;
import yeoksamstationexit1.usermanage.room.participant.dto.ChangeLocalStartRequestDTO;
import yeoksamstationexit1.usermanage.room.roomDTO.request.*;
import yeoksamstationexit1.usermanage.room.roomDTO.response.RoomListDTO;
import yeoksamstationexit1.usermanage.user.UserEntity;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/user-manage/room")
@RestController
@Tag(name = "약속방", description = "약속방 관련 API 입니다.")
public class RoomController {

    private final RoomService roomService;
    private final RoomGetInService roomGetInService;
    private final ErrorHandler errorHandler;


    @Operation(description = "방 등록 메서드입니다.")
    @PostMapping()
    public ResponseEntity<?> createRoom(@AuthenticationPrincipal UserEntity user, @RequestBody CreateRoomDTO createRoomDTO) throws Exception {
        try {
            String uuid = roomService.registRoom(user, createRoomDTO);
            return new ResponseEntity<>(uuid, HttpStatus.OK);
        }
        catch(Exception e){
            return errorHandler.errorMessage(e);
        }
    }

    @GetMapping("/findmyroom")
    public ResponseEntity<?> findMyRoom(@AuthenticationPrincipal UserEntity user){

        List<RoomListDTO> roomList =  roomService.findPersonalRoom(user);
        return ResponseEntity.ok(roomList);
    }

    //기존 방에 입장한 인원이면 방에 입장한 인원정보와 roomId 반환
    // 처음 입장 시 participant등록 후 본인의 기간 내의 불가능한 날짜 반환. -> 프론트에서 다시 방의 기간 내에 불가능한 시간 받기
    @GetMapping("/enter/{uuid}")
    public ResponseEntity<?> enterRoom(@AuthenticationPrincipal UserEntity user,@PathVariable(value = "uuid") String uuid) throws Exception {
        if(user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("로그인이 필요합니다");
        }
        ResponseEntity<?> status  =  roomGetInService.getIn(user,uuid);

        return status;
    }

    @PostMapping("/submittime") // 본인 불가능한 날짜 제출
    public ResponseEntity<Void> submitMyImpossibleTime(@AuthenticationPrincipal UserEntity user, @RequestBody addDeleteDayListDTO dayList){
        //개인의 특정 방의 출발지 변경
        ResponseEntity<Void> response = roomService.updateMyImpossibleTime(user, dayList);

        return response;
    }

    @PostMapping("/getallroomtime") // 본인 불가능한 날짜 제출
    public ResponseEntity<Map<String,List<String>>> getAllRoomTime(@AuthenticationPrincipal UserEntity user,@RequestBody GetAllTimeInRoomDTO getAllTimeInRoomDTO){
        //개인의 특정 방의 출발지 변경


        ResponseEntity<Map<String, List<String>>> response = roomService.getAllpossTime(user,getAllTimeInRoomDTO);

        return response;
    }


    @PostMapping("sendFixDate")
    public ResponseEntity<Void> sendFixDate(@AuthenticationPrincipal UserEntity user,@RequestBody @Valid FixDateDTO fixDate){
        //개인의 불가능한 시간 수정
        ResponseEntity<Void> response = roomService.saveFixDate(fixDate);

        return response;
    }

    @PostMapping("/changestartpoint") // 해당 약속의 출발지점 수정
    public ResponseEntity<String> changeSpecificStartPoint(@AuthenticationPrincipal UserEntity user, @Valid @RequestBody ChangeLocalStartRequestDTO changeLocalStartRequestDTO){
        //개인의 특정 방의 출발지 변경

        ResponseEntity<String> response = roomService.changeLocalStartPoint(user, changeLocalStartRequestDTO);

        return response;
    }


    //강제로 다음으로 넘어가는 것
    //테스트 완료
    //최적화 미완료X
    @PostMapping("/nextforce") // 해당 약속의 강제 다음
    public ResponseEntity<?> nextForce(@AuthenticationPrincipal UserEntity user,@RequestBody RoomIdDTO roomIdDTO){
        //개인의 특정 방의 출발지 변경
        ResponseEntity<?> response = roomService.nextClick(roomIdDTO.getRoomId());

        return response;
    }

    @PostMapping("/nameChange") // 해당 방의 이름 변경
    public ResponseEntity<Void> nameChange(@AuthenticationPrincipal UserEntity user,@RequestBody NameChangeDTO nameChangeDTO){
        //개인의 특정 방의 출발지 변경
        ResponseEntity<Void> response = roomService.changeName(user,nameChangeDTO);

        return response;
    }

    @PostMapping("/setstation") // 해당 방의 fix station
    public ResponseEntity<Void> setstation(@AuthenticationPrincipal UserEntity user,@RequestBody SetStationDTO setStationDTO){
        //개인의 특정 방의 출발지 변경
        ResponseEntity<Void> response = roomService.setStation(user,setStationDTO);


        return response;
    }

    @PostMapping("/setplace") // 해당 방의 fix place
    public ResponseEntity<?> setplace(@AuthenticationPrincipal UserEntity user,@RequestBody SetPlaceDTO setPlaceDTO){
        //개인의 특정 방의 출발지 변경
        ResponseEntity<?> response = roomService.setPlace(user,setPlaceDTO);

        return response;
    }


    @PostMapping("/tolivemap")
    public ResponseEntity<Void> tolivemap(@RequestBody UUIDReqDTO uuidReqDTO){
        //개인의 특정 방의 출발지 변경
        ResponseEntity response = roomService.changeToLiveMap(uuidReqDTO.getRoomCode());

        return response;
    }




}
