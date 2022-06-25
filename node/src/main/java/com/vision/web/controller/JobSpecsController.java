package com.vision.web.controller;

import com.vision.common.Constant;
import com.vision.common.VisionException;
import com.vision.job.JobCache;
import com.vision.job.JobSubscriber;
import com.vision.web.common.ResultStatus;
import com.vision.web.common.util.R;
import com.vision.web.entity.Initiator;
import com.vision.web.entity.JobSpec;
import com.vision.web.entity.JobSpecRequest;
import com.vision.web.entity.TaskSpec;
import com.vision.web.mapper.InitiatorMapper;
import com.vision.web.service.JobSpecsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/job")
@AllArgsConstructor
@CrossOrigin
public class JobSpecsController {
  private JobSpecsService jobSpecsService;
  private JobSubscriber jobSubscriber;
  private JobCache jobCache;
  private InitiatorMapper initiatorMapper;

  @GetMapping("/specs")
  public R index(
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "10") int size
  ) {
    try {
      List<JobSpec> result = jobSpecsService.getJobList(page, size);
      long count = jobSpecsService.getJobCount();
      return R.ok().put("data", result).put("count", count);
    } catch (Exception e) {
      log.error("get job list failed, error : " + e.getMessage());
      return R.error(ResultStatus.GET_JOB_LIST_FAILED);
    }
  }

  @PostMapping("/specs")
  public R create(@RequestBody JobSpecRequest jobSpecRequest) {
    try {
      JobSpec result = jobSpecsService.insert(jobSpecRequest);

      if (result != null) {
        jobSubscriber.addJob(result);

        if (jobCache.isCacheEnable()) {
          for (TaskSpec taskSpec : result.getTaskSpecs()) {
            if (taskSpec.getType().equals(Constant.TASK_TYPE_CACHE)) {
              jobCache.addToCacheList(result.getId());
            }
          }
        }
        return R.ok().put("data", "");
      } else {
        return R.error(ResultStatus.CREATE_JOB_FAILED);
      }
    } catch (VisionException te) {
      return R.error(11000, te.getMessage());
    } catch (Exception e) {
      log.error("create job failed, error : " + e.getMessage());
      return R.error(ResultStatus.CREATE_JOB_FAILED);
    }
  }

  @RequestMapping(value = "/specs/{jobId}", method = RequestMethod.GET)
  public R getJobById(@PathVariable("jobId") String jobId) {
    try {
      JobSpec jobSpec = jobSpecsService.getById(jobId);

      return R.ok().put("data", jobSpec);
    } catch (Exception e) {
      log.error("get job detail failed, error : " + e.getMessage());
      return R.error(ResultStatus.GET_JOB_DETAIL_FAILED);
    }
  }

  @DeleteMapping(value = "/specs/{jobId}")
  public R delete(@PathVariable("jobId") String jobId) {
    try {
      jobSpecsService.deleteJob(jobId);
      return R.ok();
    } catch (Exception e) {
      log.error("delete job failed, error : " + e.getMessage());
      return R.error(ResultStatus.ARCHIVE_JOB_FAILED);
    }
  }

  @GetMapping(value = "/result/{jobId}")
  public R getJobResult(@PathVariable("jobId") String jobId) {
    try {
      Long value = JobSubscriber.getJobResultById(jobId);

      return R.ok().put("data", value);
    } catch (Exception e) {
      log.error("get job result failed, jobId:" + jobId + ", error : " + e.getMessage());
      return R.error(ResultStatus.GET_JOB_DETAIL_FAILED).put("data", 0);
    }
  }

  @GetMapping(value = "/cache/{jobId}")
  public R getJobCache(@PathVariable("jobId") String jobId) {
    try {
      Queue<Long> valueList = jobCache.getValueListByJobId(jobId);
      if (valueList != null) {
        return R.ok().put("data", valueList.toArray());
      } else {
        JobSpec jobSpec = jobSpecsService.getById(jobId);
        if (jobCache.isCacheEnable()) {
          for (TaskSpec taskSpec : jobSpec.getTaskSpecs()) {
            if (taskSpec.getType().equals(Constant.TASK_TYPE_CACHE)) {
              jobCache.addToCacheList(jobId);
              //System.out.println("add to cache list");
            }
          }
        }
        return R.ok().put("data", new ArrayList<>());
      }
    } catch (Exception e) {
      log.error("get job cache failed, jobId:" + jobId + ", error : " + e.getMessage());
      return R.error(ResultStatus.GET_JOB_DETAIL_FAILED).put("data", 0);
    }
  }

  @GetMapping(value = "/active/{address}")
  public R getActiveJob(@PathVariable("address") String address) {
    Initiator initiator = initiatorMapper.getByAddress(address);
    if (initiator == null) {
      return R.error("initiator not exists");
    }

    return R.ok().put("data", initiator.getJobSpecID());
  }

  @GetMapping(value = "/active")
  public R getAllActiveJobs() {
    List<Initiator> initiators = JobSubscriber.jobRunner.getAllJobInitiatorList();
    Set<String> addresses = initiators.stream().map(Initiator::getAddress).collect(Collectors.toSet());

    List<String> jobIds = new ArrayList<>(addresses.size());
    addresses.forEach(each -> {
      Initiator initiator = initiatorMapper.getByAddress(each);
      if (initiator != null) {
        jobIds.add(initiator.getJobSpecID());
      }
    });

    return R.ok().put("data", jobIds);
  }
}
