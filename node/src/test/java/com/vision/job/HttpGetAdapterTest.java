package com.vision.job;

import com.vision.job.adapters.ConvertUsdAdapter;
import com.vision.job.adapters.HttpGetAdapter;
import com.vision.web.common.util.R;
import org.junit.Test;

public class HttpGetAdapterTest {
  @Test
  public void requestTest() {
    String url = "https://poloniex.com/public?command=returnTicker";
    HttpGetAdapter httpGetAdapter = new HttpGetAdapter(url, "USDT_VS.last");
    R input = new R();
    R output = httpGetAdapter.perform(input);
    System.out.println(output);
  }
}
