package com.vision.job;

import com.vision.client.EventRequest;
import com.vision.client.FluxAggregator;
import com.vision.client.OracleClient;
import com.vision.client.VrfEventRequest;
import com.vision.client.message.OracleRoundState;
import com.vision.keystore.KeyStore;
import com.vision.web.common.util.R;
import com.vision.web.entity.Initiator;
import com.vision.web.entity.JobSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JobSubscriber {
  public static JobRunner jobRunner;
  private List<String> jobSubscriberList = new ArrayList<>();

  @Autowired
  public JobSubscriber(JobRunner jobRunner) {
    JobSubscriber.jobRunner = jobRunner;
  }

  public boolean addJob(JobSpec jobSpec) {

    for (Initiator initiator : jobSpec.getInitiators()) {
      // register job subscription
      OracleClient.registerJob(initiator.getAddress(), jobSpec.getId(), initiator.getType());
    }
    jobSubscriberList.add(jobSpec.getId());
    return true;
  }

  public static void receiveLogRequest(EventRequest event) {
    // validate request
    if (event.getJobId() == null || event.getJobId().isEmpty()) {
      log.error("Job id in event request is empty");
      return;
    }

    if (event.getCallbackAddr() == null || event.getCallbackAddr().isEmpty() ||
        event.getCallbackFunctionId() == null || event.getCallbackFunctionId().isEmpty() ||
        event.getRequestId() == null || event.getRequestId().isEmpty() ||
        event.getContractAddr() == null || event.getContractAddr().isEmpty()) {
      log.error("Necessary parameters in event request is empty");
      return;
    }

    log.info("event: " + event);
    jobRunner.addJobRun(com.vision.web.common.util.JsonUtil.obj2String(event));
  }

  public static void receiveVrfRequest(VrfEventRequest event) {
    // validate request
    if (event.getJobId() == null || event.getJobId().isEmpty()) {
      log.error("Job id in VRF event request is empty");
      return;
    }

    if (event.getSeed() == null || event.getSeed().isEmpty() ||
        event.getKeyHash() == null || event.getKeyHash().isEmpty() ||
        event.getRequestId() == null || event.getRequestId().isEmpty() ||
        event.getContractAddr() == null || event.getContractAddr().isEmpty()) {
      log.error("Necessary parameters in  VRF event request is empty");
      return;
    }

    log.info("VRF event: " + event);
    jobRunner.addJobRun(com.vision.web.common.util.JsonUtil.obj2String(event));
  }

  public static void receiveNewRoundLog(String addr, String startBy, long roundId, long startAt) {
    log.info("receive event: roundId:{}, startBy:{}, startAt:{}", roundId, startBy, startAt);

    // validate request
    if (startBy == null || startBy.isEmpty()) {
      log.error("startBy in event request is empty");
      return;
    }

    // Ignore rounds we started
    if (KeyStore.getAddr().equals(startBy)) {
      log.info("Ignoring new round request: we started this round, contract:{}, roundId:{}", addr, roundId);
      return;
    }

    OracleRoundState roundState = FluxAggregator.getOracleRoundState(addr, roundId);
    boolean checkResult = FluxAggregator.checkOracleRoundState(roundState);
    if (checkResult) {
      jobRunner.addJobRunV2(addr, roundId, startBy, startAt, roundState.getPaymentAmount());
    }
  }

  public static void setup() {
    List<Initiator> initiators = jobRunner.getAllJobInitiatorList();
    for (Initiator initiator : initiators) {
      OracleClient.registerJob(initiator.getAddress(), initiator.getJobSpecID(), initiator.getType());
    }
  }

  public static Long getJobResultById(String jobId) {
    R result = jobRunner.getJobResultById(jobId);
    if (result.get("code").equals(0)) {
      return (Long) result.get("result");
    } else {
      return 0L;
    }
  }
}
