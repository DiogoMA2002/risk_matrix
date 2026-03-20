package ipleiria.risk_matrix.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards all non-API, non-static routes to index.html so Vue Router
 * can handle navigation in HTML5 history mode.
 */
@Controller
public class SpaController {

    @GetMapping(value = {
            "/",
            "/login",
            "/admin",
            "/admin/**",
            "/user",
            "/risk-info",
            "/requirements",
            "/questions/**",
            "/category",
            "/feedback-form"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
