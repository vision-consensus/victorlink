package com.vision.web.mapper;

import com.vision.web.entity.VisionTx;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Date;

@Mapper
public interface TxesMapper {
  int insert(VisionTx visionTx);
  int update(VisionTx visionTx);

  VisionTx getById(@Param("id") Long id);

  VisionTx getByTxId(@Param("txId") String txId);

  List<VisionTx> getByConfirmedAndDate(@Param("confirmed") Long confirmed, @Param("sentAt") Long sentAt);
}
