package yeoksamstationexit1.usermanage.room;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yeoksamstationexit1.usermanage.room.enumClass.Processivity;
import yeoksamstationexit1.usermanage.room.participant.ParticipantEmbededId;
import yeoksamstationexit1.usermanage.room.participant.ParticipantEntity;
import yeoksamstationexit1.usermanage.room.participant.ParticipantRepository;
import yeoksamstationexit1.usermanage.room.roomDTO.response.ReturnRoomDTO;
import yeoksamstationexit1.usermanage.user.UserEntity;
import yeoksamstationexit1.usermanage.user.UserRepository;
import yeoksamstationexit1.usermanage.user.entity.FixCalendarEntity;
import yeoksamstationexit1.usermanage.user.repository.FixCalendarRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoomGetInService {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;
    private final FixCalendarRepository fixCalendarRepository;
    private final ObjectMapper objectMapper;


    //일단 방의 진행도를 보고 insubmission상태가 아니면 다시 보내기
    public ResponseEntity<?> getIn(UserDetails token, Long roomId) {


        /** To do
         * 조인을 통해서 repository의 접근 줄이기
         * InSubmissiondl 이 아니면 굳이 나눌 필요 없이 일단 다 보내주고
         프론트에서 진행도 따라서 보여줄 정도 구분.

         */

        //

        //방 정보와 방의 진행도 가져오기
        RoomEntity roomEntity = roomRepository.findById(roomId).get();
        Processivity roomProgressity = roomEntity.getProcessivity();

        //방의 참가자 목록 받기
        List<ParticipantEntity> memberList = participantRepository.findByIdRoomId(roomId).get();
        List<ReturnRoomDTO> returnRoomDTOList = memberList.stream()
                .map(participantEntity -> {
                    ReturnRoomDTO returnRoomDTO = new ReturnRoomDTO();
                    returnRoomDTO.setNickname(participantEntity.getUser().getNickname());
                    returnRoomDTO.setPersonalProgress(participantEntity.getProgress());
                    returnRoomDTO.setRoomName(participantEntity.getRoomName());
                    return returnRoomDTO;
                })
                .collect(Collectors.toList());

        //해당 방의 유저의 해당 방정보 가져오기
        UserEntity existUser = userRepository.findByEmail(token.getUsername()).get();
        ParticipantEmbededId id = new ParticipantEmbededId(existUser.getId(), roomId);
        Optional<ParticipantEntity> participantOp = participantRepository.findById(id);
        ParticipantEntity participantEntity;
        System.out.println(123123);
        System.out.println(123123);
        System.out.println(123123);
        if (roomProgressity == Processivity.InSubmission) {
            //아직 참여하지 않은 경우 참여시키기
            if(participantOp.isEmpty()){

                participantEntity = new ParticipantEntity(id);

                participantEntity.setRoomName(roomEntity.getRoomName());
                participantEntity.setUser(existUser);
                participantEntity.setRoom(roomEntity);
                participantRepository.save(participantEntity);
            }
            else{
                System.out.println("아닐경우");
                participantEntity = participantOp.get();
                System.out.println("아닐경우");
            }

            //본인 불가능한 시간
            //멤버 리스트 받기
            //방정보
            //제출자 수
            //방의 진행도
            //나의 진행도
            System.out.println("여기는 1번째 단계");
            System.out.println("여기는 1번째 단계");
            System.out.println("여기는 1번째 단계");
            System.out.println("여기는 1번째 단계");
            List<FixCalendarEntity> myImpossibleList = getUserImpossibleTimeAndDeletePastDay(existUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("memberList", returnRoomDTOList);
            response.put("fixCalendarList", myImpossibleList);
            response.put("roomProgress", roomEntity.getProcessivity());
            response.put("submitNumber", roomEntity.getSubmitNumber());
            response.put("myProgress", participantEntity.getProgress());
            return ResponseEntity.accepted().body(response); // 불가능한 시간 반환 받아서 프론트에서 수정 한 후 저장

        } else {


            //방이 진행중이면 들어올 수 없음
            if(participantOp.isEmpty()){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("이미 진행 시작한 방");
            }
            else{
                participantEntity = participantOp.get();
            }


            Map<String, Object> response = new HashMap<>();
            response.put("memberList", returnRoomDTOList);
            response.put("roomId", roomId);
            response.put("roomProgress", roomEntity.getProcessivity());
            response.put("submitNumber", roomEntity.getSubmitNumber());
            response.put("myProgress", participantEntity.getProgress());

            if (roomProgressity == Processivity.RecommendDay) {
                //InSubmission에서 RecommendDay으로 넘어올 때 날짜를 넣어줄 것이고
                //이 값을 일단 반환해줌.
                //어떤 유저가 불가능하다고 말하면 이를 해당 유저의 불가능한 시간에 넣고
                //fixday다시 계산해서 넣기
                //프론트에서 동의 누를 시 제출자가1씩 증가
                //다음으로 넘어가기


                List<FixCalendarEntity> myImpossibleList = getUserImpossibleTimeAndDeletePastDay(existUser.getId());
                response.put("fixCalendarList", myImpossibleList);
                response.put("fixDay", roomEntity.getFixDay());

            } else if (roomProgressity == Processivity.RecommendStation) {
                response.put("myStartPoint", participantEntity.getStartpoint());
                response.put("fixStation", roomEntity.getFixStation());

            } else if (roomProgressity == Processivity.RecommendPlace) {
                response.put("myStartPoint", participantEntity.getStartpoint());
                response.put("fixPlace", roomEntity.getFixPlace());
            }
            return ResponseEntity.ok(response);
        }

    }



    //유저의 과거 불가능한 날짜 삭제
    //유저의 불가능한 날짜 반환
    public List<FixCalendarEntity> getUserImpossibleTimeAndDeletePastDay(Long userId){
        List<FixCalendarEntity> fixCalendarList = fixCalendarRepository.findByIdUserIdOrderByImpossibleDateAsc(userId).get();
        LocalDate now = LocalDate.now();
        Iterator<FixCalendarEntity> iterator = fixCalendarList.iterator();
        //실제 삭제되는지 확인 필요
        //실제 삭제되는지 확인 필요
        //실제 삭제되는지 확인 필요
        while (iterator.hasNext()) {
            FixCalendarEntity entity = iterator.next();
            if (entity.getId().getImpossibleDate().isBefore(now)) {
                iterator.remove();
            }
        }
        return fixCalendarList;
    }





}
