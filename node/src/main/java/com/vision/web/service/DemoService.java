package com.vision.web.service;

import com.vision.web.entity.Demo;

public interface DemoService {

  int create(Demo demo);

  Demo queryByKey(String key);
}
