package yeoksamstationexit1.usermanage.global.roomSSE;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomEmitters {

    private final Map<String, List<SseEmitter>> roomEmitters = new ConcurrentHashMap<>();

    public Map<String, List<SseEmitter>> getRoomEmitters() {
        return roomEmitters;
    }
}
