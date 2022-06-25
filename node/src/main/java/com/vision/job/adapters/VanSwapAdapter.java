package com.vision.job.adapters;

import com.vision.common.Constant;
import com.vision.job.adapters.ContractAdapter.TradePair;
import com.vision.web.common.util.R;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VanSwapAdapter extends BaseAdapter {
  @Getter
  private String pair;
  @Getter
  private String pool;
  @Getter
  private String vrc20;

  public VanSwapAdapter(String pair, String pool, String vrc20) {
    this.pair = pair;
    this.pool = pool;
    this.vrc20 = vrc20;
  }

  @Override
  public String taskType() {
    return Constant.TASK_TYPE_VAN_SWAP;
  }

  @Override
  public R perform(R input) {
    R result = new R();
    double value = getPairPrice();
    if (Math.abs(value) > 0.000000001) {
      result.put("result", value);
    } else {
      log.error("get price from vanswap failed, pair:" + pair);
      return R.error(1, "get price from vanswap failed, pair:" + pair);
    }

    return result;
  }

  private double getPairPrice() {
    double result = 0;

    try {
      if (pair == null || pair.isEmpty()) {
        result = ContractAdapter.getTradePriceWithVS(pool, vrc20);
      } else {
        switch (pair) {
          case Constant.PAIR_TYPE_VAN_VS:
            result = ContractAdapter.getTradePriceWithVS(TradePair.VAN_VS);
            break;
          case Constant.PAIR_TYPE_BTC_VS:
            result = ContractAdapter.getTradePriceWithVS(TradePair.BTC_VS);
            break;
          case Constant.PAIR_TYPE_SUN_VS:
            result = ContractAdapter.getTradePriceWithVS(TradePair.SUN_VS);
            break;
          case Constant.PAIR_TYPE_VCT_VS:
            result = ContractAdapter.getTradePriceWithVS(TradePair.VCT_VS);
            break;
          case Constant.PAIR_TYPE_DICE_VS:
            result = ContractAdapter.getTradePriceWithVS(TradePair.DICE_VS);
            break;
          case Constant.PAIR_TYPE_USDJ_VS:
            result = ContractAdapter.getTradePriceWithVS(TradePair.USDJ_VS);
            break;
          case Constant.PAIR_TYPE_USDT_VS:
            result = ContractAdapter.getTradePriceWithVS(TradePair.USDT_VS);
            break;
          default:
            break;
        }
      }
    } catch (Exception e) {
      log.error("get pair price failed! msg:" + e.getMessage());
    }

    return result;
  }
}
