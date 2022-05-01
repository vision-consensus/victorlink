package com.vision.job.adapters;

import com.vision.common.Constant;
import com.vision.web.common.util.R;

public class CacheAdapter extends BaseAdapter {
  @Override
  public String taskType() {
    return Constant.TASK_TYPE_CACHE;
  }

  @Override
  public R perform(R input) {
    return new R();
  }
}
