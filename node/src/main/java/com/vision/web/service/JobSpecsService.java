package com.vision.web.service;

import com.vision.common.VisionException;
import com.vision.web.entity.Initiator;
import com.vision.web.entity.JobSpec;
import com.vision.web.entity.JobSpecRequest;
import com.vision.web.entity.TaskSpec;
import java.util.List;

public interface JobSpecsService {

  JobSpec insert(JobSpecRequest jsr) throws VisionException;

  List<JobSpec> getJobList(int page, int size);

  JobSpec getById(String id);

  List<Initiator> getInitiatorsByJobId(String id);

  List<TaskSpec> getTasksByJobId(String id);

  TaskSpec getTasksById(Long id);

  List<JobSpec> getAllJob();

  int deleteJob(String jobId);

  long getJobCount();

  Initiator getInitiatorByAddress(String addr);
}
