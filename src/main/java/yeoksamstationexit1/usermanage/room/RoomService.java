package yeoksamstationexit1.usermanage.room;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import yeoksamstationexit1.usermanage.exception.NotInRoomException;
import yeoksamstationexit1.usermanage.room.enumClass.Processivity;
import yeoksamstationexit1.usermanage.room.participant.dto.ChangeLocalStartRequestDTO;
import yeoksamstationexit1.usermanage.room.participant.ParticipantEmbededId;
import yeoksamstationexit1.usermanage.room.participant.ParticipantEntity;
import yeoksamstationexit1.usermanage.room.participant.ParticipantRepository;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

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


        RoomEntity room = new RoomEntity(createRoomDTO,existUser.getEmail(),UUID.randomUUID().toString());

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

    public ResponseEntity<?> findPersonalRoom(UserEntity existUser) {


            List<ParticipantEntity> participateList = participantRepository.findByIdUserId(existUser.getId());
            List<RoomListDTO> roomList = participateList.stream()
                    .map(participant -> {
                        RoomListDTO roomListDTO = new RoomListDTO();
                        roomListDTO.setUUID(participant.getRoom().getUUID());
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
    public ResponseEntity<Map<String, List<String>>> getAllImpossTime(UserEntity existUser, GetAllTimeInRoomDTO getAllTimeInRoomDTO) {

        Optional<List<ParticipantEntity>> userIdListOp = participantRepository.findByIdRoomId(getAllTimeInRoomDTO.getRoomId());
        List<Long> userIdList = userIdListOp.map(participantEntities -> {
            return participantEntities.stream()
                    .map(participantEntity -> participantEntity.getUser().getId())
                    .collect(Collectors.toList());
        }).orElse(Collections.emptyList());

        List<LocalDate> roomTimeList = fixCalendarRepository.findByUserListAndDateRange(userIdList, getAllTimeInRoomDTO.getPeriodStart(), getAllTimeInRoomDTO.getPeriodEnd());


        //기간 내의 날짜 리스트 생성
        List<LocalDate> dateList = Stream.iterate(getAllTimeInRoomDTO.getPeriodStart(), date -> date.plusDays(1))
                .limit(getAllTimeInRoomDTO.getPeriodStart().until(getAllTimeInRoomDTO.getPeriodEnd().plusDays(1)).getDays())
                .collect(Collectors.toList());


        List<String> excludedDates = dateList.stream()
                .filter(date -> !roomTimeList.contains(date))
                .map(date -> date.toString())
                .collect(Collectors.toList());


        Map<String, List<String>> response = new HashMap<>();
        response.put("dateList", excludedDates);

        return ResponseEntity.ok(response);
    }


    //나의 방에서의 출발지 수정을 했으므로 나의 진행도와 방의 제출자 +1
    //변변경은 확인했음
    public ResponseEntity<String> changeLocalStartPoint(UserEntity existUser, ChangeLocalStartRequestDTO changeLocalStartRequestDTO) {


        ParticipantEmbededId id = new ParticipantEmbededId(existUser.getId(), changeLocalStartRequestDTO.getRoomId());

        RoomEntity roomEntity = roomRepository.findById(changeLocalStartRequestDTO.getRoomId()).orElseThrow(() -> new NoSuchElementException("존재하지 않는 방"));
        // 여기서 방의 유저가 아니면 빠꾸 시켜야 함
        ParticipantEntity participant = participantRepository.findById(id).orElseThrow(() -> new NoSuchElementException("방에 없는 사람"));
        participant.setPoint(changeLocalStartRequestDTO.getStartPoint());
        participant.setLatitude(changeLocalStartRequestDTO.getLatitude());
        participant.setLongitude(changeLocalStartRequestDTO.getLongitude());

        //RecommendStation 출발지 설정 완료, 역 추천 기다리는 중
        if (participant.getProgress() != Processivity.RecommendStation) {
            participant.setProgress(Processivity.RecommendStation);
            roomEntity.setSubmitNumber(roomEntity.getSubmitNumber() + 1);
        }
        if (roomEntity.getNumber() == roomEntity.getSubmitNumber()) {
            roomEntity.setProcessivity(Processivity.RecommendStation);

            //여기서 새로고침을 하기 위해 다른 값을 리턴해줘야 하나?
        }

        return ResponseEntity.ok("해당 약속의 출발지 설정");
    }


    public ResponseEntity<Void> saveFixDate(FixDateDTO fixDateDTO) {
        //방 정보 찾아와서
        RoomEntity room = roomRepository.findById(fixDateDTO.getRoomId()).orElseThrow(() -> new NoSuchElementException());

        room.setFixDay(fixDateDTO.getDate());
        room.setProcessivity(Processivity.SubmitStation);
        room.setSubmitNumber(0);

        roomRepository.save(room);

        return ResponseEntity.ok().build();

    }

    public ResponseEntity<?> nextClick(Long roomId) {
        //방 정보 찾아와서
        RoomEntity roomEntity = roomRepository.findById(roomId).get();


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

    public boolean recommendDay(RoomEntity roomEntity, List<ParticipantEntity> participantList) {


        List<Long> userIdList = participantList.stream()
                .map(participant -> participant.getUser().getId())
                .collect(Collectors.toList());

        List<LocalDate> impossibleDates = fixCalendarRepository.findByUserList(userIdList);


        // 가능한 날짜를 찾아내는 방법:
        List<LocalDate> possibleDates = IntStream.range(0, roomEntity.getPeriodEnd().getDayOfYear() - roomEntity.getPeriodStart().getDayOfYear() + 1)
                .mapToObj(roomEntity.getPeriodStart()::plusDays)
                .filter(date -> !impossibleDates.contains(date))
                .collect(Collectors.toList());

        if (possibleDates.size() == 0) {
            return false;
        }
        roomEntity.setFixDay(possibleDates.get(0));

        return true;
    }

    public Processivity nextProcess(Processivity process, RoomEntity roomEntity, List<ParticipantEntity> participantList) {

        /**
         * 여기서 방의 기존 진행도에 따라서 api요청을 통해서 fix된 장소와 날짜 역 등을 db에저장
         */
        Processivity next = process;

        if (process == Processivity.InSubmission) {
            next = Processivity.RecommendDay;
        } else if (process == Processivity.RecommendDay) {
            next = Processivity.SubmitStation;
        } else if (process == Processivity.SubmitStation) {
            next = Processivity.RecommendStation;
        } else if (process == Processivity.RecommendStation) {
            next = Processivity.RecommendPlace;
        } else if (process == Processivity.RecommendPlace) {
            next = Processivity.Fix;
        }


        return next;

    }

    public void changeParticipantsProcess(List<ParticipantEntity> memberList, Processivity process) {

        for (ParticipantEntity participant : memberList) {
            participant.setProgress(process);
        }

    }

    public ResponseEntity<Void> changeName(UserEntity user, NameChangeDTO nameChangeDTO) {



        ParticipantEntity participant = participantRepository.findByIdUserIdAndIdUUID(user.getId(), nameChangeDTO.getUuid())
                .orElseThrow(()-> new NotInRoomException("해당 방에 존재하는 유저를 찾을 수 없습니다"));


        participant.setRoomName(nameChangeDTO.getEditedTitle());

        return ResponseEntity.ok().build();




    }
    public ResponseEntity<Void> setStation(UserEntity user, SetStationDTO setStationDTO) {
        System.out.println(123);
        System.out.println(setStationDTO.toString());
        RoomEntity room = roomRepository.findById(setStationDTO.getRoomId()).orElseThrow(()->new NoSuchElementException());
        System.out.println(123);
        room.setFixStation(setStationDTO.getStation());

        return ResponseEntity.ok().build();
    }
    public ResponseEntity<Void> setPlace(UserEntity user, SetPlaceDTO setPlaceDTO) {

        RoomEntity room = roomRepository.findById(setPlaceDTO.getRoomId()).orElseThrow(()->new NoSuchElementException());
        room.setFixPlace(setPlaceDTO.getPlace());

        return ResponseEntity.ok().build();
    }



    public class UUIDgeneration {
        public String getUUID() {

            //UUID 생성
            String uuid = UUID.randomUUID().toString();
            System.out.println(uuid);

            // "-" 하이픈 제외
            uuid = uuid.replace("-", "");
            System.out.println(uuid);
            return uuid;
        }
    }


}
