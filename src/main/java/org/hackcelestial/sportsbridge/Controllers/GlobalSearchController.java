package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Services.GlobalSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class GlobalSearchController {

    @Autowired
    private GlobalSearchService globalSearchService;

    @Autowired
    private HttpSession session;

    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> globalSearch(@RequestParam("query") String query) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            Map<String, Object> searchResults = globalSearchService.searchAll(query);

            response.put("success", true);
            response.put("results", searchResults);
            response.put("message", searchResults.get("totalResults") + " results found");

        } catch (Exception e) {
            System.out.println("Error in global search: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error performing search");
        }

        return ResponseEntity.ok(response);
    }
}
