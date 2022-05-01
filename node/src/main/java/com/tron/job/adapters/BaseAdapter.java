package com.vision.job.adapters;

import com.vision.web.common.util.R;

public abstract class BaseAdapter {
  abstract public String taskType();

  abstract public R perform(R input);
}
