package simple.simple_cti.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // Added import

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/dial") // Changed from /originate
    public String dialPage() { // Changed method name for clarity
        return "dial"; // Returns dial.html
    }

    @PostMapping("/navigateToDial") // New method to handle navigation from home
    public String navigateToDial() {
        return "redirect:/dial"; // Redirects to the /dial GET endpoint
    }
}