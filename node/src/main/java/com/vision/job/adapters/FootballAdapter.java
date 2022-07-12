package com.vision.job.adapters;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.vision.client.FulfillRequest;
import com.vision.common.AbiUtil;
import com.vision.common.Constant;
import com.vision.common.util.HttpUtil;
import com.vision.web.common.util.R;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.vision.common.runtime.vm.DataWord;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static com.vision.common.Constant.FULFIL_METHOD_SIGN;

@Slf4j
public class FootballAdapter extends BaseAdapter {

    public static String matchId = "-1";

    @Getter
    private String url;

    public FootballAdapter(String urlStr) {
        url = urlStr;
    }

    @Override
    public String taskType() {
        return Constant.TASK_TYPE_THESPORTS;
    }

    @Override
    public R perform(R input) {
        R result = new R();
        if (StringUtils.isBlank(matchId) || "-1".equalsIgnoreCase(matchId)) {
            result.replace("code", 1);
            result.replace("msg", "none matchId");
            return result;
        }

        long homeScore = System.currentTimeMillis() % 5;
        long awayScore = System.currentTimeMillis() % 3;
        int status = homeScore > awayScore ? 1 : homeScore == awayScore ? 2 : 3;
        DataWord matchId = new DataWord(new BigInteger(FootballAdapter.matchId.getBytes(StandardCharsets.UTF_8)).longValue());
        DataWord s = new DataWord(status);
        DataWord hs = new DataWord(homeScore);
        DataWord as = new DataWord(awayScore);
        String hex = matchId.toHexString() + s.toHexString() + hs.toHexString() + as.toHexString();
        result.put("result", hex);

//        String response = null;
//        try {
//            response = HttpUtil.requestPostWithRetry(url);
//            System.out.println(response);
//        } catch (IOException e) {
//            log.info("parse response failed, err:" + e.getMessage());
//        }
//
//        if (!Strings.isNullOrEmpty(response)) {
//            try {
//                JsonElement data = JsonParser.parseString(response);
//                JsonArray results = data.getAsJsonObject().getAsJsonArray("results");
//                for (int i = 0; i < results.size(); i++) {
//                    JsonArray score = results.get(i).getAsJsonObject().getAsJsonArray("score");
//                    System.out.println(score);
//                    if (score.get(0).getAsString().equalsIgnoreCase(FootballAdapter.matchId)){
//                        int homeScore = score.get(2).getAsJsonArray().get(0).getAsInt();
//                        int awayScore = score.get(3).getAsJsonArray().get(0).getAsInt();
//                        int status = homeScore > awayScore ? 1 : homeScore == awayScore ? 2 : 3;
//                        DataWord matchId = new DataWord(new BigInteger(FootballAdapter.matchId.getBytes(StandardCharsets.UTF_8)).longValue());
//                        DataWord s = new DataWord(status);
//                        DataWord hs = new DataWord(homeScore);
//                        DataWord as = new DataWord(awayScore);
//                        System.out.println(matchId.toHexString() + s.toHexString() + hs.toHexString() + as.toHexString());
//                        result.put("result", matchId.toHexString() + s.toHexString() + hs.toHexString() + as.toHexString());
//                        break;
//                    }
//                }
//            } catch (Exception e) {
//                result.replace("code", 1);
//                result.replace("msg", "parse response failed, url:" + url);
//                log.info("parse response failed, url:" + url);
//            }
//        } else {
//            result.replace("code", 1);
//            result.replace("msg", "request failed, url:" + url);
//            log.error("request failed, url:" + url);
//        }

        return result;
    }
}
