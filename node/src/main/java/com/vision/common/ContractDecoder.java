package com.vision.common;

import static org.vision.visionjsdk.abi.Utils.convert;

import java.util.ArrayList;
import java.util.List;
import org.vision.visionjsdk.abi.FunctionReturnDecoder;
import org.vision.visionjsdk.abi.TypeReference;
import org.vision.visionjsdk.abi.datatypes.Address;
import org.vision.visionjsdk.abi.datatypes.Bool;
import org.vision.visionjsdk.abi.datatypes.Bytes;
import org.vision.visionjsdk.abi.datatypes.Int;
import org.vision.visionjsdk.abi.datatypes.VrcToken;
import org.vision.visionjsdk.abi.datatypes.Type;
import org.vision.visionjsdk.abi.datatypes.Uint;
import org.vision.visionjsdk.abi.datatypes.Utf8String;

public class ContractDecoder {

  public static List<Type> decode(String types, String rawInput) {

    List<TypeReference<?>> typeList = getTypeList(types);
    return FunctionReturnDecoder.decode(rawInput, convert(typeList));
  }

  private static List<TypeReference<?>> getTypeList(String types) {
    List<TypeReference<?>> typeList = new ArrayList<>();

    for(String type : types.split(",")) {
      switch (type) {
        case "bool":
          typeList.add(new TypeReference<Bool>() {});
          break;
        case "uint256":
        case "uint128":
        case "uint64":
        case "uint32":
        case "uint8":
          typeList.add(new TypeReference<Uint>() {});
          break;
        case "int256":
        case "int128":
        case "int64":
        case "int32":
        case "int8":
          typeList.add(new TypeReference<Int>() {});
          break;
        case "address":
          typeList.add(new TypeReference<Address>() {});
          break;
        case "string":
          typeList.add(new TypeReference<Utf8String>() {});
          break;
        case "trcToken":
          typeList.add(new TypeReference<VrcToken>() {});
          break;
        case "bytes":
          typeList.add(new TypeReference<Bytes>() {});
          break;
        default:

      }
    }

    return typeList;
  }
}
