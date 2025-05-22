package com.ecommerce.app.controller;

import com.ecommerce.app.util.ConfigurationExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice that adds common attributes to all views
 * This includes configuration settings that should be available to all templates
 */
@ControllerAdvice(annotations = Controller.class)
public class GlobalViewAdvice {
    
    @Autowired
    private ConfigurationExporter configExporter;
    
    /**
     * Adds configuration data to all view models
     * This includes server-side configuration that should be accessible in templates
     * 
     * @param model The model to add attributes to
     */
    @ModelAttribute
    public void addConfigData(Model model) {
        model.addAttribute("config", configExporter.getPublicConfig());
        model.addAttribute("configScript", configExporter.getConfigAsJavaScript());
    }
}
