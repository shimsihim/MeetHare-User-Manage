package yeoksamstationexit1.usermanage.global.oauth;// WebClient를 활용한 SNS 로그인 컨트롤러
// package com.ssacation.ssacation.global.oauth;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.ResponseBody;
// import org.springframework.web.bind.annotation.RestController;

// import lombok.AllArgsConstructor;

// @RestController
// @AllArgsConstructor
// @RequestMapping("/oauth")
// public class OAuthController {

// private final OAuthService oAuthService;

// /**
// * 카카오 callback
// * [GET] /oauth/kakao/callback
// */
// @ResponseBody
// @GetMapping("/kakao")
// public ResponseEntity<?> kakaoCallback(@RequestParam String code) {

// // accessToken 발급받기
// // String accessToken = oAuthService.getKakaoAccessToken(code);

// // userInfo 받아오기
// // Object userInfo = oAuthService.getUserInfo(accessToken);

// return ResponseEntity.ok("성공~!");
// }
// }