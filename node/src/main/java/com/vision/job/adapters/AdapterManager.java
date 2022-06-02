package com.vision.job.adapters;

import com.vision.common.Constant;
import com.vision.web.common.util.JsonUtil;
import com.vision.web.entity.TaskParams;
import com.vision.web.entity.TaskSpec;

public class AdapterManager {

  public static BaseAdapter getAdapter(TaskSpec taskSpec) {
    BaseAdapter adapter = null;
    TaskParams params = JsonUtil.json2Obj(taskSpec.getParams(), TaskParams.class);
    switch (taskSpec.getType()){
      case Constant.TASK_TYPE_HTTP_GET:
        adapter = new HttpGetAdapter(params.getGet(), params.getPath());
        break;
      case Constant.TASK_TYPE_HTTP_POST:
        adapter = new HttpPostAdapter(params.getGet(), params.getPath());
        break;
      case Constant.TASK_TYPE_CONVERT_VS:
        adapter = new ConvertVsAdapter(params.getGet(), params.getPath());
        break;
      case Constant.TASK_TYPE_MULTIPLY:
        adapter = new MultiplyAdapter(params.getTimes());
        break;
      case Constant.TASK_TYPE_CONVERT_USD:
        adapter = new ConvertUsdAdapter();
        break;
      case Constant.TASK_TYPE_VISION_TX:
        String visionTxType = "";
        try {
          visionTxType = params.getType();
        } catch (Exception e) {
          visionTxType = "";
        }
        if (params != null) {
          adapter = new VisionTxAdapter(params.getVersion(), visionTxType);
        } else {
          adapter = new VisionTxAdapter(null, visionTxType);
        }
        break;
      case Constant.TASK_TYPE_RECIPROCAL:
        adapter = new ReciprocalAdapter();
        break;
      case Constant.TASK_TYPE_VAN_SWAP:
        adapter = new VanSwapAdapter(params.getPair(), params.getPool(), params.getVrc20());
        break;
      case Constant.TASK_TYPE_VS_TO_USDT:
        adapter = new ConvertUsdtAdapter();
        break;
      case Constant.TASK_TYPE_CACHE:
        adapter = new CacheAdapter();
        break;
      case Constant.TASK_TYPE_RANDOM:
        adapter = new RandomAdapter(params.getPublicKey());
        break;
      default:
        break;
    }

    return adapter;
  }
}
