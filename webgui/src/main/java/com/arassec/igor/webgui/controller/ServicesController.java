package com.arassec.igor.webgui.controller;

import com.arassec.igor.core.application.ServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * TODO: Document class.
 */
@Controller
public class ServicesController {

    @Autowired
    private ServiceManager serviceManager;

    @RequestMapping("/services.html")
    public String getServices(Model model) {
        model.addAttribute("services", serviceManager.loadAll());
        return "services";
    }

}
