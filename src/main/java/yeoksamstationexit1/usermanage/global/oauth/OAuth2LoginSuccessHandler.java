package yeoksamstationexit1.usermanage.global.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import yeoksamstationexit1.usermanage.global.jwt.JwtService;
import yeoksamstationexit1.usermanage.user.Role;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtService jwtService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    log.info("OAuth2 Login 성공!");
    try {

      CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

      // User의 Role이 GUEST일 경우 처음 요청한 회원이므로 회원가입 페이지로 리다이렉트
      if (oAuth2User.getRole() == Role.USER) {

        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());

        Cookie cookie = new Cookie("domainPsX", accessToken);
        cookie.setPath("/");    // 쿠키 경로
        cookie.setDomain("meethare.site");
//        cookie.setHttpOnly(true); // HTTP Only 설정 (보안을 위해)
//        cookie.setSecure(true);


        Cookie cookie2 = new Cookie("domainPsO", accessToken);
        cookie2.setPath("/");    // 쿠키 경로
        cookie2.setDomain("meethare.site");
//        cookie.setHttpOnly(true); // HTTP Only 설정 (보안을 위해)
        cookie2.setSecure(true);

        Cookie cookie3 = new Cookie("domainXPsO", accessToken);
        cookie3.setPath("/");    // 쿠키 경로
//        cookie.setDomain("meethare.site");
//        cookie.setHttpOnly(true); // HTTP Only 설정 (보안을 위해)
        cookie3.setSecure(true);

        Cookie cookie4 = new Cookie("domainXPsX", accessToken);
        cookie4.setPath("/");    // 쿠키 경로
//        cookie.setDomain("meethare.site");
//        cookie.setHttpOnly(true); // HTTP Only 설정 (보안을 위해)
//        cookie.setSecure(true);

        ResponseCookie cookie5 = ResponseCookie.from("ResponseCookieSameDomainSecure", accessToken)
                .path("/")
                .sameSite("None")
                .secure(false)
                .domain("meethare.site")
                .build();

        ResponseCookie cookie6 = ResponseCookie.from("ResponseCookieSameDomainSecureX", accessToken)
                .path("/")
                .sameSite("None")
                .domain("meethare.site")
                .build();

        ResponseCookie cookie7 = ResponseCookie.from("ResponseCookieSameDomainXSecure", accessToken)
                .path("/")
                .sameSite("None")
                .secure(false)
                .build();

        ResponseCookie cookie8 = ResponseCookie.from("ResponseCookieSameXDomainSecure", accessToken)
                .path("/")
                .secure(false)
                .domain("meethare.site")
                .build();

        ResponseCookie cookie9 = ResponseCookie.from("ResponseCookieSameDomainXSecureX", accessToken)
                .path("/")
                .sameSite("None")
                .build();

        ResponseCookie cookie10 = ResponseCookie.from("ResponseCookieSameXDomainSecureX", accessToken)
                .path("/")
                .domain("meethare.site")
                .build();

        ResponseCookie cookie11 = ResponseCookie.from("ResponseCookieSameXDomainXSecure", accessToken)
                .path("/")
                .secure(false)
                .build();

        ResponseCookie cookie12 = ResponseCookie.from("ResponseCookieSameXDomainXSecureX", accessToken)
                .path("/")
                .build();


        // 응답 헤더에 쿠키 추가
        response.addCookie(cookie);
        response.addCookie(cookie2);
        response.addCookie(cookie3);
        response.addCookie(cookie4);


//        response.addHeader("Set-Cookie", cookie.toString());
//        response.addHeader("Set-Cookie", cookie2.toString());
//        response.addHeader("Set-Cookie", cookie3.toString());
//        response.addHeader("Set-Cookie", cookie4.toString());
        response.addHeader("Set-Cookie", cookie5.toString());
        response.addHeader("Set-Cookie", cookie6.toString());
        response.addHeader("Set-Cookie", cookie7.toString());
        response.addHeader("Set-Cookie", cookie8.toString());
        response.addHeader("Set-Cookie", cookie9.toString());
        response.addHeader("Set-Cookie", cookie10.toString());
        response.addHeader("Set-Cookie", cookie11.toString());
        response.addHeader("Set-Cookie", cookie12.toString());

        response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);


        response.sendRedirect(request.getHeader("Referer")); // 프론트의 회원가입 추가 정보 입력 폼으로 리다이렉트

//        jwtService.sendAccessAndRefreshToken(response, accessToken, null);
        // Role을 Guest에서 User로
        // UserEntity findUser = userRepository.findByEmail(oAuth2User.getEmail())
        // .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 유저가 없습니다."));
        // findUser.authorizeUser();
      } else {

        loginSuccess(response, oAuth2User); // 로그인에 성공한 경우 access, refresh 토큰 생성
      }
    } catch (Exception e) {

      throw e;
    }

  }

  // 로그인이 성공했을 경우, Access/Refresh Token 발급 후 갱신
  private void loginSuccess(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {

    String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
    String refreshToken = jwtService.createRefreshToken();
    response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
    response.addHeader(jwtService.getRefreshHeader(), "Bearer " + refreshToken);

    jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
    jwtService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);
  }
}