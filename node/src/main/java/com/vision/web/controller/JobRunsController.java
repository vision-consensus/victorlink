package com.vision.web.controller;

import com.vision.web.common.ResultStatus;
import com.vision.web.common.util.R;
import com.vision.web.entity.JobRun;
import com.vision.web.service.JobRunsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/job")
@AllArgsConstructor
@CrossOrigin
public class JobRunsController {
  JobRunsService jobRunsService;

  @GetMapping("/runs")
  public R index(
      @RequestParam(required = false, defaultValue = "") String id,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size
  ) {
    try {
      List<JobRun> jobRuns;
      long totalCount;
      if (id.isEmpty()) {
        jobRuns = jobRunsService.getRunList(page, size);
        totalCount = jobRunsService.getRunsCount(null);
      } else {
        jobRuns = jobRunsService.getRunListByJobId(id, page, size);
        totalCount = jobRunsService.getRunsCount(id);
      }

      return R.ok().put("data", jobRuns).put("count", totalCount);
    } catch (Exception e) {
      log.error("get job runs failed, error : " + e.getMessage());
      return R.error(ResultStatus.GET_JOB_RUNS_FAILED);
    }
  }
}
