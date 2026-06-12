package com.emergency.security;

import com.emergency.model.User;
import com.emergency.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth.redirect-uri:http://localhost:3000/auth/callback}")
    private String redirectUri;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String provider = oAuth2User.getAttributes().containsKey("sub") ? "google" : "github";

        String providerId = oAuth2User.getAttribute("sub") != null
            ? oAuth2User.getAttribute("sub")
            : String.valueOf(oAuth2User.getAttribute("id"));

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("picture") != null
            ? oAuth2User.getAttribute("picture")
            : oAuth2User.getAttribute("avatar_url");

        // 1. Check by OAuth identity (existing OAuth user)
        User user = userRepository.findByProviderAndProviderId(provider, providerId).orElse(null);

        if (user == null) {
            // 2. Not found by OAuth — check by email (pre-created by admin)
            user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // 3. Email not in system at all — reject (whitelist only)
                String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "not_whitelisted")
                    .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            }

            // Link OAuth to existing pre-created account
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setAvatarUrl(avatarUrl);
            if (name != null) user.setName(name);
            userRepository.save(user);
        }

        String token = jwtTokenProvider.generateToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("token", token)
            .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
