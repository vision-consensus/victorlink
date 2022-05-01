package com.vision.job.adapters;

import com.vision.common.Constant;
import com.vision.job.adapters.ContractAdapter.TradePair;
import com.vision.web.common.util.R;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvertUsdtAdapter extends BaseAdapter {
  @Override
  public String taskType() {
    return Constant.TASK_TYPE_VS_TO_USDT;
  }

  @Override
  public R perform(R input) {
    R result = new R();
    try {
      double value = ContractAdapter.getTradePriceWithVS(TradePair.USDT_VS);
      if (Math.abs(value) < 0.000000001) {
        result.replace("code", 1);
        result.replace("msg", "convert USDT failed");
        log.info("convert USDT failed");
      } else {
        double price = 1/value * (double)input.get("result");
        result.put("result", price);
      }
    } catch (IOException e) {
      result.replace("code", 1);
      result.replace("msg", "get usdt-trx value failed");
      log.error("get usdt-trx value failed, msg: {}", e.getMessage());
    } catch (Exception e) {
      result.replace("code", 1);
      result.replace("msg", "convert USDT failed");
      log.error("convert USDT failed, msg: {}", e.getMessage());
    }

    return result;
  }
}
