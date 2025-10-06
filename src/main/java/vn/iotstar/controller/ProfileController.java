package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import vn.iotstar.entity.NguoiDung;
import vn.iotstar.repository.NguoiDungRepository;
import vn.iotstar.util.JwtUtil;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public String showProfile(HttpServletRequest request, Model model) {
        try {
            // Lấy token từ request
            String token = extractJwtFromRequest(request);
            
            if (token == null) {
                // Nếu không có token, chuyển hướng đến login
                return "redirect:/login";
            }
            
            if (!jwtUtil.validateJwtToken(token)) {
                // Token không hợp lệ
                return "redirect:/login";
            }
            
            String email = jwtUtil.getUserNameFromJwtToken(token);
            NguoiDung user = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            model.addAttribute("user", user);
            model.addAttribute("activeMenu", "user");
            
        } catch (Exception e) {
            System.err.println("Error loading profile: " + e.getMessage());
            return "redirect:/login";
        }
        
        return "user/profile";
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        // Thử lấy từ header
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // Thử lấy từ parameter (cho web)
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            return tokenParam;
        }
        
        // Thử lấy từ session attribute (nếu có)
        String token = (String) request.getSession().getAttribute("jwtToken");
        if (token != null) {
            return token;
        }
        
        // Thử lấy từ cookie
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
}