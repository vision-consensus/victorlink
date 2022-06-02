package com.vision.job;

import com.vision.job.adapters.HttpPostAdapter;
import com.vision.web.common.util.R;
import org.junit.Test;

public class HttpPostAdapterTest {
  @Test
  public void requestTest() {
    String url = "https://www.vanswap.org/info/vsPrice";
    HttpPostAdapter httpPostAdapter = new HttpPostAdapter(url, "result.vsPrice");
    R input = new R();
    R output = httpPostAdapter.perform(input);
    System.out.println(output);
  }
}
