package com.ecommerce.app.util;

import com.ecommerce.app.config.AppEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for exporting configuration settings to the frontend
 * This allows us to centralize configuration and avoid hardcoding values in JavaScript files
 */
@Component
public class ConfigurationExporter {

    @Autowired
    private AppEnvironment appEnvironment;
    
    /**
     * Returns a map of configuration values that can be safely exposed to the frontend
     * Sensitive settings like database credentials are excluded
     * 
     * @return Map of configuration key-value pairs
     */
    public Map<String, Object> getPublicConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // API configuration
        Map<String, Object> api = new HashMap<>();
        api.put("baseUrl", appEnvironment.getApiBaseUrl());
        config.put("api", api);
        
        // Feature flags
        Map<String, Object> features = new HashMap<>();
        features.put("enableWishlist", Boolean.parseBoolean(System.getProperty("app.features.enable-wishlist", "true")));
        features.put("enableProductComparison", Boolean.parseBoolean(System.getProperty("app.features.enable-product-comparison", "false")));
        features.put("enableRecentlyViewed", Boolean.parseBoolean(System.getProperty("app.features.enable-recently-viewed", "true")));
        
        // Only expose debug tools if debug mode is enabled
        Map<String, Object> debug = new HashMap<>();
        if (appEnvironment.isDebugEnabled()) {
            debug.put("enableLogging", true);
            debug.put("showDevTools", true);
        } else {
            debug.put("enableLogging", false);
            debug.put("showDevTools", false);
        }
        features.put("debug", debug);
        
        config.put("features", features);
        
        // File upload settings
        Map<String, Object> fileUpload = new HashMap<>();
        fileUpload.put("allowedExtensions", appEnvironment.getAllowedFileExtensions());
        config.put("fileUpload", fileUpload);
        
        return config;
    }
    
    /**
     * Returns a JavaScript string that can be injected into the page
     * to provide configuration values to the frontend
     * 
     * @return JavaScript configuration string
     */
    public String getConfigAsJavaScript() {
        Map<String, Object> config = getPublicConfig();
        StringBuilder js = new StringBuilder();
        
        js.append("// Server-generated configuration - do not modify\n");
        js.append("window.SERVER_CONFIG = ").append(mapToJsonString(config)).append(";\n");
        js.append("\n");
        js.append("// Merge server config with client config if it exists\n");
        js.append("if (window.APP_CONFIG) {\n");
        js.append("    window.APP_CONFIG = Object.assign({}, window.APP_CONFIG, window.SERVER_CONFIG);\n");
        js.append("}\n");
        
        return js.toString();
    }
    
    /**
     * Simple utility to convert a map to a JSON string
     * In a real application, you would use a proper JSON library like Jackson
     * 
     * @param map Map to convert
     * @return JSON string representation
     */
    private String mapToJsonString(Map<String, Object> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof String[]) {
                json.append("[");
                String[] arr = (String[]) value;
                for (int i = 0; i < arr.length; i++) {
                    if (i > 0) {
                        json.append(",");
                    }
                    json.append("\"").append(arr[i]).append("\"");
                }
                json.append("]");
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                json.append(mapToJsonString(nestedMap));
            }
        }
        
        json.append("}");
        return json.toString();
    }
}
