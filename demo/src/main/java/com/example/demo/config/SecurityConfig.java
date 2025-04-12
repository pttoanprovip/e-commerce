package com.example.demo.config; // Xác định package cho lớp cấu hình này

import javax.crypto.spec.SecretKeySpec; // Nhập SecretKeySpec để tạo khóa ký JWT

import org.springframework.beans.factory.annotation.Value; // Nhập Value để tiêm các thuộc tính từ file cấu hình
import org.springframework.context.annotation.Bean; // Nhập Bean để định nghĩa các bean trong Spring
import org.springframework.context.annotation.Configuration; // Nhập Configuration để đánh dấu đây là lớp cấu hình
import org.springframework.http.HttpMethod; // Nhập HttpMethod để chỉ định các phương thức yêu cầu HTTP
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Nhập EnableMethodSecurity để bật bảo mật cấp phương thức
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Nhập HttpSecurity để cấu hình bảo mật web
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Nhập EnableWebSecurity để kích hoạt Spring Security
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Nhập BCryptPasswordEncoder để mã hóa mật khẩu
import org.springframework.security.crypto.password.PasswordEncoder; // Nhập PasswordEncoder để cung cấp giao diện mã hóa mật khẩu
import org.springframework.security.oauth2.jose.jws.MacAlgorithm; // Nhập MacAlgorithm để chỉ định thuật toán ký JWT
import org.springframework.security.oauth2.jwt.JwtDecoder; // Nhập JwtDecoder để giải mã JWT
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder; // Nhập NimbusJwtDecoder để triển khai giải mã JWT
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter; // Nhập JwtAuthenticationConverter để chuyển đổi JWT thành thông tin xác thực
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter; // Nhập JwtGrantedAuthoritiesConverter để ánh xạ quyền từ JWT
import org.springframework.security.web.SecurityFilterChain; // Nhập SecurityFilterChain để cấu hình chuỗi bộ lọc bảo mật

@Configuration // Đánh dấu lớp này là một lớp cấu hình Spring
@EnableWebSecurity // Kích hoạt Spring Security cho ứng dụng
@EnableMethodSecurity // Kích hoạt bảo mật cấp phương thức (ví dụ: @PreAuthorize)
public class SecurityConfig {

        @Value("${jwt.signerKey}") // Tiêm giá trị của khóa ký JWT từ file cấu hình (application.properties/yaml)
        private String signerKey; // Lưu trữ khóa ký JWT dưới dạng chuỗi

        private final String[] PUBLIC_ENDPOINTS = { "/users", "/auth/token", "/auth/introspect", "/auth/logout", }; // Mảng
                                                                                                                    // chứa
                                                                                                                    // các
                                                                                                                    // endpoint
                                                                                                                    // công
                                                                                                                    // khai
                                                                                                                    // không
                                                                                                                    // yêu
                                                                                                                    // cầu
                                                                                                                    // xác
                                                                                                                    // thực

        @Bean // Đánh dấu phương thức này trả về một bean được quản lý bởi Spring
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { // Cấu hình chuỗi bộ lọc
                                                                                             // bảo mật
                System.out.println("Configuring SecurityFilterChain with OAuth2 login"); // In thông báo để debug khi
                                                                                         // cấu hình SecurityFilterChain
                http.cors(cors -> cors.configure(http)) // Kích hoạt và cấu hình CORS để cho phép các yêu cầu
                                                        // cross-origin
                                .authorizeHttpRequests(request -> request // Bắt đầu cấu hình các quy tắc xác thực cho
                                                                          // yêu cầu HTTP
                                                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll() // Cho
                                                                                                                // phép
                                                                                                                // tất
                                                                                                                // cả
                                                                                                                // yêu
                                                                                                                // cầu
                                                                                                                // POST
                                                                                                                // tới
                                                                                                                // các
                                                                                                                // endpoint
                                                                                                                // công
                                                                                                                // khai
                                                                                                                // mà
                                                                                                                // không
                                                                                                                // cần
                                                                                                                // xác
                                                                                                                // thực
                                                .requestMatchers(HttpMethod.GET, "/auth/google", "/auth/oauth2/**")
                                                .permitAll() // Cho phép tất cả yêu cầu GET tới các endpoint liên quan
                                                             // đến OAuth2 và Google mà không cần xác thực
                                                .requestMatchers("/login/oauth2/code/**").permitAll() // Cho phép tất cả
                                                                                                      // yêu cầu tới
                                                                                                      // endpoint xử lý
                                                                                                      // mã code OAuth2
                                                .anyRequest().authenticated()) // Yêu cầu xác thực cho tất cả các yêu
                                                                               // cầu khác
                                .oauth2ResourceServer(oauth2 -> oauth2 // Cấu hình server tài nguyên OAuth2
                                                .jwt(jwtConfigurer -> jwtConfigurer // Cấu hình xác thực JWT
                                                                .decoder(jwtDecoder()) // Sử dụng JwtDecoder để giải mã
                                                                                       // JWT
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter()))) // Sử
                                                                                                                // dụng
                                                                                                                // JwtAuthenticationConverter
                                                                                                                // để
                                                                                                                // chuyển
                                                                                                                // đổi
                                                                                                                // JWT
                                                                                                                // thành
                                                                                                                // thông
                                                                                                                // tin
                                                                                                                // xác
                                                                                                                // thực
                                .oauth2Login(oauth2 -> oauth2 // Cấu hình đăng nhập bằng OAuth2
                                                .authorizationEndpoint(auth -> auth // Cấu hình endpoint ủy quyền
                                                                .baseUri("/auth/google")) // Đặt URI cơ sở cho endpoint
                                                                                          // ủy quyền Google
                                                .redirectionEndpoint(red -> red // Cấu hình endpoint chuyển hướng
                                                                .baseUri("/login/oauth2/code/*")) // Đặt URI cơ sở cho
                                                                                                  // endpoint nhận mã
                                                                                                  // code OAuth2
                                                .defaultSuccessUrl("/auth/oauth2/success", true) // Đặt URL mặc định khi
                                                                                                 // đăng nhập thành công
                                                .failureUrl("/auth/oauth2/failure")) // Đặt URL khi đăng nhập thất bại
                                .csrf(csrf -> csrf.disable()); // Vô hiệu hóa CSRF vì API thường không cần nó

                return http.build(); // Xây dựng và trả về SecurityFilterChain
        }

        @Bean // Đánh dấu phương thức này trả về một bean được quản lý bởi Spring
        public PasswordEncoder passwordEncoder() { // Cung cấp bean PasswordEncoder
                return new BCryptPasswordEncoder(); // Trả về BCryptPasswordEncoder để mã hóa mật khẩu
        }

        @Bean // Đánh dấu phương thức này trả về một bean được quản lý bởi Spring
        public JwtDecoder jwtDecoder() { // Cung cấp bean JwtDecoder
                SecretKeySpec keySpec = new SecretKeySpec(signerKey.getBytes(), "HS512"); // Tạo SecretKeySpec từ khóa
                                                                                          // ký với thuật toán HS512
                return NimbusJwtDecoder // Tạo NimbusJwtDecoder
                                .withSecretKey(keySpec) // Đặt khóa bí mật
                                .macAlgorithm(MacAlgorithm.HS512) // Chỉ định thuật toán HS512
                                .build(); // Xây dựng và trả về JwtDecoder
        }

        @Bean // Đánh dấu phương thức này trả về một bean được quản lý bởi Spring
        public JwtAuthenticationConverter jwtAuthenticationConverter() { // Cung cấp bean JwtAuthenticationConverter
                JwtGrantedAuthoritiesConverter gConverter = new JwtGrantedAuthoritiesConverter(); // Tạo converter để
                                                                                                  // ánh xạ quyền từ JWT
                gConverter.setAuthorityPrefix("ROLE_"); // Đặt tiền tố "ROLE_" cho các quyền
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter(); // Tạo
                                                                                         // JwtAuthenticationConverter
                converter.setJwtGrantedAuthoritiesConverter(gConverter); // Gán converter quyền vào
                                                                         // JwtAuthenticationConverter
                return converter; // Trả về JwtAuthenticationConverter
        }
}