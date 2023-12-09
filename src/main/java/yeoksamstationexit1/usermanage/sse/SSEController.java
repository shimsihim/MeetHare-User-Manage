package yeoksamstationexit1.usermanage.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user-manage/sse")
@Slf4j
public class SSEController {

    //방마다 emitter관리

    private final Map<Long, List<SseEmitter>> roomEmitters = new ConcurrentHashMap<>();
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 5;
    //연결 수립

//    public SseEmitter handleSSE(@PathVariable Long room) {
    @GetMapping(value = "/{room}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(@PathVariable Long room) {

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        roomEmitters.computeIfAbsent(room, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> {
            log.info("onCompletion callback");
            removeFromRoom(emitter, room);
        });
        // 만료되면 리스트에서 삭제
        emitter.onTimeout(() -> {
            log.info("타임아웃");
            emitter.complete();
        });


        return ResponseEntity.ok(emitter);
    }


    @GetMapping("/submit/quit/{room}")
    public ResponseEntity<?> handleSubmission(@PathVariable Long room) {

        // 새로고침을 위해 SSE 이벤트 발송
        sendRefreshEventToRoom(room);



        return ResponseEntity.ok("Submission completed");

    }

    @GetMapping("/remove/{room}")
    public ResponseEntity<?> quit2(@PathVariable Long room) {

        // 새로고침을 위해 SSE 이벤트 발송
        List<SseEmitter> emitters = roomEmitters.get(room);
        if (emitters != null) {
           roomEmitters.remove(room);
        }



        return ResponseEntity.ok("Submission completed");

    }

    private void removeFromRoom(SseEmitter emitter, Long room) {
        List<SseEmitter> emitters = roomEmitters.get(room);
        if (emitters != null) {
            emitters.remove(emitter);
            log.info("emitter삭제");
            // 방에 속한 모든 클라이언트가 나갔을 때 방을 삭제
            if (emitters.isEmpty()) {
                log.info("방삭제");
                roomEmitters.remove(room);
            }
        }
    }

    public void sendRefreshEventToRoom(Long room) {
        List<SseEmitter> emitters = roomEmitters.get(room);
        if (emitters != null) {
            log.info("emitter존재");
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().id("아이디").name("refresh").data("Refresh"));
                } catch (IOException e) {
                    log.info("전송오류");
                    emitter.complete();
                    emitters.remove(emitter);
                }
            }
        }else{
            log.info("emitter없음");
        }
    }
}