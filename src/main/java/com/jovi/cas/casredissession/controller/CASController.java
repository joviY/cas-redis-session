package com.jovi.cas.casredissession.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CASController {
    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }

    @RequestMapping("index")
    public String index() {
        return "index";
    }
}
