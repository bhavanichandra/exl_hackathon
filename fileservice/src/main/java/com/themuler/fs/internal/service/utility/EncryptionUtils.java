package com.themuler.fs.internal.service.utility;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Log4j2
@Component
public class EncryptionUtils {

  @Value("${encryption.key}")
  private String encryptionKey;

  @Value("${encryption.iv}")
  private String initVector;

  private Cipher init(int cipherMode)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
          InvalidKeyException {
    IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(UTF_8));
    SecretKeySpec skeySpec = new SecretKeySpec(encryptionKey.getBytes(UTF_8), "AES");

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    cipher.init(cipherMode, skeySpec, iv);
    return cipher;
  }

  public String decrypt(String encrypted) {
    try {
      Cipher cipher = init(Cipher.DECRYPT_MODE);
      byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

      return new String(original);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return null;
  }

  public String encrypt(String value) {
    try {
      Cipher cipher = init(Cipher.ENCRYPT_MODE);
      byte[] encrypted = cipher.doFinal(value.getBytes());
      return Base64.encodeBase64String(encrypted);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
}
