package com.vision.web.service;

import com.vision.web.entity.Head;

import java.util.List;

public interface HeadService {
  int insert(Head head);

  int update(Head head);

  List<Head> getByAddress(String address);
}
