package com.vims.security.controller;

import com.vims.security.config.AppProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@PreAuthorize("hasAnyRole('SECURITY_MANAGER','ADMIN')")
public class PolicyController {
    private final AppProperties properties;

    public PolicyController(AppProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/policy")
    public String view(Model model) {
        model.addAttribute("minLength", properties.getMinLength());
        model.addAttribute("requireUppercase", properties.isRequireUppercase());
        model.addAttribute("requireNumber", properties.isRequireNumber());
        return "policy";
    }

    @PostMapping("/policy")
    public String update(@RequestParam int minLength,
                         @RequestParam(defaultValue = "false") boolean requireUppercase,
                         @RequestParam(defaultValue = "false") boolean requireNumber,
                         Model model) {
        properties.setMinLength(minLength);
        properties.setRequireUppercase(requireUppercase);
        properties.setRequireNumber(requireNumber);
        model.addAttribute("message", "Policy updated (not persisted across restarts)");
        return view(model);
    }
}


