package yeoksamstationexit1.usermanage.room;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import yeoksamstationexit1.usermanage.exception.NotInRoomException;
import yeoksamstationexit1.usermanage.room.enumClass.Processivity;
import yeoksamstationexit1.usermanage.room.participant.dto.ChangeLocalStartRequestDTO;
import yeoksamstationexit1.usermanage.room.participant.ParticipantEmbededId;
import yeoksamstationexit1.usermanage.room.participant.ParticipantEntity;
import yeoksamstationexit1.usermanage.room.participant.ParticipantRepository;
import yeoksamstationexit1.usermanage.room.roomDTO.ForAlertDTO;
import yeoksamstationexit1.usermanage.room.roomDTO.request.*;
import yeoksamstationexit1.usermanage.room.roomDTO.response.RoomListDTO;
import yeoksamstationexit1.usermanage.user.UserEntity;
import yeoksamstationexit1.usermanage.user.UserRepository;
import yeoksamstationexit1.usermanage.user.entity.FixCalendarEntity;
import yeoksamstationexit1.usermanage.user.entity.FixCalendertId;
import yeoksamstationexit1.usermanage.user.repository.FixCalendarRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


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
    public String registRoom(UserEntity existUser, CreateRoomDTO createRoomDTO) {


        RoomEntity room = new RoomEntity(createRoomDTO, existUser.getEmail(), UUID.randomUUID().toString());

        roomRepository.save(room);

        ParticipantEmbededId id = new ParticipantEmbededId(existUser.getId(), room.getRoomId());
        ParticipantEntity participantEntity = new ParticipantEntity(id, createRoomDTO.getRoomName());


        /**
         * 현재 문제는 복합키만을 넣던가 
         * 아니면 entity들만을 넣으면 되어야 할 것 같은데 둘 다 넣고 있음 수정 해야 함
         */

        participantEntity.setUser(existUser);
        participantEntity.setRoom(room);
        participantEntity.setStartpoint(existUser.getHome());


        participantRepository.save(participantEntity);
        return room.getUUID();

    }

    public List<RoomListDTO> findPersonalRoom(UserEntity existUser) {


        List<ParticipantEntity> participateList = participantRepository.findByIdUserId(existUser.getId());
        List<RoomListDTO> roomList = participateList.stream()
                .map(participant -> {
                    RoomListDTO roomListDTO = new RoomListDTO();
                    roomListDTO.setUUID(participant.getRoom().getUUID());
                    roomListDTO.setRoomName(participant.getRoomName()); // ParticipantEntity에서 roomName 가져오기
                    return roomListDTO;
                })
                .collect(Collectors.toList());
        return roomList;


    }


    //날짜의 추가리스트와 삭제리스트를 받아서
    //추가 및 삭제

    /**
     * 현재 문제는 saveAll 시 여러번의 쿼리가 날아감
     * 또한 delete가 되지 않음
     * saveAll을 1번으로 처리한다고 해도
     * deleteAll또한 쿼리가 날아감
     * <p>
     * 방정보와 방의 유저정보를 가져올 때 join문 쓰기
     */
    @Transactional
    public ResponseEntity<Void> updateMyImpossibleTime(UserEntity existUser, addDeleteDayListDTO dayList) {


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
            FixCalendarEntity fixcalendar = new FixCalendarEntity(id, existUser);
            beforeList.add(fixcalendar);
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
        ParticipantEntity participantEntity = participantRepository.findByIdUserIdAndIdRoomId(existUser.getId(), dayList.getRoomId()).get();

        if (participantEntity.getProgress() != Processivity.RecommendDay) {
            RoomEntity roomEntity = roomRepository.findById(dayList.getRoomId()).get();
            participantEntity.setProgress(Processivity.RecommendDay);
            roomEntity.setSubmitNumber(roomEntity.getSubmitNumber() + 1);

        }

        /**
         * 여기부터는 따로 update문 만들기! 왜냐하면 이걸 위해서 roomEntity를 불러오는게 손해 아닐까?
         */


        return ResponseEntity.ok().build();
    }


    //방 내의 모든 유저의 불가능한 날짜를 가져와서
    //모든 가능힌 날짜 반환
    public ResponseEntity<Map<String, List<String>>> getAllpossTime(UserEntity existUser, GetAllTimeInRoomDTO getAllTimeInRoomDTO) {

        Optional<List<ParticipantEntity>> userIdListOp = participantRepository.findByIdRoomId(getAllTimeInRoomDTO.getRoomId());
        List<Long> userIdList = userIdListOp.map(participantEntities -> {
            return participantEntities.stream()
                    .map(participantEntity -> participantEntity.getUser().getId())
                    .collect(Collectors.toList());
        }).orElse(Collections.emptyList());


        List<LocalDate> roomTimeList = fixCalendarRepository.findByUserListAndDateRange(userIdList, getAllTimeInRoomDTO.getPeriodStart(), getAllTimeInRoomDTO.getPeriodEnd());


        List<LocalDate> dateList = getAllTimeInRoomDTO.getPeriodStart()
                .datesUntil(getAllTimeInRoomDTO.getPeriodEnd().plusDays(1))
                .collect(Collectors.toCollection(ArrayList::new));


        List<String> excludedDates = dateList.stream()
                .filter(date -> !roomTimeList.contains(date))
                .map(date -> date.toString())
                .collect(Collectors.toList());


        Map<String, List<String>> response = new HashMap<>();
        response.put("dateList", excludedDates);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Void> rollback( GetAllTimeInRoomDTO getAllTimeInRoomDTO) {

        RoomEntity room = roomRepository.findById(getAllTimeInRoomDTO
                .getRoomId())
                .orElseThrow(()->new NotInRoomException("rollback서비스에서 방 정보 찾을 수 없음"));

        List<ParticipantEntity> members = participantRepository
                .findByIdRoomId(getAllTimeInRoomDTO.getRoomId())
                .orElseThrow(()->new NotInRoomException("rollback함수에서 참여자 정보 찾을 수 없음"));

        room.setProcessivity(Processivity.InSubmission);
        room.setSubmitNumber(0);
        room.setPeriodStart(getAllTimeInRoomDTO.getPeriodStart());
        room.setPeriodEnd(getAllTimeInRoomDTO.getPeriodEnd());
        for(ParticipantEntity participant : members){
            participant.setProgress(Processivity.InSubmission);
        }

        return ResponseEntity.ok().build();

    }

    //나의 방에서의 출발지 수정을 했으므로 나의 진행도와 방의 제출자 +1
    //변변경은 확인했음
    public ResponseEntity<String> changeLocalStartPoint(UserEntity existUser, ChangeLocalStartRequestDTO changeLocalStartRequestDTO) {


        RoomEntity roomEntity = roomRepository.findById(changeLocalStartRequestDTO.getRoomId()).orElseThrow(() -> new NoSuchElementException("존재하지 않는 방"));
        // 여기서 방의 유저가 아니면 빠꾸 시켜야 함
        ParticipantEntity participant = participantRepository
                .findById(new ParticipantEmbededId(existUser.getId(), changeLocalStartRequestDTO.getRoomId()))
                .orElseThrow(() -> new NoSuchElementException("방에 없는 사람"));

        participant.setPoint(changeLocalStartRequestDTO.getStartPoint());
        participant.setLatitude(changeLocalStartRequestDTO.getLatitude());
        participant.setLongitude(changeLocalStartRequestDTO.getLongitude());

        //RecommendStation 출발지 설정 완료, 역 추천 기다리는 중
        if (participant.getProgress() != Processivity.RecommendStation) {
            participant.setProgress(Processivity.RecommendStation);
            roomEntity.setSubmitNumber(roomEntity.getSubmitNumber() + 1);
            if (roomEntity.getNumber() == roomEntity.getSubmitNumber()) {

                roomEntity.setProcessivity(Processivity.RecommendStation);

                //여기서 새로고침을 하기 위해 다른 값을 리턴해줘야 하나?
            }
        }


        return ResponseEntity.ok("해당 약속의 출발지 설정");
    }


    @Transactional
    public ResponseEntity<Void> saveFixDate(UserEntity user,FixDateDTO fixDateDTO) {
        //방 정보 찾아와서
        RoomEntity room = roomRepository.findById(fixDateDTO.getRoomId()).orElseThrow(() -> new NoSuchElementException());
        if(!room.getMaster().equals(user.getEmail())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        room.setFixDay(fixDateDTO.getDate());
        room.setProcessivity(Processivity.SubmitStation);
        room.setSubmitNumber(0);
        List<ParticipantEntity> participantList = participantRepository.findByIdRoomId(fixDateDTO.getRoomId()).orElseThrow(() -> new NoSuchElementException());

        for (ParticipantEntity c : participantList) {
            c.setProgress(Processivity.SubmitStation);
        }

        participantRepository.saveAll(participantList);
        roomRepository.save(room);

        return ResponseEntity.ok().build();

    }

    public ResponseEntity<?> nextClick(UserEntity user,Long roomId) {
        //방 정보 찾아와서
        RoomEntity roomEntity = roomRepository.findById(roomId).get();
        if(!roomEntity.getMaster().equals(user.getEmail())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Processivity processivity = roomEntity.getProcessivity();
        List<ParticipantEntity> participantList = participantRepository.findByIdRoomId(roomId).get();

        Processivity nextProcessivity;

        nextProcessivity = nextProcess(processivity, roomEntity, participantList);

        changeParticipantsProcess(participantList, nextProcessivity);

        roomEntity.setProcessivity(nextProcessivity);


        //        수정 성공하면 숫자 0으로 바꿔야 함
        roomEntity.setSubmitNumber(0);

        return ResponseEntity.ok("Next");
    }


    public Processivity nextProcess(Processivity process, RoomEntity roomEntity, List<ParticipantEntity> participantList) {

        /**
         * 여기서 방의 기존 진행도에 따라서 api요청을 통해서 fix된 장소와 날짜 역 등을 db에저장
         */
        Processivity next = process;

        if (process == Processivity.InSubmission) 
            next = Processivity.RecommendDay;


        return next;

    }

    public void changeParticipantsProcess(List<ParticipantEntity> memberList, Processivity process) {

        for (ParticipantEntity participant : memberList) {
            participant.setProgress(process);
        }

    }

    public ResponseEntity<Void> changeName(UserEntity user, NameChangeDTO nameChangeDTO) {


        ParticipantEntity participant = participantRepository.findByIdUserIdAndIdUUID(user.getId(), nameChangeDTO.getUuid())
                .orElseThrow(() -> new NotInRoomException("해당 방에 존재하는 유저를 찾을 수 없습니다"));


        participant.setRoomName(nameChangeDTO.getEditedTitle());

        return ResponseEntity.ok().build();


    }

    public ResponseEntity<Void> setStation(UserEntity user, SetStationDTO setStationDTO) {

        RoomEntity room = roomRepository.findById(setStationDTO.getRoomId()).orElseThrow(() -> new NoSuchElementException());
        if(!room.getMaster().equals(user.getEmail())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        room.setFixStation(setStationDTO.getStation());
        room.setProcessivity(Processivity.RecommendPlace);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> setPlace(UserEntity user, SetPlaceDTO setPlaceDTO) {
        RoomEntity room = roomRepository.findById(setPlaceDTO.getRoomId())
                .orElseThrow(() -> new NoSuchElementException("방을 찾을 수 없음"));

        List<ParticipantEntity> memberList = participantRepository.findByIdRoomId(room.getRoomId())
                .orElseThrow(() -> new NoSuchElementException("방에 속한 사람이 없음"));

        if(!room.getMaster().equals(user.getEmail())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ForAlertDTO req = ForAlertDTO.builder()
                .roomCode(room.getUUID())
                .reserveTime(room.getFixDay())
                .reserveMembers(memberList.stream()
                        .map(participant -> participant.getUser().getEmail())
                        .collect(Collectors.toList())).build();

        // exchange() 메서드를 사용하여 ClientResponse를 직접 처리
        try {
            ClientResponse clientResponse = webClient.post()
                    .uri("/reserve")
                    .bodyValue(req)
                    .exchange()
                    .block();

            // ClientResponse를 ResponseEntity로 변환
            ResponseEntity<String> res = ResponseEntity.status(clientResponse.rawStatusCode())
                    .headers(headers -> headers.putAll(clientResponse.headers().asHttpHeaders()))
                    .body(clientResponse.bodyToMono(String.class).block());

            room.setFixPlace(setPlaceDTO.getPlace());
            room.setProcessivity(Processivity.Fix);

            return res;

        }catch(Exception e){
            log.warn(e.toString());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    public ResponseEntity<String> changeToLiveMap(String roomUUID) {

        RoomEntity room = roomRepository.findByUUID(roomUUID)
                .orElseThrow(() -> new NoSuchElementException("방을 찾을 수 없음"));

        room.setProcessivity(Processivity.LiveMap);

        return ResponseEntity.ok("연결완료");

    }


    public class UUIDgeneration {
        public String getUUID() {

            //UUID 생성
            String uuid = UUID.randomUUID().toString();


            // "-" 하이픈 제외
            uuid = uuid.replace("-", "");

            return uuid;
        }
    }


}
