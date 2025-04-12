package com.example.demo.service.Impl.AuthenticationImpl; // Định nghĩa package chứa class này

import java.text.ParseException; // Import ParseException để xử lý lỗi khi phân tích JWT
import java.time.Instant; // Import Instant để xử lý thời gian
import java.time.temporal.ChronoUnit; // Import ChronoUnit để xác định đơn vị thời gian
import java.util.Date; // Import Date để sử dụng trong JWT
import java.util.UUID; // Import UUID để tạo ID ngẫu nhiên cho JWT

import org.springframework.beans.factory.annotation.Value; // Import annotation Value để tiêm giá trị từ cấu hình
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder để mã hóa và kiểm tra mật khẩu
import org.springframework.security.oauth2.core.user.OAuth2User; // Import OAuth2User để xử lý xác thực OAuth2
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch

import com.example.demo.dto.req.Authentication.AuthenticationRequest; // Import DTO AuthenticationRequest để nhận thông tin đăng nhập
import com.example.demo.dto.req.Authentication.IntrospectRequest; // Import DTO IntrospectRequest để kiểm tra token
import com.example.demo.dto.req.Authentication.LogoutRequest; // Import DTO LogoutRequest để xử lý đăng xuất
import com.example.demo.dto.res.Authentication.AuthenticationResponse; // Import DTO AuthenticationResponse để trả về kết quả đăng nhập
import com.example.demo.dto.res.Authentication.IntrospectResponse; // Import DTO IntrospectResponse để trả về kết quả kiểm tra token
import com.example.demo.entity.User.InvalidatedToken; // Import lớp InvalidatedToken từ entity
import com.example.demo.entity.User.Role; // Import lớp Role từ entity
import com.example.demo.entity.User.User; // Import lớp User từ entity
import com.example.demo.repository.User.InvalidatedTokenRepository; // Import InvalidatedTokenRepository để truy vấn InvalidatedToken
import com.example.demo.repository.User.RoleRepository; // Import RoleRepository để truy vấn Role
import com.example.demo.repository.User.UserRepository; // Import UserRepository để truy vấn User
import com.example.demo.service.Authentication.AuthenticationService; // Import interface AuthenticationService mà lớp này triển khai
import com.nimbusds.jose.JOSEException; // Import JOSEException để xử lý lỗi liên quan đến JWT
import com.nimbusds.jose.JWSAlgorithm; // Import JWSAlgorithm để xác định thuật toán ký JWT
import com.nimbusds.jose.JWSHeader; // Import JWSHeader để tạo header cho JWT
import com.nimbusds.jose.JWSObject; // Import JWSObject để tạo đối tượng JWT
import com.nimbusds.jose.JWSVerifier; // Import JWSVerifier để xác minh chữ ký JWT
import com.nimbusds.jose.Payload; // Import Payload để tạo nội dung JWT
import com.nimbusds.jose.crypto.MACSigner; // Import MACSigner để ký JWT
import com.nimbusds.jose.crypto.MACVerifier; // Import MACVerifier để xác minh JWT
import com.nimbusds.jwt.JWTClaimsSet; // Import JWTClaimsSet để xây dựng claims cho JWT
import com.nimbusds.jwt.SignedJWT; // Import SignedJWT để xử lý JWT đã ký

import lombok.RequiredArgsConstructor; // Import annotation RequiredArgsConstructor để tự động tạo constructor với các field final
import lombok.experimental.NonFinal; // Import annotation NonFinal để đánh dấu field không phải final

@Service // Đánh dấu lớp này là một Spring Service
@RequiredArgsConstructor // Tự động tạo constructor để tiêm các dependency final
public class AuthenticationServiceImpl implements AuthenticationService { // Lớp triển khai interface
                                                                          // AuthenticationService

    private final UserRepository userRepository; // Khai báo biến UserRepository để tương tác với cơ sở dữ liệu User
    private final RoleRepository roleRepository; // Khai báo biến RoleRepository để tương tác với cơ sở dữ liệu Role
    private final PasswordEncoder passwordEncoder; // Khai báo biến PasswordEncoder để mã hóa và kiểm tra mật khẩu
    private final InvalidatedTokenRepository invalidatedTokenRepository; // Khai báo biến InvalidatedTokenRepository để
                                                                         // tương tác với cơ sở dữ liệu InvalidatedToken

    @NonFinal // Đánh dấu field này không phải final để có thể tiêm giá trị
    @Value("${jwt.signerKey}") // Tiêm giá trị của signerKey từ file cấu hình
    protected String SIGNER_KEY; // Khai báo biến lưu khóa ký JWT

    @Override // Ghi đè phương thức từ interface
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) { // Phương thức xác thực
                                                                                              // người dùng
        User user = userRepository.findByName(authenticationRequest.getName()) // Tìm User theo tên từ request
                .orElseThrow(() -> new RuntimeException("User name not found")); // Ném ngoại lệ nếu không tìm thấy user

        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), // Kiểm tra mật khẩu
                user.getPasswordHash()); // So sánh mật khẩu nhập vào với mật khẩu đã mã hóa

        if (!authenticated) // Kiểm tra nếu xác thực thất bại
            throw new RuntimeException("User login faild"); // Ném ngoại lệ nếu mật khẩu không khớp

        var token = generateToken(user); // Tạo token JWT cho user

        return AuthenticationResponse.builder() // Tạo đối tượng AuthenticationResponse
                .token(token) // Gán token vào response
                .authenticated(true) // Đánh dấu xác thực thành công
                .build(); // Trả về AuthenticationResponse
    }

    private String generateToken(User user) { // Phương thức tạo token JWT cho user
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512); // Tạo header JWT với thuật toán HS512

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder() // Xây dựng claims cho JWT
                .subject(String.valueOf(user.getId())) // Đặt subject là ID của user
                .issuer("ToanPhan.com") // Đặt issuer là tên miền
                .issueTime(new Date()) // Đặt thời gian phát hành là hiện tại
                .expirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS))) // Đặt thời gian hết hạn sau 1 giờ
                .jwtID(UUID.randomUUID().toString()) // Đặt ID ngẫu nhiên cho JWT
                .claim("scope", buildScope(user)) // Thêm claim scope với vai trò của user
                .build(); // Hoàn thành claims

        Payload payload = new Payload(jwtClaimsSet.toJSONObject()); // Tạo payload từ claims

        JWSObject jwsObject = new JWSObject(jwsHeader, payload); // Tạo đối tượng JWT với header và payload

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes())); // Ký JWT với khóa bí mật
            return jwsObject.serialize(); // Trả về chuỗi JWT đã ký
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi khi ký
            throw new RuntimeException(e); // Ném ngoại lệ với thông tin lỗi
        }
    }

    @Override // Ghi đè phương thức từ interface
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) throws JOSEException, ParseException { // Phương
                                                                                                                     // thức
                                                                                                                     // kiểm
                                                                                                                     // tra
                                                                                                                     // tính
                                                                                                                     // hợp
                                                                                                                     // lệ
                                                                                                                     // của
                                                                                                                     // token
        var token = introspectRequest.getToken(); // Lấy token từ request
        boolean isVaild = true; // Khởi tạo biến đánh dấu token hợp lệ

        try {
            verifyToken(token); // Xác minh token
        } catch (Exception e) { // Bắt ngoại lệ nếu token không hợp lệ
            isVaild = false; // Đánh dấu token không hợp lệ
        }

        return IntrospectResponse.builder() // Tạo đối tượng IntrospectResponse
                .vaild(isVaild) // Gán trạng thái hợp lệ
                .build(); // Trả về IntrospectResponse
    }

    private String buildScope(User user) { // Phương thức xây dựng scope cho JWT
        return user.getRole().getRoleName(); // Trả về tên vai trò của user
    }

    @Override // Ghi đè phương thức từ interface
    public void logout(LogoutRequest request) throws JOSEException, ParseException { // Phương thức xử lý đăng xuất
        var signToken = verifyToken(request.getToken()); // Xác minh token từ request

        String jit = signToken.getJWTClaimsSet().getJWTID(); // Lấy ID của JWT
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime(); // Lấy thời gian hết hạn của JWT

        InvalidatedToken invalidatedToken = InvalidatedToken.builder() // Tạo đối tượng InvalidatedToken
                .id(jit) // Gán ID của JWT
                .expiryTime(expiryTime) // Gán thời gian hết hạn
                .build(); // Hoàn thành đối tượng

        invalidatedTokenRepository.save(invalidatedToken); // Lưu InvalidatedToken vào cơ sở dữ liệu
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException { // Phương thức xác minh token JWT
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes()); // Tạo verifier với khóa bí mật

        SignedJWT signedJWT = SignedJWT.parse(token); // Phân tích token thành SignedJWT

        Date exprityTime = signedJWT.getJWTClaimsSet().getExpirationTime(); // Lấy thời gian hết hạn từ claims

        var verified = signedJWT.verify(verifier); // Xác minh chữ ký của JWT

        if (!(verified && exprityTime.after(new Date()))) // Kiểm tra nếu chữ ký không hợp lệ hoặc token đã hết hạn
            throw new RuntimeException("Token is not valid"); // Ném ngoại lệ nếu token không hợp lệ

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) { // Kiểm tra nếu token đã bị
                                                                                             // vô hiệu hóa
            throw new RuntimeException("Token is invalidated"); // Ném ngoại lệ nếu token đã bị vô hiệu
        }

        return signedJWT; // Trả về SignedJWT nếu hợp lệ
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    public AuthenticationResponse authenticateOAuth2(OAuth2User oAuth2User) { // Phương thức xác thực OAuth2
        String email = oAuth2User.getAttribute("email"); // Lấy email từ OAuth2User
        String name = oAuth2User.getAttribute("name"); // Lấy tên từ OAuth2User

        User user = userRepository.findByEmail(email).orElseGet(() -> { // Tìm User theo email hoặc tạo mới
            Role userRole = roleRepository.findByRoleName("User") // Tìm vai trò User
                    .orElseThrow(() -> new RuntimeException("Role not found")); // Ném ngoại lệ nếu không tìm thấy vai
                                                                                // trò
            User newU = User.builder() // Tạo đối tượng User mới
                    .email(email) // Gán email
                    .name(name) // Gán tên
                    .role(userRole) // Gán vai trò
                    .build(); // Hoàn thành đối tượng
            return userRepository.save(newU); // Lưu User mới vào cơ sở dữ liệu
        });

        var token = generateToken(user); // Tạo token JWT cho user
        return AuthenticationResponse.builder() // Tạo đối tượng AuthenticationResponse
                .token(token) // Gán token
                .authenticated(true) // Đánh dấu xác thực thành công
                .build(); // Trả về AuthenticationResponse
    }
}