package com.resourcebooking.config;

import com.resourcebooking.security.CustomUserDetailsService;
import com.resourcebooking.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService customUserDetailsService) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    /*
     * ==========================================
     * PASSWORD ENCODER
     * ==========================================
     */

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    /*
     * ==========================================
     * AUTHENTICATION PROVIDER
     * ==========================================
     */

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        customUserDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }

    /*
     * ==========================================
     * AUTHENTICATION MANAGER
     * ==========================================
     */

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    /*
     * ==========================================
     * SECURITY FILTER CHAIN
     * ==========================================
     */

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http

                // JWT APIs are stateless.
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authenticationProvider(
                        authenticationProvider()
                )

                .authorizeHttpRequests(auth -> auth

                        /*
                         * ======================================
                         * PUBLIC ENDPOINTS
                         * ======================================
                         */

                        .requestMatchers(
                                "/auth/login"
                        ).permitAll()

                        /*
                         * Swagger/OpenAPI
                         */

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        /*
                         * ======================================
                         * RESOURCE ENDPOINTS
                         * ======================================
                         */

                        // USER + ADMIN can read resources
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/resources",
                                "/resources/**"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // ADMIN can create resources
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/resources",
                                "/resources/**"
                        ).hasRole("ADMIN")

                        // ADMIN can update resources
                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/resources",
                                "/resources/**"
                        ).hasRole("ADMIN")

                        // ADMIN can delete resources
                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/resources",
                                "/resources/**"
                        ).hasRole("ADMIN")

                        /*
                         * ======================================
                         * RESERVATION ENDPOINTS
                         * ======================================
                         */

                        // USER + ADMIN can create reservations
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/reservations"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // USER + ADMIN can view reservations
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/reservations",
                                "/reservations/**"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // ADMIN only - update reservations
                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/reservations",
                                "/reservations/**"
                        ).hasRole("ADMIN")

                        // ADMIN only - delete reservations
                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/reservations",
                                "/reservations/**"
                        ).hasRole("ADMIN")

                        /*
                         * ======================================
                         * EVERYTHING ELSE
                         * ======================================
                         */

                        .anyRequest()
                        .authenticated()
                )

                /*
                 * JWT filter must execute before the
                 * username/password authentication filter.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}