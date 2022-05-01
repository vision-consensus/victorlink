package com.vision.common.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.vision.client.message.BroadCastResponse;
import com.vision.client.message.Transaction;
import com.vision.client.message.TriggerResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.spongycastle.util.encoders.Hex;
import org.vision.common.crypto.ECKey;
import org.vision.common.utils.ByteArray;
import org.vision.common.utils.JsonUtil;
import org.vision.common.utils.Sha256Hash;
import org.vision.common.utils.StringUtil;
import org.vision.protos.Protocol;

public class Tool {

  public static String convertHexToVisionAddr(String addr) {
    if (addr.startsWith("0x")) {
      addr = addr.replaceFirst("0x", "41");
    }
    return StringUtil.encode58Check(ByteArray.fromHexString(addr));
  }

  public static BroadCastResponse triggerContract(ECKey key, Map<String, Object> params, String schema, String api)
          throws IOException, URISyntaxException {
    String response = HttpUtil.post(schema, api, "/wallet/triggersmartcontract", params);
    TriggerResponse triggerResponse = JsonUtil.json2Obj(response, TriggerResponse.class);
    //
    return broadcastHex(schema, api, signTransaction(triggerResponse.getTransaction(), key));
  }

  public static BroadCastResponse triggerContract(ECKey key, Map<String, Object> params, String api)
          throws IOException, URISyntaxException {
    String response = HttpUtil.post("https", api, "/wallet/triggersmartcontract", params);
    TriggerResponse triggerResponse = JsonUtil.json2Obj(response, TriggerResponse.class);
    //
    return broadcastHex("https", api, signTransaction(triggerResponse.getTransaction(), key));
  }

  public static org.vision.protos.Protocol.Transaction signTransaction(Transaction transaction,
      ECKey key) throws InvalidProtocolBufferException {
    String rawDataHex = transaction.getRawDataHex();
    Protocol.Transaction.raw raw = Protocol.Transaction.raw
        .parseFrom(ByteArray.fromHexString(rawDataHex));
    byte[] hash = Sha256Hash.hash(true, raw.toByteArray());
    ECKey.ECDSASignature signature = key.sign(hash);
    ByteString bsSign = ByteString.copyFrom(signature.toByteArray());
    return Protocol.Transaction.newBuilder().setRawData(raw).addSignature(bsSign).build();
  }

  public static BroadCastResponse broadcastHex(String schema, String api,
      org.vision.protos.Protocol.Transaction transaction) throws IOException, URISyntaxException {
    Map<String, Object> params = new HashMap<>();
    params.put("transaction", Hex.toHexString(transaction.toByteArray()));
    String response = HttpUtil.post(schema, api, "/wallet/broadcasthex", params);
    return JsonUtil.json2Obj(response, BroadCastResponse.class);
  }

}
