package com.vision.job.adapters;

import com.vision.common.Constant;
import com.vision.web.common.util.R;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class ChainLinkAdapter  extends BaseAdapter {
    @Getter
    private String rpc;
    @Getter
    private String feedContract;

    public ChainLinkAdapter(String rpc, String feedContract){
        this.rpc = rpc;
        this.feedContract = feedContract;
    }

    @Override
    public String taskType() {
        return Constant.TASK_TYPE_CHAIN_LINK;
    }

    @Override
    public R perform(R input) {
        R result = new R();
        try{
//            String decimals = ContractAdapter.getDecimal(rpc, feedContract);
//            if(StringUtils.isBlank(decimals)) {
//                result.replace("code", 1);
//                result.replace("msg", "get decimals failed!");
//                return result;
//            }
//            BigInteger d = Numeric.toBigInt(decimals);
            String latestAnswer = ContractAdapter.getlatestAnswer(rpc, feedContract);
            if(StringUtils.isBlank(latestAnswer)) {
                result.replace("code", 1);
                result.replace("msg", "get latestAnswer failed!");
                return result;
            }
            BigInteger latestPrice = Numeric.toBigInt(latestAnswer);
            //double price = new BigDecimal(latestPrice).divide(new BigDecimal(d), d.intValue(), RoundingMode.DOWN).doubleValue();
            result.put("result", latestPrice.longValue());
        }catch(IOException e) {
            e.printStackTrace();
            result.replace("code", 1);
            result.replace("msg", e.getMessage());
        }finally {
            return result;
        }
    }
}
