package com.emergency.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.emergency.security.JwtAuthenticationFilter;
import com.emergency.security.OAuth2SuccessHandler;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/chat/**").authenticated()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/events/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/calls/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/calls/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/calls/**").hasAnyRole("admin", "superadmin")
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/disaster-types/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/disaster-types/**").hasAnyRole("admin", "superadmin")
                .requestMatchers(HttpMethod.PUT, "/api/disaster-types/**").hasAnyRole("admin", "superadmin")
                .requestMatchers(HttpMethod.DELETE, "/api/disaster-types/**").hasAnyRole("admin", "superadmin")
                .requestMatchers(HttpMethod.GET, "/api/centers/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/centers/**").hasAnyRole("admin", "superadmin")
                .requestMatchers(HttpMethod.PUT, "/api/centers/**").hasAnyRole("admin", "superadmin")
                .requestMatchers(HttpMethod.DELETE, "/api/centers/**").hasAnyRole("admin", "superadmin")
                .requestMatchers("/api/admin/**").hasAnyRole("admin", "superadmin")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Unauthorized\"}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
