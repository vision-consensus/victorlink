package com.vision.web.controller;

import com.vision.web.common.AbstractController;
import com.vision.web.common.ResultStatus;
import com.vision.web.common.util.R;
import com.vision.web.entity.Demo;
import com.vision.web.service.DemoService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class DemoController extends AbstractController {

  @RequestMapping("/health_check")
  public R health_check() {
    return R.ok("I'm OK!");
  }
}
