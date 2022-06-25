package com.vision.job.adapters;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.vision.common.Constant;
import com.vision.common.util.HttpUtil;
import com.vision.web.common.util.R;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ConvertVsAdapter extends BaseAdapter {

  @Getter
  private String url;
  @Getter
  private String path;

  public ConvertVsAdapter(String urlStr, String pathStr) {
    url = urlStr;
    path = pathStr;
  }

  @Override
  public String taskType() {
    return Constant.TASK_TYPE_CONVERT_VS;
  }

  @Override
  public R perform(R input) {
    R result = new R();
    String response = null;
    try {
      response = HttpUtil.requestWithRetry(url);
    } catch (IOException e) {
      log.warn("request failed, err:" + e.getMessage());
    }

    if (!Strings.isNullOrEmpty(response)) {
      try {
        JsonElement data = JsonParser.parseString(response);

        String[] paths = path.split("\\.");
        for (String key : paths) {
          if (data.isJsonArray()) {
            data = data.getAsJsonArray().get(Integer.parseInt(key));
          } else {
            data = data.getAsJsonObject().get(key);
          }
        }
        double value = data.getAsDouble();

        if (Math.abs(value) < 0.000000001) {
          result.replace("code", 1);
          result.replace("msg", "convert VS failed");
          log.warn("convert VS failed, value : " + value + ", url : " + url);
        } else {
          value = (double) input.get("result") / value;
          result.put("result", value);
        }
      } catch (Exception e) {
        result.replace("code", 1);
        result.replace("msg", "convert VS failed");
        log.warn("parse response failed, url:" + url);
      }
    } else {
      result.replace("code", 1);
      result.replace("msg", "convert VS failed");
      log.warn("request failed, url:" + url);
    }

    return result;
  }
}