package yeoksamstationexit1.usermanage.room;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import yeoksamstationexit1.usermanage.room.enumClass.Processivity;
import yeoksamstationexit1.usermanage.room.participant.dto.ChangeLocalStartRequestDTO;
import yeoksamstationexit1.usermanage.room.participant.ParticipantEmbededId;
import yeoksamstationexit1.usermanage.room.participant.ParticipantEntity;
import yeoksamstationexit1.usermanage.room.participant.ParticipantRepository;
import yeoksamstationexit1.usermanage.room.roomDTO.request.CreateRoomDTO;
import yeoksamstationexit1.usermanage.room.roomDTO.request.PlaceRequestDTO;
import yeoksamstationexit1.usermanage.room.roomDTO.request.addDeleteDayListDTO;
import yeoksamstationexit1.usermanage.room.roomDTO.response.PlaceResponseDTO;
import yeoksamstationexit1.usermanage.room.roomDTO.response.RoomListDTO;
import yeoksamstationexit1.usermanage.room.roomDTO.response.StationDTO;
import yeoksamstationexit1.usermanage.user.UserEntity;
import yeoksamstationexit1.usermanage.user.UserRepository;
import yeoksamstationexit1.usermanage.user.entity.FixCalendarEntity;
import yeoksamstationexit1.usermanage.user.entity.FixCalendertId;
import yeoksamstationexit1.usermanage.user.repository.FixCalendarRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;
    private final FixCalendarRepository fixCalendarRepository;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;


    //방 등록
    public Long registRoom(UserDetails token, CreateRoomDTO createRoomDTO) {
        UserEntity existUser = userRepository.findByEmail(token.getUsername()).get();

        RoomEntity room = new RoomEntity(createRoomDTO);

        roomRepository.save(room);

        ParticipantEmbededId id = new ParticipantEmbededId(existUser.getId(), room.getRoomId());
        ParticipantEntity participantEntity = new ParticipantEntity(id,createRoomDTO.getRoomName());


        /**
         * 현재 문제는 복합키만을 넣던가 
         * 아니면 entity들만을 넣으면 되어야 할 것 같은데 둘 다 넣고 있음 수정 해야 함
         */

        participantEntity.setUser(existUser);
        participantEntity.setRoom(room);
        participantEntity.setStartpoint(existUser.getHome());


        participantRepository.save(participantEntity);
        return room.getRoomId();

    }

    public ResponseEntity<?> findPersonalRoom(UserDetails token){
        UserEntity existUser = userRepository.findByEmail(token.getUsername()).get();

        List<ParticipantEntity> participateList = participantRepository.findByIdUserId(existUser.getId());
        List<RoomListDTO> roomList = participateList.stream()
                .map(participant -> {
                    RoomListDTO roomListDTO = new RoomListDTO();
                    roomListDTO.setRoomId(participant.getRoom().getRoomId());
                    roomListDTO.setRoomName(participant.getRoomName()); // ParticipantEntity에서 roomName 가져오기
                    return roomListDTO;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(roomList);
    }


    //날짜의 추가리스트와 삭제리스트를 받아서
    //추가 및 삭제

    /**
     * 현재 문제는 saveAll 시 여러번의 쿼리가 날아감
     * 또한 delete가 되지 않음
     * saveAll을 1번으로 처리한다고 해도
     * deleteAll또한 쿼리가 날아감
     * 
     * 방정보와 방의 유저정보를 가져올 때 join문 쓰기
     */
    @Transactional
    public ResponseEntity<String> updateMyImpossibleTime(UserDetails token, addDeleteDayListDTO dayList) {

        UserEntity existUser = userRepository.findByEmail(token.getUsername()).get();

        List<FixCalendarEntity> beforeList = fixCalendarRepository.findByIdUserIdOrderByImpossibleDateAsc(existUser.getId()).get();
        List<LocalDate> addList = dayList.getAddDayList();
        List<LocalDate> deleteList = dayList.getDeleteDayList();
        //정렬 전에 null체크
        if (!deleteList.isEmpty()) {
            Collections.sort(deleteList);
        }
        if (!addList.isEmpty()) {
            Collections.sort(addList);
        }


        for (int i = 0; i < addList.size(); ++i) {
            FixCalendertId id = new FixCalendertId(existUser.getId(), addList.get(i));
            FixCalendarEntity fixcal = new FixCalendarEntity(id, existUser);
            beforeList.add(fixcal);
        }


        fixCalendarRepository.saveAll(beforeList);
        // 불가능한 시간 업데이트
        List<FixCalendarEntity> newDeleteList = deleteList.stream()
                .map(date -> {
                    FixCalendertId id = new FixCalendertId(existUser.getId(), date);
                    return new FixCalendarEntity(id, existUser);
                })
                .collect(Collectors.toList());

        fixCalendarRepository.deleteAll(newDeleteList);



        //아직 제출하지 않은 경우 방 내의 제추자수 +1 과 나의 진행도 +1
        ParticipantEntity participantEntity = participantRepository.findByIdUserIdAndIdRoomId(existUser.getId(), dayList.getRoomId());

        if(participantEntity.getProgress() != Processivity.RecommendDay){
            RoomEntity roomEntity = roomRepository.findById(dayList.getRoomId()).get();
            participantEntity.setProgress(Processivity.RecommendDay);
            roomEntity.setSubmitNumber(roomEntity.getSubmitNumber()+1);
        }



        return ResponseEntity.ok("불가능한 시간 업데이트");
    }


    //나의 방에서의 출발지 수정을 했으므로 나의 진행도와 방의 제출자 +1
    //변변경은 확인했음
    public ResponseEntity<String> changeLocalStartPoint(UserDetails token, ChangeLocalStartRequestDTO changeLocalStartRequestDTO) {

        UserEntity existUser = userRepository.findByEmail(token.getUsername()).get();
        ParticipantEmbededId id = new ParticipantEmbededId(existUser.getId(), changeLocalStartRequestDTO.getRoomId());

        // 여기서 방의 유저가 아니면 빠꾸 시켜야 함
        ParticipantEntity participant = participantRepository.findById(id).get();
        participant.setPoint(changeLocalStartRequestDTO.getStartPoint());

        if(participant.getProgress()!=Processivity.RecommendPlace){
            participant.setProgress(Processivity.RecommendPlace);
            RoomEntity roomEntity = roomRepository.findById(changeLocalStartRequestDTO.getRoomId()).get();
            roomEntity.setSubmitNumber(roomEntity.getSubmitNumber()+1);

        }

        //일단 입장유저의 진행도를 보고
        // 진행도가 동일하면 출발지만 바꾸고
        //불일치하면 출발지와 개인진행도, 방제출자수,  바꾸고



        return ResponseEntity.ok("해당 약속의 출발지 설정");
    }

    public ResponseEntity<?> nextClick( Long roomId) {
        //방 정보 찾아와서
        RoomEntity roomEntity = roomRepository.findById(roomId).get();
        
//        수정 성공하면 숫자 0으로 바꿔야 함
//        roomEntity.setSubmitNumber(0);

        Processivity processivity = roomEntity.getProcessivity();
        List<ParticipantEntity> participantList = participantRepository.findByIdRoomId(roomId).get();

        Processivity nextProcessivity;
        if(processivity==Processivity.InSubmission){
            boolean hasFixDay = recommendDay(roomEntity,participantList);
            if(!hasFixDay){
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("가능한 날짜가 없습니다.");
            }
            nextProcessivity = Processivity.RecommendDay;
        }
        else {
            nextProcessivity = nextProcess(processivity, roomEntity, participantList);
        }
        changeParticipantsProcess(participantList,nextProcessivity);
        roomEntity.setProcessivity(nextProcessivity);


        return ResponseEntity.ok("Next");
    }

    public boolean recommendDay(RoomEntity roomEntity,List<ParticipantEntity> participantList){


            List<Long> userIdList = participantList.stream()
                    .map(participant -> participant.getUser().getId())
                    .collect(Collectors.toList());

            List<LocalDate> impossibleDates = fixCalendarRepository.findByUserList(userIdList);




            // 가능한 날짜를 찾아내는 방법:
            List<LocalDate> possibleDates = IntStream.range(0, roomEntity.getPeriodEnd().getDayOfYear() - roomEntity.getPeriodStart().getDayOfYear() + 1)
                    .mapToObj(roomEntity.getPeriodStart()::plusDays)
                    .filter(date -> !impossibleDates.contains(date))
                    .collect(Collectors.toList());

            if(possibleDates.size()==0){
                return false;
            }
            roomEntity.setFixDay(possibleDates.get(0));

            return true;
    }
    public Processivity nextProcess(Processivity process , RoomEntity roomEntity,List<ParticipantEntity> participantList){

        /**
         * 여기서 방의 기존 진행도에 따라서 api요청을 통해서 fix된 장소와 날짜 역 등을 db에저장
         */
        Processivity next = process;

        if(process == Processivity.RecommendDay){
            next = Processivity.RecommendStation;
        }


        else if(process == Processivity.RecommendStation){
            next = Processivity.RecommendPlace;

//            Mono<StationResponseDTO> response = webClient.post()
//                        .uri("/map/middlespot")
//                        .bodyValue(requestDTO)
//                        .retrieve()
//                        .bodyToMono(StationResponseDTO.class);

        }


        else if(process == Processivity.RecommendPlace){
            next = Processivity.Fix;

            PlaceRequestDTO placeRequestDTO = new PlaceRequestDTO();
            /**
             * 만들어놓기
             */
            List<Long>userList = participantList.stream()
                    .map(participant -> participant.getUser().getId())
                    .collect(Collectors.toList());

            int stationId = -1;
            try {
                stationId = (objectMapper.readValue(roomEntity.getFixStation(), StationDTO.class).getStationId());
            } catch (Exception e) {

            }

            placeRequestDTO = PlaceRequestDTO.builder()
                    .station_id(stationId)
                    .category(roomEntity.getCategory().toString())
                    .user_list(userList)
                    .final_time(0l)
                    .build();

            Mono<PlaceResponseDTO> response = webClient.post()
                .uri("/place/complex")
                .bodyValue(placeRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PlaceResponseDTO.class);

//            장소를 저장해야 함
//            roomEntity.setFixPlace();

        }
        return next;

    }

    public void changeParticipantsProcess(List<ParticipantEntity> memberList , Processivity process){

        for(ParticipantEntity participant :memberList ){
            participant.setProgress(process);
        }

    }



}
