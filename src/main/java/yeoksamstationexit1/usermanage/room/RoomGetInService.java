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
import yeoksamstationexit1.usermanage.room.participant.dto.Response.ParticipantDTO;
import yeoksamstationexit1.usermanage.room.roomDTO.request.CreateRoomDTO;
import yeoksamstationexit1.usermanage.room.roomDTO.response.ReturnRoomDTO;
import yeoksamstationexit1.usermanage.user.UserEntity;
import yeoksamstationexit1.usermanage.user.UserRepository;
import yeoksamstationexit1.usermanage.user.entity.FixCalendarEntity;
import yeoksamstationexit1.usermanage.user.repository.FixCalendarRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public ResponseEntity<?> getIn(UserEntity existUser, String uuid) {

        System.out.println("uuid" + "  " + uuid);
        /** To do
         * 조인을 통해서 repository의 접근 줄이기
         * InSubmissiondl 이 아니면 굳이 나눌 필요 없이 일단 다 보내주고
         프론트에서 진행도 따라서 보여줄 정도 구분.

         */

        //

        //방 정보와 방의 진행도 가져오기
        Optional<RoomEntity> roomEntityOp = roomRepository.findByUUID(uuid);
        RoomEntity roomEntity;
        if(roomEntityOp.isPresent()){
            roomEntity = roomEntityOp.get();
        }
        //방 없으면 nocontent
        else{
            System.out.println("왜없니");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        //진행도
        Processivity roomProgressity = roomEntity.getProcessivity();
        
        //방의 진행도, 카테고리 , 제출자, 기간 시작일, 기간 종료일, 고정날, 고정역, 고정장소, 주인,총 참여인원
        ReturnRoomDTO returnRoom = ReturnRoomDTO.builder()
                .processivity(roomEntity.getProcessivity())
                .category(roomEntity.getCategory())
                .submitNumber(roomEntity.getSubmitNumber())
                .periodStart(roomEntity.getPeriodStart().toString())
                .periodEnd(roomEntity.getPeriodEnd().toString())
                .fixDay(roomEntity.getFixDay())
                .fixStation(roomEntity.getFixStation())
                .fixPlace(roomEntity.getFixPlace())
                .master(roomEntity.getMaster())
                .number(roomEntity.getNumber())
                .roomId(roomEntity.getRoomId())
                .build();

        //방의 참가자 목록 받기
        List<ParticipantEntity> memberList = participantRepository.findByIdRoomId(roomEntity.getRoomId()).get();




        List<ParticipantDTO> memberDtoList = memberList.stream()
                .map(participantEntity -> {
                        ParticipantDTO participantDTO = new ParticipantDTO(participantEntity.getUser().getId(),participantEntity.getUser().getNickname(),participantEntity.getStartpoint(),participantEntity.getLatitude(),participantEntity.getLongitude());
                    return participantDTO;
                })
                .collect(Collectors.toList());

        //해당 방의 유저의 해당 방정보 가져오기
        //나의 방정보 가져오기


        ParticipantEmbededId id = new ParticipantEmbededId(existUser.getId(), roomEntity.getRoomId());
        Optional<ParticipantEntity> participantOp = participantRepository.findById(id);
        ParticipantEntity participantEntity;


        //나의 불가능한 날짜 넣는 중
        if (roomProgressity == Processivity.InSubmission) {
            //아직 참여하지 않은 경우 참여시키기
            if(participantOp.isEmpty()){

                participantEntity = new ParticipantEntity(id);
                roomEntity.setNumber(roomEntity.getNumber()+1);
                participantEntity.setRoomName(roomEntity.getRoomName());
                participantEntity.setUser(existUser);
                participantEntity.setRoom(roomEntity);
                participantRepository.save(participantEntity);
            }
            //참여한 경우 나의 정보
            else{
                participantEntity = participantOp.get();
            }

            List<String> myImpossibleList = getUserImpossibleTimeAndDeletePastDay(existUser.getId(),roomEntity.getPeriodStart(),roomEntity.getPeriodEnd());

            Map<String, Object> response = new HashMap<>();
            response.put("memberList", memberDtoList);
            response.put("fixCalendarList", myImpossibleList);
            response.put("roominfo", returnRoom);
            response.put("myProgress", participantEntity.getProgress());
            response.put("myRoomName", participantEntity.getRoomName());
            return ResponseEntity.ok(response); // 불가능한 시간 반환 받아서 프론트에서 수정 한 후 저장

        } else {
            /**
             * InSubmission 이외의 경우
             */


            //방이 진행중이면 들어올 수 없음
            if(participantOp.isEmpty()){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("이미 진행 시작한 방");
            }
            else{
                participantEntity = participantOp.get();
            }

            //여기는 fixCalendarList가 없음 왜냐면 다음 분기마다 다 다른 정보 필요
            Map<String, Object> response = new HashMap<>();
            response.put("memberList", memberDtoList);
            response.put("roominfo", returnRoom);
            response.put("myProgress", participantEntity.getProgress());
            response.put("myRoomName", participantEntity.getRoomName());


            //여기는 가능한 날짜 보내기
            // 현재 roomservice로 fetch가 1번 더 보내짐
            if (roomProgressity == Processivity.RecommendDay) {
                //InSubmission에서 RecommendDay으로 넘어올 때 날짜를 넣어줄 것이고
                //이 값을 일단 반환해줌.
                //어떤 유저가 불가능하다고 말하면 이를 해당 유저의 불가능한 시간에 넣고
                //fixday다시 계산해서 넣기
                //프론트에서 동의 누를 시 제출자가1씩 증가
                //다음으로 넘어가기


                List<String> myImpossibleList = getUserImpossibleTimeAndDeletePastDay(existUser.getId(),roomEntity.getPeriodStart(),roomEntity.getPeriodEnd());
                response.put("fixCalendarList", myImpossibleList);

            }
            else if(roomProgressity == Processivity.SubmitStation){
                response.put("fixDay", roomEntity.getFixDay());
                response.put("myStarPoint", participantEntity.getStartpoint());
            }


            /**
             * 여기 하단부터는 이제 장민스네 필요한 것들 보내기
             */
            else if (roomProgressity == Processivity.RecommendStation) {

                response.put("memberList", memberDtoList);

            } else if (roomProgressity == Processivity.RecommendPlace) {
                response.put("memberList", memberDtoList);
                response.put("fixStation", roomEntity.getFixStation());
            }

            else{
                response.put("fixDay", roomEntity.getFixDay());
                response.put("fixStation", roomEntity.getFixStation());
                response.put("fixPlace", roomEntity.getFixPlace());

            }
            return ResponseEntity.ok(response);
        }

    }



    //유저의 과거 불가능한 날짜 삭제
    //유저의 불가능한 날짜 반환
    public List<String> getUserImpossibleTimeAndDeletePastDay(Long userId, LocalDate start,LocalDate end){

        List<FixCalendarEntity> fixCalendarList = fixCalendarRepository.findByIdUserIdOrderByImpossibleDateAsc(userId).get();

        Iterator<FixCalendarEntity> iterator = fixCalendarList.iterator();
        //실제 삭제되는지 확인 필요
        //실제 삭제되는지 확인 필요
        //실제 삭제되는지 확인 필요
        while (iterator.hasNext()) {
            FixCalendarEntity entity = iterator.next();
            if (entity.getId().getImpossibleDate().isBefore(start)) {
                iterator.remove();
            } else if (entity.getId().getImpossibleDate().isAfter(end)) {
                iterator.remove();
            }
        }



        return fixCalendarList.stream()
                .map(participantEntity->{
                    return participantEntity.getId().getImpossibleDate().toString();
                }).collect(Collectors.toList());
    }
}
