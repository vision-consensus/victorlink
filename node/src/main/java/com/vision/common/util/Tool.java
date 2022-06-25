package com.vision.common.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.vision.client.message.BroadCastResponse;
import com.vision.client.message.Transaction;
import com.vision.client.message.TriggerResponse;
import org.spongycastle.util.encoders.Hex;
import org.vision.common.crypto.ECKey;
import org.vision.common.utils.*;
import org.vision.protos.Protocol;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class Tool {

  public static String convertHexToVisionAddr(String addr) {
    if (addr.startsWith("0x")) {
      addr = addr.replaceFirst("0x", "46");
    }
    return StringUtil.encode58Check(ByteArray.fromHexString(addr));
  }

  public static String base582HexAddress(String base58Address) {
    return ByteArray.toHexString(decodeFromBase58Check(base58Address));
  }

  public static byte[] decodeFromBase58Check(String addressBase58) {
    if (addressBase58 == null || addressBase58.length() == 0) {
      System.out.println("Warning: Address is empty !!");
      return null;
    }
    byte[] address = decode58Check(addressBase58);
    if (!addressValid(address)) {
      return null;
    }
    return address;
  }

  private static final int ADDRESS_SIZE = 21;
  private static final byte ADD_PRE_FIX_BYTE = (byte) 0x46;

  public static boolean addressValid(byte[] address) {
    if (address == null || address.length == 0) {
      System.out.println("Warning: Address is empty !!");
      return false;
    }
    if (address.length != ADDRESS_SIZE) {
      System.out.println(
          "Warning: Address length need " + ADDRESS_SIZE + " but " + address.length
              + " !!");
      return false;
    }
    byte preFixbyte = address[0];
    if (preFixbyte != ADD_PRE_FIX_BYTE) {
      System.out.println("Warning: Address need prefix with " + ADD_PRE_FIX_BYTE + " but "
          + preFixbyte + " !!");
      return false;
    }
    //Other rule;
    return true;
  }

  private static byte[] decode58Check(String input) {
    byte[] decodeCheck = Base58.decode(input);
    if (decodeCheck.length <= 4) {
      return null;
    }
    byte[] decodeData = new byte[decodeCheck.length - 4];
    System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
    byte[] hash0 = Sha256Hash.hash(true,
        decodeData);
    byte[] hash1 = Sha256Hash.hash(true,
        hash0);
    if (hash1[0] == decodeCheck[decodeData.length] &&
        hash1[1] == decodeCheck[decodeData.length + 1] &&
        hash1[2] == decodeCheck[decodeData.length + 2] &&
        hash1[3] == decodeCheck[decodeData.length + 3]) {
      return decodeData;
    }
    return null;
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
