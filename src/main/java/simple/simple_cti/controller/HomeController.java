package simple.simple_cti.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam; // Import RequestParam
import simple.simple_cti.ami.DialPlanService;

import java.util.ArrayList; // Import ArrayList
import java.util.List;     // Import List
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private final DialPlanService dialPlanService;

    public HomeController(DialPlanService dialPlanService) {
        this.dialPlanService = dialPlanService;
    }

    @GetMapping("/")
    public String home(Model model) {
        if (dialPlanService != null) {
            model.addAttribute("connectionState", dialPlanService.getConnectionState().toString());
        } else {
            model.addAttribute("connectionState", "NOT_CONNECTED");
        }
        return "home";
    }

    @GetMapping("/dialplan")
    public String viewDialplan(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageSize = 5;
        List<Map.Entry<String, String>> dialplanEntries = new ArrayList<>();
        int totalPages = 0;
        int totalContexts = 0;
        int startPage = 0; // For pagination display
        int endPage = 0;   // For pagination display

        if (dialPlanService != null) {
            Map<String, String> fullDialplan = dialPlanService.getDialplan();
            totalContexts = fullDialplan.size();
            List<Map.Entry<String, String>> allDialplanEntries = new ArrayList<>(fullDialplan.entrySet());

            totalPages = (int) Math.ceil((double) totalContexts / pageSize);

            // Ensure page is within valid bounds
            if (page < 0) {
                page = 0;
            } else if (page >= totalPages && totalPages > 0) {
                page = totalPages - 1;
            }

            int start = page * pageSize;
            int end = Math.min(start + pageSize, totalContexts);

            if (start < end) {
                dialplanEntries = allDialplanEntries.subList(start, end);
            } else {
                dialplanEntries = new ArrayList<>(); // No entries for this page
            }

            // Calculate pagination display bounds
            int maxPagesToShow = 5;
            startPage = Math.max(0, page - (maxPagesToShow / 2));
            endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);
            startPage = Math.max(0, endPage - maxPagesToShow + 1); // Adjust startPage if endPage was capped
        }

        model.addAttribute("dialplanEntries", dialplanEntries);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalContexts", totalContexts);
        model.addAttribute("startPage", startPage); // Pass calculated startPage
        model.addAttribute("endPage", endPage);     // Pass calculated endPage
        return "dialplan";
    }

    @GetMapping("/dialplan/{contextName}")
    public String viewDialplanDetail(@PathVariable String contextName, Model model) {
        String contextContent = "";
        if (dialPlanService != null) {
            contextContent = dialPlanService.getDialplan().getOrDefault(contextName, "Context not found.");
        }
        model.addAttribute("contextName", contextName);
        model.addAttribute("contextContent", contextContent);
        return "dialplan-detail";
    }
}