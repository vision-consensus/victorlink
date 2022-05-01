package com.vision.job.adapter;

import com.vision.job.adapters.VanSwapAdapter;
import com.vision.web.common.util.R;
import org.junit.Test;

public class VanSwapAdapterTest {
  @Test
  public void testVanSwapPrice() {
    VanSwapAdapter justSwapAdapter = new VanSwapAdapter("usdt-trx", null, null);
    R input = new R();
    R output = justSwapAdapter.perform(input);
    System.out.println(output);
  }

  @Test
  public void testVanSwapPriceByAddr() {
    VanSwapAdapter justSwapAdapter = new VanSwapAdapter("", "", "");
    R input = new R();
    R output = justSwapAdapter.perform(input);
    System.out.println(output);
  }
}
