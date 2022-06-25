package com.vision.web.service.impl;

import com.vision.web.entity.VisionTx;
import com.vision.web.mapper.TxesMapper;
import com.vision.web.service.VisionTxService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
@AllArgsConstructor
public class VisionTxServiceImpl implements VisionTxService {
  private TxesMapper txesMapper;

  public int insert(VisionTx visionTx) {
    return txesMapper.insert(visionTx);
  }

  public int update(VisionTx visionTx) {
    return txesMapper.update(visionTx);
  }

  public VisionTx getById(Long id) {
    return txesMapper.getById(id);
  }

  public VisionTx getByTxId(String txId) {
    return txesMapper.getByTxId(txId);
  }

  @Override
  public List<VisionTx> getByConfirmedAndDate(Long confirmed, Long sentAt) {
    return txesMapper.getByConfirmedAndDate(confirmed, sentAt);
  }
}
