package yeoksamstationexit1.usermanage.global.oauth;// // WebClient를 활용한 SNS 로그인 서비스
// package com.ssacation.ssacation.global.oauth;

// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Service;
// import org.springframework.util.StringUtils;
// import org.springframework.web.reactive.function.client.WebClient;

// import lombok.extern.slf4j.Slf4j;

// import java.util.HashMap;
// import java.util.Map;

// @Slf4j
// @Service
// public class OAuthService {

// String clientId = "5751a58d2e844ce9688d9a20b2fffd60";

// // 카카오 서버로 부터 Access 토큰값 받아오기
// public String getKakaoAccessToken(String code) {

// String kakaoAuthUrl = "https://kauth.kakao.com";
// WebClient kakaoWebClient = WebClient.builder()
// .baseUrl(kakaoAuthUrl)
// .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
// .build();

// // 카카오 Auth API 호출
// @SuppressWarnings("unchecked")
// Map<String, Object> tokenResponse = kakaoWebClient
// .post()
// .uri(uriBuilder -> uriBuilder
// .path("/oauth/token")
// .queryParam("grant_type", "authorization_code")
// .queryParam("client_id", clientId)
// .queryParam("code", code)
// .build())
// .retrieve()
// .bodyToMono(Map.class)
// .block();

// String accessToken = (String) tokenResponse.get("access_token");
// log.info("accessToken : " + accessToken);
// return accessToken;
// }

// // 액세스 토큰으로 카카오 서버에서 유저 정보 받아오기
// public Object getUserInfo(String accessToken) {

// String kakaoApiUrl = "https://kapi.kakao.com";
// // webClient 설정
// WebClient kakaoApiWebClient = WebClient.builder()
// .baseUrl(kakaoApiUrl)
// .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
// .build();

// // 카카오 Info API 설정
// @SuppressWarnings("unchecked")
// Map<String, Object> infoResponse = kakaoApiWebClient
// .post()
// .uri(uriBuilder -> uriBuilder
// .path("/v2/user/me")
// .build())
// .header("Authorization", "Bearer " + accessToken)
// .retrieve()
// .bodyToMono(Map.class)
// .block();

// @SuppressWarnings("unchecked")
// Map<String, Object> kakaoAccountMap = (Map<String, Object>)
// infoResponse.get("kakao_account");
// @SuppressWarnings("unchecked")
// Map<String, String> profileMap = (Map<String, String>)
// kakaoAccountMap.get("profile");
// HashMap<String, Object> responseMap = new HashMap<>();

// // 닉네임 정보 담기
// if (StringUtils.hasText(profileMap.get("nickname"))) {
// responseMap.put("nickname", profileMap.get("nickname"));
// }
// // 프로필 사진 정보 담기
// if (StringUtils.hasText(profileMap.get("profile_image_url"))) {
// responseMap.put("profileImageUrl", profileMap.get("profile_image_url"));
// }
// // 이메일 정보 담기
// if ("true".equals(kakaoAccountMap.get("has_email").toString())) {
// responseMap.put("email", kakaoAccountMap.get("email").toString());
// }
// // 성별 정보 담기
// if ("true".equals(kakaoAccountMap.get("has_gender").toString())) {
// responseMap.put("gender", kakaoAccountMap.get("gender").toString());
// }
// // 연령대 정보 담기
// if ("true".equals(kakaoAccountMap.get("has_age_range").toString())) {
// responseMap.put("ageRange", kakaoAccountMap.get("age_range").toString());
// }
// // 생일 정보 담기
// if ("true".equals(kakaoAccountMap.get("has_birthday").toString())) {
// responseMap.put("birthday", kakaoAccountMap.get("birthday").toString());
// }

// // 결과 반환
// log.info("kakaoAccountMap : " + kakaoAccountMap);
// return responseMap;
// }
// }