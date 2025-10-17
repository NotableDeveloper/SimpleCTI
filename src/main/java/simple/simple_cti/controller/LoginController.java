package simple.simple_cti.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import simple.simple_cti.ami.AmiConnectionManager;
import simple.simple_cti.ami.AmiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;

@Controller
public class LoginController {

    private final Environment env;

    public LoginController(Environment env) {
        this.env = env;
    }

    @GetMapping("/")
    public String login() {
        return "index";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("username") String username, @RequestParam("password") String password, org.springframework.ui.Model model, HttpServletRequest request) {
        if ("admin".equals(username) && "admin".equals(password)) {
            AmiConnectionManager amiConnectionManager = new AmiConnectionManager(
                env.getProperty("asterisk.host"),
                env.getProperty("asterisk.username"),
                env.getProperty("asterisk.password")
            );
            amiConnectionManager.connect();
            AmiService amiService = new AmiService(amiConnectionManager);
            request.getSession().setAttribute("amiService", amiService);
            return "redirect:/home";
        } else {
            model.addAttribute("loginError", "Invalid username or password");
            return "index";
        }
    }

    @GetMapping("/home")
    public String home(org.springframework.ui.Model model, HttpServletRequest request) {
        AmiService amiService = (AmiService) request.getSession().getAttribute("amiService");
        if (amiService != null) {
            model.addAttribute("connectionState", amiService.getConnectionState().toString());
        } else {
            model.addAttribute("connectionState", "NOT_CONNECTED");
        }
        return "home";
    }
}
