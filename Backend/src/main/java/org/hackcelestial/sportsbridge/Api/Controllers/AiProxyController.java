package org.hackcelestial.sportsbridge.Api.Controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiProxyController {

    @Value("${ai.service.url:http://localhost:8001}")
    private String aiBaseUrl;

    private final RestTemplate rest = new RestTemplate();

    @PostMapping("/run")
    public ResponseEntity<?> runWorkflow(@RequestBody Map<String, Object> body, @RequestHeader HttpHeaders headers) {
        // Forward JSON to AI service
        String url = aiBaseUrl + "/run-workflow";
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        // Optionally forward auth header if present (not required by AI service now)
        if (headers.containsKey("Authorization") && headers.getFirst("Authorization") != null) {
            h.set("Authorization", headers.getFirst("Authorization"));
        }
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, h);
        try {
            ResponseEntity<Object> resp = rest.postForEntity(url, req, Object.class);
            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
        } catch (Exception ex) {
            return ResponseEntity.status(502).body(Map.of("error", "AI service unavailable", "detail", ex.getMessage()));
        }
    }

    @GetMapping("/results/{id}")
    public ResponseEntity<?> getResult(@PathVariable("id") Long id) {
        String url = aiBaseUrl + "/ai-results/" + id;
        try {
            ResponseEntity<Object> resp = rest.getForEntity(url, Object.class);
            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
        } catch (Exception ex) {
            return ResponseEntity.status(502).body(Map.of("error", "AI service unavailable", "detail", ex.getMessage()));
        }
    }

    @GetMapping("/sponsor-recommendations")
    public ResponseEntity<?> sponsorRecommendations() {
        String url = aiBaseUrl + "/sponsor-recommendations";
        try {
            ResponseEntity<Object> resp = rest.getForEntity(url, Object.class);
            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
        } catch (Exception ex) {
            return ResponseEntity.status(502).body(Map.of("error", "AI service unavailable", "detail", ex.getMessage()));
        }
    }
}
