package com.vision.client;
import com.google.common.collect.Maps;
import com.vision.OracleApplication;
import com.vision.common.AbiUtil;
import com.vision.common.Config;
import com.vision.job.JobCache;
import com.vision.job.JobSubscriber;
import com.vision.keystore.KeyStore;
import com.vision.web.entity.VisionTx;
import com.vision.web.service.VisionTxService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import com.vision.common.Constant;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileNotFoundException;
import java.util.Map;

import static com.vision.common.Constant.FULFIL_METHOD_SIGN;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=OracleApplication.class)
public class ReSenderTest {

  @BeforeClass
  public static void init() throws FileNotFoundException {
//    KeyStore.initKeyStore("classpath:key.store");
    KeyStore.initKeyStore("/home/hh/workspace/projects/vision/config/key.store");
    Constant.initEnv("vtest");
  }

  @Autowired
  private VisionTxService visionTxService;

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

  @Test
  public void testResend(){
    VisionTx tx = visionTxService.getById(68L);
    System.out.println(tx.getData());
    VisionTx resendTx = ReSender.resendUnconfirmed(tx);
    System.out.println(resendTx.getData());
  }
}

