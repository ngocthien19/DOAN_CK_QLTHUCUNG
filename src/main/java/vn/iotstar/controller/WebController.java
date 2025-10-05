package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/login")
    public String loginPage() {
        return "web/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "web/register";
    }
}