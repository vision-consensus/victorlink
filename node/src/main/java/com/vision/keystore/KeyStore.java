package com.vision.keystore;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.vision.common.crypto.ECKey;
import org.vision.common.utils.ByteArray;
import org.vision.common.utils.PropUtil;
import org.vision.common.utils.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Store the private and address info of the node.
 */
@Slf4j
@Component
public class KeyStore {

  private static ECKey ecKey;
  @Getter
  private static String privateKey;

  public static void initKeyStore(String filePath) throws FileNotFoundException {
    if (Strings.isEmpty(filePath)) {
      filePath = "classpath:key.store";
    }
    if (!filePath.startsWith("classpath")) {
      log.info("init ECKey from {}", filePath);
      privateKey = PropUtil.readProperty(filePath, "privatekey");
    } else {
      log.info("init ECKey from classpath");
      File file = ResourceUtils.getFile(filePath);
      privateKey = PropUtil.readProperty(file.getPath(), "privatekey");
    }
    ecKey = ECKey.fromPrivate(ByteArray.fromHexString(privateKey));
  }

  public static ECKey getKey() {
    return ecKey;
  }

  public static String getAddr() {
    return StringUtil.encode58Check(ecKey.getAddress());
  }
}
