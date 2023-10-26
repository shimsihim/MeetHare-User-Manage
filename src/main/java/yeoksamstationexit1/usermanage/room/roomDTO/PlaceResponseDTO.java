package yeoksamstationexit1.usermanage.room.roomDTO;

import lombok.Data;

import java.util.List;

@Data
public class PlaceResponseDTO {

    private List<Place> placeList;

    @Data
    class Place {
        private Integer placeNum;
        private String placeName;
        private Float placeGrade;
        private String placeAddress;
    }

}
