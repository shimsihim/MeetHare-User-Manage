package yeoksamstationexit1.usermanage.route;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import yeoksamstationexit1.usermanage.room.participant.ParticipantEntity;
import yeoksamstationexit1.usermanage.room.participant.ParticipantRepository;
import yeoksamstationexit1.usermanage.route.dto.request.place.FixStationAndRoomIdDTO;
import yeoksamstationexit1.usermanage.route.dto.request.station.RoomIdAndStartPointListDTO;
import yeoksamstationexit1.usermanage.user.UserEntity;

import java.util.Optional;


@RequiredArgsConstructor
@RequestMapping("/path")
@RestController
@Tag(name = "추천", description = "추천을 위한 단순 인증 관련 API 입니다.")
public class authRouteController {

    private final ParticipantRepository participantRepository;
    private final WebClient webClient;
    private final Logger log = LoggerFactory.getLogger(getClass());

    //지하철역 추천을 위한 것
    @PostMapping("/map/middlespot")
    public ResponseEntity<Object> getRecommendStationList(@AuthenticationPrincipal UserEntity user, @RequestBody RoomIdAndStartPointListDTO roomIdAndStartPointListDTO) {

        Optional<ParticipantEntity> participantOp = participantRepository.findByIdUserIdAndIdRoomId(user.getId(), roomIdAndStartPointListDTO.getRoomId());

        if (participantOp.isPresent()) {
            ParticipantEntity participant = participantOp.get();

            Mono<Object> resp = webClient.post()
                    .uri("/map/middlespot")
                    .bodyValue(roomIdAndStartPointListDTO.getStartPointList())
                    .retrieve()
                    .bodyToMono(Object.class);

            return resp.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                    .onErrorResume(e -> {
                        log.warn("역 찾기 서버 에러");
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                    })
                    .block();
        } else {
            log.warn("방에 참여한 사용자가 아님");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }


    //장소 추천을 위한 것
    @PostMapping("/place/complex")
    public ResponseEntity<?> getRecommendPlaceList(@AuthenticationPrincipal UserEntity user, @RequestBody FixStationAndRoomIdDTO fixStationAndRoomIdDTO) {

        Optional<ParticipantEntity> participantOp = participantRepository.findByIdUserIdAndIdRoomId(user.getId(), fixStationAndRoomIdDTO.getRoomId());

        if (participantOp.isPresent()) {
            Mono<Object> resp = webClient.post()
                    .uri("/place/complex")
                    .bodyValue(fixStationAndRoomIdDTO.getFixStation())
                    .retrieve()
                    .bodyToMono(Object.class);


            return resp.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                    .onErrorResume(e -> {
                        log.warn("장소찾기 서버 에러");
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                    })
                    .block();


        }
        else{
            log.warn("방에 참여한 사용자가 아님");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }


}
