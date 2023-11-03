package yeoksamstationexit1.usermanage.route;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
import reactor.core.publisher.Mono;
import yeoksamstationexit1.usermanage.route.dto.request.station.RoomIdAndStartPointListDTO;
import yeoksamstationexit1.usermanage.user.UserEntity;

@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
@Tag(name = "추천", description = "추천을 위한 단순 인증 관련 API 입니다.")
public class authRouteController {

    private final WebClient webClient;

    //지하철역 추천을 위한 것
    @PostMapping("/map/middlespot")
    public ResponseEntity<Object> getRecommendStationList(@AuthenticationPrincipal UserEntity user, @RequestBody RoomIdAndStartPointListDTO roomIdAndStartPointListDTO) {

        Mono<Object> resp = webClient.post()
                .uri("/map/middlespot")
                .bodyValue(roomIdAndStartPointListDTO.getStartPointList())
                .retrieve()
                .bodyToMono(Object.class);


        return new ResponseEntity<>(resp.block(), HttpStatus.OK);
    }


    //장소 추천을 위한 것
    @PostMapping("/place/complex")
    public void getRecommendPlaceList(@RequestBody RoomIdAndStartPointListDTO roomIdAndStartPointListDTO) {

        System.out.println(roomIdAndStartPointListDTO.toString());




    }



}
