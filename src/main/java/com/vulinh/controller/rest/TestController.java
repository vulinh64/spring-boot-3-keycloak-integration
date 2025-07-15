package com.vulinh.controller.rest;

import com.vulinh.controller.api.TestAPI;
import com.vulinh.utils.SecurityUtils;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController implements TestAPI {

  @Override
  public String free() {
    return "Hello!";
  }

  @Override
  public String hello() {
    return "Hello, World!";
  }

  @Override
  public String adminAccess() {
    return "Hello %s".formatted(SecurityUtils.getUserDetails().username());
  }
}
