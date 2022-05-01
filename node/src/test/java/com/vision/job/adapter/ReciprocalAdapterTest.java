package com.vision.job.adapter;

import com.vision.job.adapters.ReciprocalAdapter;
import com.vision.web.common.util.R;
import org.junit.Test;

public class ReciprocalAdapterTest {

  @Test
  public void testReciprocal() {
    ReciprocalAdapter reciprocalAdapter = new ReciprocalAdapter();
    R input = new R();
    double value = 2;
    input.put("result", value);
    R output = reciprocalAdapter.perform(input);
    System.out.println(output);
  }

}
