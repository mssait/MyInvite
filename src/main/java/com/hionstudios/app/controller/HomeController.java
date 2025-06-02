package com.hionstudios.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {
    @GetMapping({
            "/",
            "index.html",
            "/{x:[\\w\\-]+}",
            "/{x:[\\w\\-]+}/{y:[\\w\\-]+}",
            "/{x:[\\w\\-]+}/{y:[\\w\\-]+}/{z:[\\w\\-]+}",
            "/{x:[\\w\\-]+}/{y:[\\w\\-]+}/{z:[\\w\\-]+}/{x1:[\\w\\-]+}",
            "/{x:[\\w\\-]+}/{y:[\\w\\-]+}/{z:[\\w\\-]+}/{x1:[\\w\\-]+}/{x2:[\\w\\-]+}"
    })
    public ModelAndView home(
            @PathVariable(required = false) String x,
            @PathVariable(required = false) String y) {
        return new ModelAndView("index");
    }
}
