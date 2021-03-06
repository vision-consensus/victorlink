package com.vision.web.mapper;

import com.vision.web.entity.TaskRun;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskRunsMapper {

  int insert(TaskRun taskRun);

  TaskRun getById(@Param("id") String id);

  List<TaskRun> getByJobRunId(@Param("id") String id);

  int updateResult(@Param("id") String id, @Param("status") int status, @Param("result") String result, @Param("error") String error);
}
