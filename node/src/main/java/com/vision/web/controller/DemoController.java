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
@RequestMapping("/demo")
@AllArgsConstructor
public class DemoController extends AbstractController {
  private final DemoService demoService;

  @RequestMapping("/create")
  public R create(@RequestBody Demo demo, HttpSession session) {
    int result = demoService.create(demo);
    if (result >= 0) {
      return R.ok().put("data", "");
    } else {
      return R.error(ResultStatus.Failed);
    }

  }

  @RequestMapping("/query")
  public R query(@RequestParam Map<String, Object> map, HttpSession session) {
    String key = (String) map.get("key");
    Demo demo = demoService.queryByKey(key);
    return R.ok().put("data", demo);
  }
}
