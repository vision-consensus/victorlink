package com.vision.common;

import com.vision.keystore.KeyStore;

public class Constant {

  public static final long ONE_HOUR = 60 * 60 * 1000L;
  public static final long ONE_MINUTE = 60 * 1000L;

  public static final String READONLY_ACCOUNT = KeyStore.getAddr();

  public static final String TRIGGET_CONSTANT_CONTRACT = "/wallet/triggerconstantcontract";

  public static final String VS_DECIMAL_STR = "1000000";

  public static String HTTP_EVENT_HOST = "infragrid.v.network";
  public static String FULLNODE_HOST = "infragrid.v.network";

  public static void initEnv(String env) {
    if ("vtest".equals(env)) {
      HTTP_EVENT_HOST = "vtest.infragrid.v.network";
      FULLNODE_HOST = "vtest.infragrid.v.network";
    }
    if ("vpioneer".equals(env)) {
      HTTP_EVENT_HOST = "vpioneer.infragrid.v.network";
      FULLNODE_HOST = "vpioneer.infragrid.v.network";
    }
  }

  public static final int HTTP_MAX_RETRY_TIME = 3;

  public static final String FULFIL_METHOD_SIGN =
          "fulfillOracleRequest(bytes32,uint256,address,bytes4,uint256,bytes32)";
  public static final String VRF_FULFIL_METHOD_SIGN =
          "fulfillRandomnessRequest(bytes)";
  public static final String SUBMIT_METHOD_SIGN = "submit(uint256,int256)";
  public static final String ROUND_STATE_METHOD_SIGN = "oracleRoundState(address,uint32)";
  public static final String ROUND_STATE_RESULT_SIGN = "bool,uint32,int256,uint64,uint64,uint128,uint8,uint128";


  // task type
  public static final String TASK_TYPE_HTTP_GET = "httpget";
  public static final String TASK_TYPE_HTTP_POST = "httppost";
  public static final String TASK_TYPE_VISION_TX = "visiontx";
  public static final String TASK_TYPE_MULTIPLY = "multiply";
  public static final String TASK_TYPE_CONVERT_USD = "convertusd";
  public static final String TASK_TYPE_VS_TO_USDT = "vs2usdt";
  public static final String TASK_TYPE_RECIPROCAL = "reciprocal";
  public static final String TASK_TYPE_VAN_SWAP = "vanswap";
  public static final String TASK_TYPE_CACHE = "cache";
  public static final String TASK_TYPE_CONVERT_VS = "convertvs";
  public static final String TASK_TYPE_RANDOM = "random";

  // initiator type
  public static final String INITIATOR_TYPE_RUN_LOG = "runlog";
  public static final String INITIATOR_TYPE_RANDOMNESS_LOG = "randomnesslog";

  // pairs
  public static final String PAIR_TYPE_VAN_VS = "van-vs";
  public static final String PAIR_TYPE_SUN_VS = "sun-vs";
  public static final String PAIR_TYPE_VCT_VS = "vct-vs";
  public static final String PAIR_TYPE_DICE_VS = "dice-vs";
  public static final String PAIR_TYPE_BTC_VS = "btc-vs";
  public static final String PAIR_TYPE_USDJ_VS = "usdj-vs";
  public static final String PAIR_TYPE_USDT_VS = "usdt-vs";


  public static final long VisionTxUnstarted = 101L;
  public static final long VisionTxInProgress = 102L;
  public static final long VisionTxFatalError = 103L;
  public static final long VisionTxOutOfEnergy = 104L;
  public static final long VisionTxConfirmed = 105L;

}
