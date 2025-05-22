package com.ecommerce.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for debugging template existence and configuration.
 * This is only for development purposes and should be disabled in production.
 */
@Controller
@RequestMapping("/debug")
public class TemplateExistenceController {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @GetMapping("/check-template")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkTemplate(@RequestParam String template) {
        Map<String, Object> result = new HashMap<>();
        result.put("template", template);
        
        try {
            // Check template resolvers
            StringBuilder resolverInfo = new StringBuilder();
            if (templateEngine != null && templateEngine.getTemplateResolvers() != null) {
                for (ITemplateResolver resolver : templateEngine.getTemplateResolvers()) {
                    resolverInfo.append(resolver.getClass().getSimpleName()).append(", ");
                }
            }
            result.put("resolvers", resolverInfo.toString());
            
            // Add generic information about the environment
            result.put("success", true);
            result.put("message", "Template resolver information retrieved");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            result.put("errorType", e.getClass().getName());
            return ResponseEntity.status(500).body(result);
        }
    }
}
