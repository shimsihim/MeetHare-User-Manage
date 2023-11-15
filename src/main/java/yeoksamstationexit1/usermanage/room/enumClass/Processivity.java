package yeoksamstationexit1.usermanage.room.enumClass;


public enum Processivity {
    InSubmission,RecommendDay,SubmitStation,RecommendStation,RecommendPlace,Fix,LiveMap

    //InSubmission : 나의 불가능한 날짜 넣는 중
    //RecommendDay : 방장이 날짜 리스트를 받고 이들 중 1개를 선택하는 중
    //SubmitStation : 나의 출발지 선택하는 중
    //RecommendStation : 역 추천 정보 받는 중
    //RecommendPlace : 장소 선택하는 중
    //Fix : 결정
    //LiveMap : 약속 당일 실시간 지도로 보여줌
}
