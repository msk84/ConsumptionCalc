package net.msk.consumptionCalc.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TemplateController {

    @Value("${spring.application.name}")
    String appName;

    public TemplateController() {
    }

    @GetMapping("/")
    public String homePage(final Model model) {
        model.addAttribute("appName", this.appName);
        return "index";
    }
}
