package com.vulinh.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/test")
public interface TestAPI {

  @GetMapping("/free")
  String free();

  @GetMapping
  String normalAccess();

  @GetMapping("/admin")
  String adminAccess();
}
