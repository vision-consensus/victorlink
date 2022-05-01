package com.vision.client;
import com.google.common.collect.Maps;
import com.vision.OracleApplication;
import com.vision.common.AbiUtil;
import com.vision.common.Config;
import com.vision.job.JobCache;
import com.vision.job.JobSubscriber;
import com.vision.keystore.KeyStore;
import com.vision.web.entity.VisionTx;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Strings;
import com.vision.client.OracleClient;
import com.vision.client.ReSender;
import com.vision.common.Constant;
import com.vision.job.JobCache;
import com.vision.job.JobSubscriber;
import com.vision.keystore.KeyStore;
import com.vision.keystore.VrfKeyStore;
import java.io.FileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.vision.common.parameter.CommonParameter;

import java.io.FileNotFoundException;
import java.util.Map;

import static com.vision.common.Constant.FULFIL_METHOD_SIGN;
import java.util.List;

import static com.vision.common.Constant.VisionTxInProgress;


public class ReSenderTest {

  @BeforeClass
  public static void init() throws FileNotFoundException {
    KeyStore.initKeyStore("classpath:key.store");
  }

  @Test
  public void stringMapTest() {
    Map<String, Object> params = Maps.newHashMap();
    params.put("owner_address", KeyStore.getAddr());
    params.put("contract_address", "");
    params.put("function_selector", FULFIL_METHOD_SIGN);
    params.put("parameter", "test");
    params.put("fee_limit", Config.getMinFeeLimit());
    params.put("call_value", 0);
    params.put("visible", true);

    String strFromMap = OracleClient.convertWithIteration(params);
    Map<String, Object> mapFromStr = ReSender.convertWithStream(strFromMap);
    for (Map.Entry<String, Object> param : params.entrySet())  {
      Assert.assertEquals(param.getValue().toString(), mapFromStr.get(param.getKey()));
    }
  }
}

