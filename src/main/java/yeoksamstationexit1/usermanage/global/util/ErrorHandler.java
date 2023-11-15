package yeoksamstationexit1.usermanage.global.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    public ResponseEntity<Void> errorMessage(Exception e) {
        logger.error("에러 발생 위치: " + Arrays.toString(e.getStackTrace()));
        logger.error("에러 메시지: " + e.getMessage());
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}