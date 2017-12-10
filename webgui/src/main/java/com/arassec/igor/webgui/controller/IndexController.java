package com.arassec.igor.webgui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Serves the index page of the web ui.
 */
@Controller
public class IndexController {

    /**
     * Redirects to the index page on a GET request.
     *
     * @return The redirect-directive.
     */
    @RequestMapping("/")
    public String redirectIndex() {
        return "redirect:/index.html";
    }

    /**
     * Serves the index page on a GET request.
     *
     * @param model The page model.
     * @return The name of the view to render.
     */
    @RequestMapping("/index.html")
    public String getIndex(Model model) {
        model.addAttribute("message", "Igor rulez!");
        return "index";
    }

}
