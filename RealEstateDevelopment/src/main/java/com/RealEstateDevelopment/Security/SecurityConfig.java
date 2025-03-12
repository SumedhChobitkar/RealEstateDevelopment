package com.RealEstateDevelopment.Security;

import com.RealEstateDevelopment.Repository.BlacklistedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
@EnableMethodSecurity(prePostEnabled = true)  // Enable @PreAuthorize
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to registration and other authorization
                        .requestMatchers(HttpMethod.POST, "/api/agents/registerTemporaryAgent").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/registerAdmin").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/registerTemporaryUser").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/verifyOtpToRegisterUser").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/properties/getAllTheProperties").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/properties/searchProperties").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/properties/searchProperty").permitAll()

                        // Allow public access to login
                        .requestMatchers(HttpMethod.POST, "/api/agents/loginAgent").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/loginAdmin").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/loginUser").permitAll()

                        // Allow logout for authenticated users
                        .requestMatchers(HttpMethod.POST, "/api/users/logoutUser").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/admin/logoutAdmin").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/agents/logoutAgent").authenticated()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/agents/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/properties/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers("/api/forgotPassword/**").permitAll()
                        .requestMatchers("/api/blog/**").permitAll()
                        .requestMatchers("/api/reviews/**").permitAll()
                        .requestMatchers("/api/locations/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService, blacklistedTokenRepository), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
