package com.vision.job.adapters;

import static com.vision.common.Constant.HTTP_EVENT_HOST;
import static com.vision.common.Constant.VS_DECIMAL_STR;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.vision.common.Constant;
import com.vision.common.util.AbiUtil;
import com.vision.common.util.HttpUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.spongycastle.util.encoders.Hex;

public class ContractAdapter {

  private static final String VISIONGRID_HOST = "infragrid.v.network";
  private static final String GET_ACCOUNT = "/walletsolidity/getaccount";
  private static final String TRIGGET_CONSTANT = "/wallet/triggerconstantcontract";
  private static final String READONLY_ACCOUNT = "";

  private static final String BALANCE_OF = "balanceOf(address)";
  private static final String DECIMAL = "decimals()";

  public static long getVSBalance(String addr) throws Exception {
    return getVSBalance(addr, true, false);
  }

  public static long getVSBalance(String addr, boolean visible, boolean flexibleHost) throws Exception {
    Map<String, Object> params = Maps.newHashMap();
    params.put("address", addr);
    params.put("visible", visible);
    String response = null;
    if (flexibleHost) {
      response = HttpUtil.post("https", Constant.FULLNODE_HOST, GET_ACCOUNT, params);
    } else {
      response = HttpUtil.post("https", VISIONGRID_HOST, GET_ACCOUNT, params);
    }
    ObjectMapper mapper = new ObjectMapper();
    assert response != null;
    Map<String, Object> result = mapper.readValue(response, Map.class);
    return Optional.ofNullable(result.get("balance"))
            .map(balance -> {
              if (balance instanceof Integer) {
                return ((Integer) balance).longValue();
              }
              return (long)balance;
            })
            .orElse(0L);
  }

  public static BigInteger balanceOf(String ownerAddress, String contractAddress) throws Exception {
    return balanceOf(ownerAddress, contractAddress, true);
  }

  public static BigInteger balanceOf(String ownerAddress, String contractAddress, boolean visible) throws Exception {
    if (!visible) {
      throw new UnsupportedOperationException("not supported yet");
    }
    String param = AbiUtil.parseParameters(BALANCE_OF, Arrays.asList(ownerAddress));
    Map<String, Object> params = Maps.newHashMap();
    params.put("owner_address", ownerAddress);
    params.put("contract_address", contractAddress);
    params.put("function_selector", BALANCE_OF);
    params.put("parameter", param);
    params.put("visible", visible);
    String response = HttpUtil.post(
            "https", VISIONGRID_HOST, TRIGGET_CONSTANT, params);
    ObjectMapper mapper = new ObjectMapper();
    assert response != null;
    Map<String, Object> result = mapper.readValue(response, Map.class);
    return Optional.ofNullable((List<String>)result.get("constant_result"))
            .map(constantResult -> constantResult.get(0))
            .map(Hex::decode)
            .map(Hex::toHexString)
            .map(str -> new BigInteger(str, 16))
            .orElse(new BigInteger("0"));
  }

  public static int getDecimal(String contractAddress) throws Exception {
    return getDecimal(contractAddress, true);
  }

  public static int getDecimal(String contractAddress, boolean visible) throws Exception {
    if (!visible) {
      throw new UnsupportedOperationException("not supported yet");
    }
    String param = AbiUtil.parseParameters(DECIMAL, "");
    Map<String, Object> params = Maps.newHashMap();
    params.put("owner_address", READONLY_ACCOUNT);
    params.put("contract_address", contractAddress);
    params.put("function_selector", DECIMAL);
    params.put("parameter", param);
    params.put("visible", visible);
    String response = HttpUtil.post(
            "https", VISIONGRID_HOST, TRIGGET_CONSTANT, params);
    ObjectMapper mapper = new ObjectMapper();
    assert response != null;
    Map<String, Object> result = mapper.readValue(response, Map.class);
    return Optional.ofNullable((List<String>)result.get("constant_result"))
            .map(constantResult -> constantResult.get(0))
            .map(Hex::decode)
            .map(Hex::toHexString)
            .map(str -> new BigInteger(str, 16))
            .map(BigInteger::intValue)
            .orElse(0);
  }

  // todo 1. rename  2. check handle exception when blance is 0
  public static double getTradePriceWithVS(TradePair pair) throws Exception {
    return getTradePriceWithVS(pair.getPoolAddr(), pair.getVrc20Addr());
  }

  public static double getTradePriceWithVS(String poolAddr, String vrc20Addr) throws Exception {
    // 1. get vs balance
    BigDecimal vsBalance = new BigDecimal(getVSBalance(poolAddr));
    vsBalance = vsBalance.divide(new BigDecimal(VS_DECIMAL_STR), 4, RoundingMode.HALF_UP);
    // 2. get trc20 decimal
    int decimals = getDecimal(vrc20Addr);
    StringBuilder strDecimals = new StringBuilder("1");
    while (--decimals >= 0) {
      strDecimals.append("0");
    }
    // 3. get vrc20 balance
    BigDecimal vrc20balance = new BigDecimal(balanceOf(poolAddr, vrc20Addr));
    vrc20balance = vrc20balance.divide(new BigDecimal(strDecimals.toString()), 4, RoundingMode.HALF_UP);

    return vsBalance.divide(vrc20balance, 8, RoundingMode.HALF_UP).doubleValue();
  }

  public enum TradePair {
    VAN_VS("", ""),
    SUN_VS("", ""),
    VCT_VS("", ""),
    DICE_VS("", ""),
    BTC_VS("", ""),
    USDJ_VS("", ""),
    USDT_VS("", "");

    private String vrc20Addr; // the first vrc20 addr
    private String poolAddr;    // the trade pair addr in vanswap

    TradePair(String vrc20Addr, String poolAddr) {
      this.vrc20Addr = vrc20Addr;
      this.poolAddr = poolAddr;
    }

    public String getVrc20Addr() {
      return vrc20Addr;
    }

    public String getPoolAddr() {
      return poolAddr;
    }
  }
}
