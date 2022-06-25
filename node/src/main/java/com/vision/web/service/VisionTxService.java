package com.vision.web.service;

import com.vision.web.entity.VisionTx;

import java.util.List;

public interface VisionTxService {
  int insert(VisionTx visionTx);

  int update(VisionTx visionTx);

  VisionTx getById(Long id);

  VisionTx getByTxId(String txId);

  List<VisionTx> getByConfirmedAndDate(Long confirmed, Long sentAt);
}
