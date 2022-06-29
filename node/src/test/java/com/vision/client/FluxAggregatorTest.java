package com.vision.client;

import com.vision.client.message.OracleRoundState;
import com.vision.common.Constant;
import com.vision.keystore.KeyStore;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

public class FluxAggregatorTest {

  @Before
  public void init() throws FileNotFoundException {
    KeyStore.initKeyStore("classpath:key.store");
  }

  @Test
  public void getOracleRoundState() throws IOException {
    OracleRoundState ret = FluxAggregator.getOracleRoundState("VGm9cecRyrHAUziKrmRASPLb8fgZbJJmF9", 13);
    System.out.println(ret);
  }
}
