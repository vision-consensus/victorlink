package com.vision.job.adapters;

import com.vision.common.Constant;
import com.vision.common.util.AbiUtil;
import com.vision.web.common.util.R;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
public class PancakeAdapter extends BaseAdapter {

    //    private static final String JSON_RPC = "https://bsc-mainnet.web3api.com/v1/KBR2FY9IJ2IXESQMQ45X76BNWDAW2TT3Z3";
    private static final String RESERVES_FUNC = "0x" + AbiUtil.parseMethod("getReserves()", "");
    private static final String TOKEN0_FUNC = "0x" + AbiUtil.parseMethod("token0()", "");
    private static final String TOKEN1_FUNC = "0x" + AbiUtil.parseMethod("token1()", "");
    private static final String USDT_ADDRESS = "55d398326f99059ff775485246999027b3197955";
    private static final String WVS_ADDRESS = "cd76bc49a69bcdc5222d81c18d4a04dc8a387297";
    private static final BigDecimal WVS_DIGITS = new BigDecimal(6L);
    private static final BigDecimal USDT_DIGITS = new BigDecimal(18L);
    private String TOKEN0 = "";
    private String TOKEN1 = "";

    @Getter
    private String url;

    public PancakeAdapter(String url) {
        this.url = url; //JSON_RPC
    }

    @Override
    public String taskType() {
        return Constant.TASK_TYPE_PANCAKE;
    }

    @Override
    public R perform(R input) {
        R result = new R();
        String reserve = getJSONRPCResult(RESERVES_FUNC);
        if (StringUtils.isBlank(reserve)){
            result.replace("code", 1);
            result.replace("msg", "getReserves is blank");
            return result;
        }
        reserve = reserve.substring(2);
        String coin1 = reserve.substring(0, 64);
        String coin2 = reserve.substring(64, 128);
        BigInteger coin1BigInt = Numeric.toBigInt(coin1);
        BigInteger coin2BigInt = Numeric.toBigInt(coin2);
        //457274187363764275246756
        //451277514666
        if(StringUtils.isBlank(TOKEN0)){
            String token0 = getJSONRPCResult(TOKEN0_FUNC);
            if(StringUtils.isBlank(token0)){
                result.replace("code", 1);
                result.replace("msg", "token0 is blank");
                return result;
            }
            TOKEN0 = token0.substring(token0.length() - 40);
        }
        
        if(StringUtils.isBlank(TOKEN1)){
            String token1 = getJSONRPCResult(TOKEN1_FUNC);
            if(StringUtils.isBlank(token1)){
                result.replace("code", 1);
                result.replace("msg", "token1 is blank");
                return result;
            }
            TOKEN1 = token1.substring(token1.length() - 40);
        }

        if (TOKEN0.equals(USDT_ADDRESS) && TOKEN1.equals(WVS_ADDRESS)) {
            BigDecimal usdtNum = new BigDecimal(coin1BigInt).divide(BigDecimal.valueOf(Math.pow(10,USDT_DIGITS.longValue())), USDT_DIGITS.intValue(), BigDecimal.ROUND_DOWN);
            BigDecimal wvsNum = new BigDecimal(coin2BigInt).divide(BigDecimal.valueOf(Math.pow(10, WVS_DIGITS.longValue())), WVS_DIGITS.intValue(), BigDecimal.ROUND_DOWN);
            BigDecimal wvsToUsdt = usdtNum.divide(wvsNum, USDT_DIGITS.intValue(), BigDecimal.ROUND_DOWN);
            result.put("result", wvsToUsdt.stripTrailingZeros().toPlainString());
        }
        if (TOKEN0.equals(WVS_ADDRESS) && TOKEN1.equals(USDT_ADDRESS)){
            BigDecimal usdtNum = new BigDecimal(coin1BigInt).divide(BigDecimal.valueOf(Math.pow(10, WVS_DIGITS.longValue())), WVS_DIGITS.intValue(), BigDecimal.ROUND_DOWN);
            BigDecimal wvsNum = new BigDecimal(coin2BigInt).divide(BigDecimal.valueOf(Math.pow(10, USDT_DIGITS.longValue())), USDT_DIGITS.intValue(), BigDecimal.ROUND_DOWN);
            BigDecimal wvsToUsdt = usdtNum.divide(wvsNum, USDT_DIGITS.intValue(), BigDecimal.ROUND_DOWN);
            result.put("result", wvsToUsdt.stripTrailingZeros().toPlainString());
        }
        return result;
    }

    private String getJSONRPCResult(String data){
        try {
            Web3j web3 = Web3j.build(new HttpService(url));
            EthCall response = web3.ethCall(
                    Transaction.createEthCallTransaction(null, "0xb8b412ff0944cd1f17940336c88108341d83a539", data),
                    DefaultBlockParameterName.LATEST).send();
            return response.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
