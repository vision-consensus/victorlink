package com.vision.web.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.vision.client.message.BroadCastResponse;
import com.vision.client.message.TriggerResponse;
import com.vision.common.AbiUtil;
import com.vision.common.util.HttpUtil;
import com.vision.keystore.KeyStore;
import com.vision.keystore.VrfKeyStore;
import com.vision.web.common.ResultStatus;
import com.vision.web.common.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.springframework.web.bind.annotation.*;
import org.vision.common.crypto.ECKey;
import org.vision.common.utils.ByteArray;
import org.vision.common.utils.JsonUtil;
import org.vision.common.utils.Sha256Hash;
import org.vision.core.capsule.TransactionCapsule;
import org.vision.protos.Protocol;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.vision.common.Constant.FULL_NODE_HOST;

@Slf4j
@RestController
@RequestMapping("/vrf")
@AllArgsConstructor
@CrossOrigin
public class VrfTestController {
  @PostMapping("/rolldice")
  public R create(@RequestBody VrfRollDiceTest vrfRollDiceTest) {
    try {
      String userSeed = vrfRollDiceTest.getUserSeed();
      SecureRandom random = new SecureRandom();
      BigInteger seed = new BigInteger(10, random);
      userSeed = seed.toString(10);
      String rollerAddr = vrfRollDiceTest.getRollerAddr();
      String contractAddr = vrfRollDiceTest.getContractAddr();
      String ROLLDICE_METHOD_SIGN =
          "rollDice(uint256,address)";
      List<Object> parameters = Lists.newArrayList();
      parameters.add(userSeed);
      parameters.add(rollerAddr);
      Map<String, Object> params = Maps.newHashMap();
      params.put("owner_address", KeyStore.getAddr());
      params.put("contract_address", contractAddr);
      params.put("function_selector", ROLLDICE_METHOD_SIGN);
      params.put("parameter", AbiUtil.parseParameters(ROLLDICE_METHOD_SIGN, parameters));
      params.put("fee_limit", 100_000_000L);
      params.put("call_value", 0);
      params.put("visible", true);
      String response = HttpUtil.post("https", FULL_NODE_HOST,
          "/wallet/triggersmartcontract", params);
      TriggerResponse triggerResponse = null;
      triggerResponse = JsonUtil.json2Obj(response, TriggerResponse.class);

      // sign
      ECKey key = KeyStore.getKey();
      String rawDataHex = triggerResponse.getTransaction().getRawDataHex();
      Protocol.Transaction.raw raw = Protocol.Transaction.raw.parseFrom(ByteArray.fromHexString(rawDataHex));
      byte[] hash = Sha256Hash.hash(true, raw.toByteArray());
      ECKey.ECDSASignature signature = key.sign(hash);
      ByteString bsSign = ByteString.copyFrom(signature.toByteArray());
      TransactionCapsule transactionCapsule = new TransactionCapsule(raw, Arrays.asList(bsSign));

      // broadcast
      params.clear();
      params.put("transaction", Hex.toHexString(transactionCapsule.getInstance().toByteArray()));
      response = HttpUtil.post("https", FULL_NODE_HOST,
          "/wallet/broadcasthex", params);
      BroadCastResponse broadCastResponse =
          JsonUtil.json2Obj(response, BroadCastResponse.class);

      return R.ok().put("data", "");
    } catch (Exception e) {
      log.error("vrf rolldice failed, error : " + e.getMessage());
      return R.error(ResultStatus.Failed);
    }
  }

  @RequestMapping(value = "/updateVRFKey/{vrfConfigFile}", method = RequestMethod.GET)
  public R getJobById(@PathVariable("vrfConfigFile") String filePath) {
    try {
      VrfKeyStore.initKeyStore(filePath);
      return R.ok().put("data", VrfKeyStore.getVrfKeyMap().keySet());
    } catch (FileNotFoundException e) {
      log.error("update init VRF ECKey failed, err: {}", e.getMessage());
      return R.error(ResultStatus.Failed);
    }
  }

}
