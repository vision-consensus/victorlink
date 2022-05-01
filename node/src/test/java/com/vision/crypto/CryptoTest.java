package com.vision.crypto;

import com.google.common.collect.Maps;
import com.vision.OracleApplication;
import com.vision.client.FulfillRequest;
import com.vision.client.OracleClient;
import com.vision.common.Constant;
import com.vision.job.JobCache;
import com.vision.job.JobSubscriber;
import com.vision.keystore.KeyStore;
import com.vision.web.entity.VisionTx;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.vision.common.crypto.ECKey;
import org.vision.common.parameter.CommonParameter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

import com.vision.crypto.Proof;
import com.vision.crypto.SolidityProof;
import com.vision.crypto.VRF;
import com.vision.crypto.VRFException;
import org.vision.common.utils.ByteArray;

public class CryptoTest {
  public static final BigInteger groupOrder = ECKey.CURVE_SPEC.getN();

  private BigInteger getFieldRandomBigInteger() {
    SecureRandom random = new SecureRandom();
    int numBits = groupOrder.bitLength();
    BigInteger a;
    do {
      //Constructs a randomly generated BigInteger, uniformly distributed over the range 0 to (2^numBits - 1),
      a = new BigInteger(numBits, random);
    } while (a.compareTo(groupOrder) >= 0);
    return a;
  }

  private BigInteger getRandomBigInteger() {
    SecureRandom random = new SecureRandom();
    int numBits = groupOrder.bitLength();
    BigInteger a;
      //Constructs a randomly generated BigInteger, uniformly distributed over the range 0 to (2^numBits - 1),
      a = new BigInteger(numBits, random);
      return a;
  }

  @Test
  public void vRFTest() throws IOException {
    for (int i = 1; i < 100; i++) {
      System.out.println();
      System.out.println("i = : "+ i);
      //secretKey must be less than secp256k1 group order
      String prikey = getFieldRandomBigInteger().toString(16);
      String seed = getRandomBigInteger().toString(16);
      //System.out.println("prikey = " + prikey);
      VRF vrf = new VRF(prikey);

      Proof proof = vrf.generateProof(ByteArray.fromHexString(seed));
      assert(proof != null ) : "fail to generate Proof";
      //System.out.println(proof.toString());

      //2
      SolidityProof solidityProof = vrf.solidityPrecalculations(proof);
      assert(proof != null ) : "fail to generate solidityProof";
      //System.out.println(solidityProof.toString());

      //3
      byte[] marshaledProof;
      try {
        marshaledProof = vrf.marshalForSolidityVerifier(solidityProof);
      } catch (VRFException vrfException) {
        vrfException.printStackTrace();
        return;
      }
      //System.out.println("marshaledProof:" + ByteArray.toHexString(marshaledProof));
    }
  }
  }
