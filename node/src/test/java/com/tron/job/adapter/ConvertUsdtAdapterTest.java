package com.vision.job.adapter;

import com.vision.job.adapters.ConvertUsdtAdapter;
import com.vision.web.common.util.R;
import org.junit.Test;

public class ConvertUsdtAdapterTest {

  @Test
  public void testConvertUsdt() {
    ConvertUsdtAdapter convertUsdtAdapter = new ConvertUsdtAdapter();
    R input = new R();
    double value = 2;
    input.put("result", value);
    R output = convertUsdtAdapter.perform(input);
    System.out.println(output);
  }
}
