package com.vision.web.mapper;

import com.vision.web.entity.TaskSpec;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskSpecsMapper {

  int insert(TaskSpec taskSpec);

  int insertList(List<TaskSpec> taskSpecs);

  List<TaskSpec> getByJobId(@Param("jobId") String jobId);

  TaskSpec getById(@Param("id") Long id);
}
