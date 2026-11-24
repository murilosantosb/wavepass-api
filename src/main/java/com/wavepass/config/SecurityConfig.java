package com.wavepass.config;

import com.wavepass.service.AuthService;
import com.wavepass.service.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, TokenService tokenService,
                                            AuthService authService) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/wavepass/usuario/login",
                                "/api/wavepass/usuario/registerClient",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/errors"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/wavepass/evento/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/wavepass/evento/**")
                        .hasAnyRole("ADMIN", "WAVEPASS")

                        .requestMatchers(HttpMethod.PUT, "/api/wavepass/evento/**")
                        .hasAnyRole("ADMIN", "WAVEPASS")

                        .requestMatchers(HttpMethod.DELETE, "/api/wavepass/evento/**")
                        .hasAnyRole("ADMIN", "WAVEPASS")

                        .requestMatchers(HttpMethod.GET, "/api/wavepass/usuario")
                        .hasRole("WAVEPASS")

                        .requestMatchers(HttpMethod.DELETE, "/api/wavepass/usuario/**")
                        .hasRole("WAVEPASS")

                        .requestMatchers(
                                "/api/wavepass/gerente/**",
                                "/api/wavepass/admin/**",
                                "/api/wavepass/usuario/registerAdmin",
                                "/api/wavepass/usuario/admin/**"
                        ).hasRole("WAVEPASS")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(new SecurityFilter(tokenService, authService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}