package com.arassec.igor.webgui.controller;

import com.arassec.igor.core.application.JobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * TODO: Document class.
 */
@Controller
public class JobsController {

    /**
     * Manages jobs.
     */
    @Autowired
    private JobManager jobManager;

    @RequestMapping("/jobs.html")
    public String getJobs(Model model) {
        model.addAttribute("jobs", jobManager.loadAll());
        return "jobs";
    }

}
