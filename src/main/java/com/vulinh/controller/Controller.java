package com.vulinh.controller;

import com.vulinh.utils.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class Controller {

  @GetMapping
  public String hello() {
    return "Hello, World!";
  }

  @GetMapping("/admin")
  public String adminAccess() {
    return "Hello %s".formatted(SecurityUtils.getUserDetails().username());
  }
}
