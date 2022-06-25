package com.vision.web.mapper;

import com.vision.web.entity.Initiator;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface InitiatorMapper {

  int insert(Initiator initiator);

  int insertList(List<Initiator> initiators);

  List<Initiator> getByJobId(@Param("jobId") String jobId);

  Initiator getByAddress(@Param("addr") String addr);
}
