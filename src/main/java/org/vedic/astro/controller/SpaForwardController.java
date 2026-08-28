package org.vedic.astro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {
            "/",
            "/panchangam",
            "/horoscope",
            "/matching",
            "/settings",
            "/{path:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
