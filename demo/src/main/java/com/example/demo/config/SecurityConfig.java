package com.example.demo.config;

// import java.security.KeyStore.SecretKeyEntry;
// import java.util.List;
// import java.util.stream.Collectors;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Value("${jwt.signerKey}")
        private String signerKey;

        private final String[] PUBLIC_ENDPOINTS = { "/users", "/auth/token", "/auth/introspect", "/auth/logout", };

        // @Bean
        // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
        // Exception {
        // // http
        // // .csrf(csrf -> csrf.disable()) // Tắt CSRF cho API
        // // .authorizeHttpRequests(auth -> auth
        // // .requestMatchers(HttpMethod.GET, "/users")
        // // .hasRole("Admin") // Cho phép tất cả các phương thức tại /users
        // // .requestMatchers("/payments/**").permitAll()
        // // .anyRequest().permitAll() // Cho phép tất cả các request khác mà không cần
        // // xác thực
        // // ).oauth2ResourceServer(oauth2 -> oauth2
        // // .jwt(jwt ->
        // jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        // http.authorizeHttpRequests(request ->
        // request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
        // // .requestMatchers(HttpMethod.GET, "/users")
        // // .hasRole("Admin")
        // .anyRequest().authenticated());

        // http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfiguer ->
        // jwtConfiguer.decoder(jwtDecoder())
        // .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        // http.csrf(csrf -> csrf.disable());

        // http.cors(cors -> cors.configure(http));

        // return http.build();
        // }
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                System.out.println("Configuring SecurityFilterChain with OAuth2 login");
                // http.cors(cors -> cors.configure(http)) // Thêm dòng này để Spring Security
                // không chặn CORS
                // .authorizeHttpRequests(request -> request
                // .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                // .requestMatchers(HttpMethod.GET, "/auth/google", "/auth/oauth2/**")
                // .permitAll()
                // .requestMatchers("/login/oauth2/code/**").permitAll()
                // .anyRequest().authenticated())
                // .oauth2ResourceServer(oauth2 -> oauth2
                // .jwt(jwtConfiguer -> jwtConfiguer.decoder(jwtDecoder())
                // .jwtAuthenticationConverter(
                // jwtAuthenticationConverter())))
                // .oauth2Login(oauth2 -> oauth2
                // .authorizationEndpoint(auth -> auth
                // .baseUri("/auth/google"))
                // .redirectionEndpoint(red -> red
                // .baseUri("/login/oauth2/code/*"))
                // .defaultSuccessUrl("/auth/oauth2/success", true)
                // .failureUrl("/auth/oauth2/failure"))
                // .csrf(csrf -> csrf.disable());

                http.cors(cors -> cors.configure(http))
                                .authorizeHttpRequests(request -> request
                                                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                                                .requestMatchers(HttpMethod.GET, "/auth/google", "/auth/oauth2/**")
                                                .permitAll()
                                                .requestMatchers("/login/oauth2/code/**").permitAll()
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwtConfigurer -> jwtConfigurer
                                                                .decoder(jwtDecoder())
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter())))
                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(auth -> auth
                                                                .baseUri("/auth/google"))
                                                .redirectionEndpoint(red -> red
                                                                .baseUri("/login/oauth2/code/*"))
                                                .defaultSuccessUrl("/auth/oauth2/success", true)
                                                .failureUrl("/auth/oauth2/failure"))
                                .csrf(csrf -> csrf.disable());

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public JwtDecoder jwtDecoder() {
                SecretKeySpec keySpec = new SecretKeySpec(signerKey.getBytes(), "HS512"); // Tạo SecretKeySpec
                return NimbusJwtDecoder
                                .withSecretKey(keySpec)
                                .macAlgorithm(MacAlgorithm.HS512)
                                .build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                // JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                // converter.setJwtGrantedAuthoritiesConverter(jwt -> {
                // System.out.println("JWT Claims: " + jwt.getClaims()); // In toàn bộ claim để
                // kiểm tra

                // List<String> roles = jwt.getClaim("roles"); // Kiểm tra claim "roles"
                // if (roles == null)
                // return List.of();

                // return roles.stream()
                // .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Kiểm tra định
                // dạng
                // .collect(Collectors.toList());
                // });
                // return converter;

                JwtGrantedAuthoritiesConverter gConverter = new JwtGrantedAuthoritiesConverter();
                gConverter.setAuthorityPrefix("ROLE_");
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                converter.setJwtGrantedAuthoritiesConverter(gConverter);
                return converter;
        }

}
