package com.vision.job.adapter;

import static com.vision.job.adapters.ContractAdapter.TradePair.BTC_VS;
import static com.vision.job.adapters.ContractAdapter.TradePair.DICE_VS;
import static com.vision.job.adapters.ContractAdapter.TradePair.VAN_VS;
import static com.vision.job.adapters.ContractAdapter.TradePair.SUN_VS;
import static com.vision.job.adapters.ContractAdapter.TradePair.USDJ_VS;
import static com.vision.job.adapters.ContractAdapter.TradePair.WIN_VS;

import com.vision.job.adapters.ContractAdapter;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.Test;

public class ContractAdapterTest {

  @Test
  public void testGetTRXBalance() throws Exception {
    System.out.println(ContractAdapter.getVSBalance(
            "", true, false));
    System.out.println(ContractAdapter.getVSBalance(
            "", true, false));
  }

  @Test
  public void testGetBalance() throws Exception {
    BigInteger balance = ContractAdapter.balanceOf(
            "",
            "");
    System.out.println(balance.toString());
  }

  @Test
  public void testGetDecimal() throws Exception {
    System.out.println(ContractAdapter.getDecimal(VAN_VS.getVrc20Addr()));
    System.out.println(ContractAdapter.getDecimal(BTC_VS.getVrc20Addr()));
    System.out.println(ContractAdapter.getDecimal(DICE_VS.getVrc20Addr()));
    System.out.println(ContractAdapter.getDecimal(SUN_VS.getVrc20Addr()));
    System.out.println(ContractAdapter.getDecimal(WIN_VS.getVrc20Addr()));
  }

  @Test
  public void testGetTradePriceWithTRX() throws Exception {
    System.out.println(ContractAdapter.getTradePriceWithVS(USDJ_VS));
  }
}
