package simple.simple_cti.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import simple.simple_cti.ami.AmiConnectionManager;

@Controller
public class HomeController {

    private final AmiConnectionManager amiConnectionManager;

    public HomeController(AmiConnectionManager amiConnectionManager) {
        this.amiConnectionManager = amiConnectionManager;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("host", amiConnectionManager.getHost());
        model.addAttribute("username", amiConnectionManager.getUsername());
        model.addAttribute("connectionState", amiConnectionManager.getConnectionState());
        model.addAttribute("asteriskVersion", amiConnectionManager.getAsteriskVersion());
        return "home";
    }

    @GetMapping("/dial")
    public String dialPage() { // Changed method name for clarity
        return "dial"; // Returns dial.html
    }
}