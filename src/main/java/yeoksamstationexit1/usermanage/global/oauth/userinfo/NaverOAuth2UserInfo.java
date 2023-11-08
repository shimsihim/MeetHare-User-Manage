package yeoksamstationexit1.usermanage.global.oauth.userinfo;

import java.util.Map;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {

  public NaverOAuth2UserInfo(Map<String, Object> attributes) {

    super(attributes);
  }

  @Override
  public String getId() {

    @SuppressWarnings("unchecked")
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");

    if (response == null) {
      return null;
    }
    return (String) response.get("id");
  }

  @Override
  public String getNickname() {

    @SuppressWarnings("unchecked")
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");

    if (response == null) {
      return null;
    }

    return (String) response.get("nickname");
  }

  @Override
  public String getImageUrl() {

    @SuppressWarnings("unchecked")
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");

    if (response == null) {
      return null;
    }

    return (String) response.get("profile_image");
  }
}