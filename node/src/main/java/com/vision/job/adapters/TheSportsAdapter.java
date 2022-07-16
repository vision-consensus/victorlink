package com.vision.job.adapters;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.vision.client.EventRequest;
import com.vision.common.Constant;
import com.vision.common.util.HttpUtil;
import com.vision.web.common.util.JsonUtil;
import com.vision.web.common.util.R;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.vision.common.runtime.vm.DataWord;
import org.vision.common.utils.ByteArray;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TheSportsAdapter extends BaseAdapter {

    @Getter
    private String url;

    public TheSportsAdapter(String urlStr) {
        url = urlStr;
    }

    @Override
    public String taskType() {
        return Constant.TASK_TYPE_THESPORTS;
    }

    @Override
    public R perform(R input) {
        R result = new R();
        EventRequest event = JsonUtil.fromJson((String) input.get("params"), EventRequest.class);
        if (StringUtils.isBlank(event.getMatchId())){
            result.replace("code", 1);
            result.replace("msg", "none matchId");
            return result;
        }

        String response = null;
        try {
            response = HttpUtil.requestPostWithRetry(url);
        } catch (IOException e) {
            log.info("parse response failed, err:" + e.getMessage());
            result.replace("code", 1);
            result.replace("msg", "request failed, url:" + url);
            log.error("request failed, url:" + url);
        }
        if (!Strings.isNullOrEmpty(response)) {
            try {
                BigInteger matchId = new BigInteger(ByteArray.fromHexString(event.getMatchId()));
                JsonElement data = JsonParser.parseString(response);
                JsonArray results = data.getAsJsonObject().getAsJsonArray("results");
                for (int i = 0; i < results.size(); i++) {
                    JsonArray score = results.get(i).getAsJsonObject().getAsJsonArray("score");
                    String matchStr = score.get(0).getAsString();
                    BigInteger item = new BigInteger(matchStr.getBytes(StandardCharsets.UTF_8));
                    if (item.equals(matchId)){
                        int homeScore = score.get(2).getAsJsonArray().get(0).getAsInt();
                        int awayScore = score.get(3).getAsJsonArray().get(0).getAsInt();
                        int status = homeScore > awayScore ? 1 : homeScore == awayScore ? 2 : 3;
                        DataWord m = new DataWord(matchId.longValue());
                        DataWord s = new DataWord(status);
                        DataWord hs = new DataWord(homeScore);
                        DataWord as = new DataWord(awayScore);
                        System.out.println(m.toHexString() + s.toHexString() + hs.toHexString() + as.toHexString());
                        result.put("result", m.toHexString() + s.toHexString() + hs.toHexString() + as.toHexString());
                        break;
                    }
                }
            } catch (Exception e) {
                result.replace("code", 1);
                result.replace("msg", "parse response failed, url:" + url);
                log.info("parse response failed, url:" + url);
            }
        } else {
            result.replace("code", 1);
            result.replace("msg", "request failed, url:" + url);
            log.error("request failed, url:" + url);
        }

        return result;
    }
}
