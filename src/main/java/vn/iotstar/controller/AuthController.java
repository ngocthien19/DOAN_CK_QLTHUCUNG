package vn.iotstar.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.NguoiDung;
import vn.iotstar.model.*;
import vn.iotstar.repository.NguoiDungRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.OtpService;
import vn.iotstar.service.UserDetailsImpl;
import vn.iotstar.util.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginModel loginModel) {
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginModel.getEmail(), loginModel.getMatKhau())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate JWT
            String jwt = jwtUtil.generateJwtToken(authentication);
            
            // Get user details
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // Lấy thông tin người dùng từ database
            NguoiDung nguoiDung = nguoiDungRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            
            // Get role (remove ROLE_ prefix if exists)
            String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .orElse("USER");
            
            // Create response
            JwtResponse jwtResponse = JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(userDetails.getId())
                .username(nguoiDung.getTenNguoiDung())
                .email(userDetails.getEmail())
                .role(role)
                .build();
            
            return ResponseEntity.ok(ApiResponse.success(jwtResponse));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email hoặc mật khẩu không đúng"));
        }
    }

    // API gửi OTP
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody OtpRequest otpRequest) {
        ApiResponse<String> result = otpService.generateAndSendOtp(otpRequest);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    // API xác thực OTP và hoàn tất đăng ký
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        ApiResponse<String> result = otpService.verifyOtpAndRegister(
            request.getEmail(), 
            request.getOtpCode()
        );
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    // API đăng ký cũ (giữ lại để tương thích ngược)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody NguoiDungModel signUpModel) {
        ApiResponse<String> result = authService.registerUser(signUpModel);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}