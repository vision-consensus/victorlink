package com.vision.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vision.client.message.OracleRoundState;
import com.vision.common.AbiUtil;
import com.vision.common.Config;
import com.vision.common.ContractDecoder;
import com.vision.common.util.HttpUtil;
import com.vision.keystore.KeyStore;
import com.vision.web.entity.VisionTx;
import lombok.extern.slf4j.Slf4j;
import org.vision.visionjsdk.abi.datatypes.Bool;
import org.vision.visionjsdk.abi.datatypes.Int;
import org.vision.visionjsdk.abi.datatypes.Type;
import org.vision.visionjsdk.abi.datatypes.Uint;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static com.vision.common.Constant.*;

@Slf4j
public class FluxAggregator {

  /**
   * @param
   * @return transactionid
   */
  public static void submit(String addr, long roundId, long result, VisionTx tx)
      throws Exception {
    Map<String, Object> params = Maps.newHashMap();
    params.put("owner_address", KeyStore.getAddr());
    params.put("contract_address", addr);
    params.put("function_selector", SUBMIT_METHOD_SIGN);
    List<Object> list = Lists.newArrayList();
    list.add(roundId);
    list.add(result);
    params.put("parameter", AbiUtil.parseParameters(SUBMIT_METHOD_SIGN, list));
    params.put("fee_limit", Config.getMinFeeLimit());
    params.put("call_value", 0);
    params.put("visible", true);

    OracleClient.triggerSignAndResponse(params, tx);
  }

  public static OracleRoundState getOracleRoundState(String addr, long roundId) {
    OracleRoundState oracleRoundState = null;
    try {
      Map<String, Object> params = Maps.newHashMap();
      params.put("owner_address", KeyStore.getAddr());
      params.put("contract_address", addr);
      params.put("function_selector", ROUND_STATE_METHOD_SIGN);
      List<Object> list = Lists.newArrayList();
      list.add(KeyStore.getAddr());
      list.add(roundId);
      params.put("parameter", AbiUtil.parseParameters(ROUND_STATE_METHOD_SIGN, list));
      params.put("visible", true);

      String response = HttpUtil.post("https", FULLNODE_HOST,
          "/wallet/triggerconstantcontract", params);
      ObjectMapper mapper = new ObjectMapper();
      assert response != null;
      Map<String, Object> result = mapper.readValue(response, Map.class);

      // decode result
      List<Type> ret = ContractDecoder.decode(ROUND_STATE_RESULT_SIGN, ((List<String>) result.get("constant_result")).get(0));
      oracleRoundState = new OracleRoundState();
      oracleRoundState.setEligibleToSubmit(((Bool) ret.get(0)).getValue());
      oracleRoundState.setRoundId(((Uint) ret.get(1)).getValue().longValue());
      oracleRoundState.setLatestSubmission(((Int) ret.get(2)).getValue());
      oracleRoundState.setStartedAt(((Uint) ret.get(3)).getValue().longValue());
      oracleRoundState.setTimeout(((Uint) ret.get(4)).getValue().longValue());
      oracleRoundState.setAvailableFunds(((Uint) ret.get(5)).getValue());
      oracleRoundState.setOracleCount(((Uint) ret.get(6)).getValue().intValue());
      oracleRoundState.setPaymentAmount(((Uint) ret.get(7)).getValue());
    } catch (Exception e) {
      log.error("get oracle round state info error, msg:" + e.getMessage());
    }

    return oracleRoundState;
  }

  public static boolean checkOracleRoundState(OracleRoundState oracleRoundState) {
    System.out.println(oracleRoundState);
    if (oracleRoundState == null) {
      return false;
    }

    if (!oracleRoundState.getEligibleToSubmit()) {
      log.warn("not eligible to submit");
      return false;
    }

    if (oracleRoundState.getPaymentAmount() == null ||
        oracleRoundState.getPaymentAmount().compareTo(BigInteger.ZERO) == 0) {
      log.warn("PaymentAmount shouldn't be 0");
      return false;
    }

    BigInteger minFunds = oracleRoundState.getPaymentAmount().multiply(
        new BigInteger(String.valueOf(oracleRoundState.getOracleCount() * 3)));
    if (oracleRoundState.getAvailableFunds() == null ||
        minFunds.compareTo(oracleRoundState.getAvailableFunds()) > 0) {
      log.warn("aggregator is underfunded");
      return false;
    }

    return true;
  }
}
