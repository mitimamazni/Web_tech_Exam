/**
 * Application environment configuration
 * This file centralizes all environment-specific configuration
 * 
 * Usage:
 * - Development: default values are used
 * - Staging: override with environment variables
 * - Production: override with environment variables
 */

package com.ecommerce.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppEnvironment {
    
    // Database configuration
    @Value("${spring.datasource.url}")
    private String dbUrl;
    
    @Value("${spring.datasource.username}")
    private String dbUsername;
    
    // Security settings
    @Value("${app.security.jwt.secret:defaultSecretKey}")
    private String jwtSecret;
    
    @Value("${app.security.jwt.expiration:86400000}")
    private long jwtExpiration;
    
    // API rate limiting
    @Value("${app.api.rate-limit.enabled:false}")
    private boolean rateLimitEnabled;
    
    @Value("${app.api.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;
    
    // File upload settings
    @Value("${app.file-upload.base-path:${user.home}/ecommerce-uploads}")
    private String uploadBasePath;
    
    @Value("${app.file-upload.allowed-extensions:.jpg,.jpeg,.png,.gif}")
    private String[] allowedFileExtensions;
    
    // Debug settings
    @Value("${app.debug.enabled:false}")
    private boolean debugEnabled;
    
    // Email configuration (for order confirmations, etc.)
    @Value("${app.email.from-address:noreply@example.com}")
    private String emailFromAddress;
    
    @Value("${app.email.enable-html:true}")
    private boolean emailEnableHtml;
    
    // API base URL (for link generation in emails, etc.)
    @Value("${app.api.base-url:http://localhost:8080}")
    private String apiBaseUrl;
    
    // getters
    
    public String getDbUrl() {
        return dbUrl;
    }
    
    public String getDbUsername() {
        return dbUsername;
    }
    
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    public long getJwtExpiration() {
        return jwtExpiration;
    }
    
    public boolean isRateLimitEnabled() {
        return rateLimitEnabled;
    }
    
    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }
    
    public String getUploadBasePath() {
        return uploadBasePath;
    }
    
    public String[] getAllowedFileExtensions() {
        return allowedFileExtensions;
    }
    
    public boolean isDebugEnabled() {
        return debugEnabled;
    }
    
    public String getEmailFromAddress() {
        return emailFromAddress;
    }
    
    public boolean isEmailEnableHtml() {
        return emailEnableHtml;
    }
    
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
}
