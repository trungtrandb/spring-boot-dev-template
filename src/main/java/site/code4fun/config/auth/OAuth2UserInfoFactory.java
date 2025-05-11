package site.code4fun.config.auth;


import site.code4fun.constant.Oauth2Provider;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(Oauth2Provider provider, Map<String, Object> attributes) {
        if(provider == Oauth2Provider.google) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (provider == Oauth2Provider.facebook) {
            return new FacebookOAuth2UserInfo(attributes);
        } else if (provider == Oauth2Provider.github) {
            return new GithubOAuth2UserInfo(attributes);
        }
        throw new RuntimeException("Sorry! Login with " + provider + " is not supported yet.");
    }

    static class GithubOAuth2UserInfo extends OAuth2UserInfo {

        public GithubOAuth2UserInfo(Map<String, Object> attributes) {
            super(attributes);
        }

        @Override
        public String getId() {
            return ((Integer) attributes.get("id")).toString();
        }

        @Override
        public String getImageUrl() {
            return (String) attributes.get("avatar_url");
        }
    }

    static class GoogleOAuth2UserInfo extends OAuth2UserInfo {

        public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
            super(attributes);
        }

        @Override
        public String getId() {
            return (String) attributes.get("sub");
        }

        @Override
        public String getImageUrl() {
            return (String) attributes.get("picture");
        }
    }

    static class FacebookOAuth2UserInfo extends OAuth2UserInfo {
        public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
            super(attributes);
        }

        @Override
        public String getImageUrl() {
            if(attributes.containsKey("picture")) {
                Map<String, Object> pictureObj = (Map<String, Object>) attributes.get("picture");
                if(pictureObj.containsKey("data")) {
                    Map<String, Object>  dataObj = (Map<String, Object>) pictureObj.get("data");
                    if(dataObj.containsKey("url")) {
                        return (String) dataObj.get("url");
                    }
                }
            }
            return null;
        }
    }
}