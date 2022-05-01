package com.vision.web.mapper;

import com.vision.web.entity.Head;
import org.apache.ibatis.annotations.Param;
import java.util.List;


public interface HeadMapper {

  int insert(Head head);

  int update(Head head);

  List<Head> getByAddress(@Param("address") String address);
}
