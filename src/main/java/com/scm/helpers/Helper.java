package com.scm.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class Helper {

    @Value("${server.baseUrl}")
    private String baseUrl;

    public static String getEmailOfLoggedInUser(Authentication authentication) {

        // agar email is password se login kiya hai to : email kaise nikalenge
        if (authentication instanceof OAuth2AuthenticationToken) {

            var aOAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            var clientId = aOAuth2AuthenticationToken.getAuthorizedClientRegistrationId();

            var oauth2User = (OAuth2User) authentication.getPrincipal();
            String username = "";

            if (clientId.equalsIgnoreCase("google")) {

                // sign with google
                System.out.println("Getting email from google");
                username = oauth2User.getAttribute("email").toString();

            } else if (clientId.equalsIgnoreCase("github")) {

                // sign with github
                System.out.println("Getting email from github");
                username = oauth2User.getAttribute("email") != null ? oauth2User.getAttribute("email").toString()
                        : oauth2User.getAttribute("login").toString() + "@gmail.com";
            }

            // sign with facebook
            return username;

        } else {
            System.out.println("Getting data from local database");
            return authentication.getName();
        }

    }

    public String getLinkForEmailVerificatiton(String emailToken) {
        try {
            var attributes = (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                var request = attributes.getRequest();
                String scheme = request.getHeader("X-Forwarded-Proto");
                if (scheme == null || scheme.isEmpty()) {
                    scheme = request.getScheme();
                }
                String host = request.getHeader("Host");
                if (host != null && !host.isEmpty()) {
                    return scheme + "://" + host + request.getContextPath() + "/auth/verify-email?token=" + emailToken;
                }
            }
        } catch (Exception e) {
            // fallback to configured properties baseUrl
        }
        return this.baseUrl + "/auth/verify-email?token=" + emailToken;
    }
}
