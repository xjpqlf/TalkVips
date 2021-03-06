package dao.cn.com.talkvip.utils;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Created by uway on 2017/3/17.
 */
public class Rsa {


    private static final String ALGORITHM = "RSA";
 /*  private static final String RSA_PUBLICE ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDIidQqKAhVJJfWYyHOhNZKxbQM\n" +
            "E2rTNxJVqARqbwMzud1YXss4WKRHtiNzMbC2JzrrHbXp8+xkmATDPr1ZMZkrFd7y\n" +
            "yUSm3Jd8L4tfZ4eGXDRdwsMNRuB6WkgEVaW56I8gO2rDKbv4m4+/sCTP+GKHUAS8\n" +
            "SWvcUSNA9triIYANWwIDAQAB";*/
  // 沙箱rsa
   /* private static final String RSA_PUBLICE ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDnB8zx8Er9m37eBv3MrUyUVGhn\n" +
            "HUXAvEy51IAQ1dEI39VShs+h3EQgE+TAhKKjStgqSIklnU370ISvYCCwIFxQw5oy\n" +
            "1CwUQ/qdxExOmxaxaAfLbIyQCsih+35MM/OsDXQzN4DNjPnGvar4+Rz11ttVkdLL\n" +
            "XSWge8A1iuHlsOJe9wIDAQAB";*/
//正式的
   private static final String RSA_PUBLICE ="\n" +
           "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDZoOhczmYXf1qk055TAnSWiRVp\n" +
           "c4D4d1ustgx1OPjXFAliaQFqxdt2Voh/ewTOWjf94QdC0Ce971Ji5548WQtYqCkT\n" +
           "M17b2Sb//mfatNc9TeX5YRJwV4SVDyWtEwbugWOAe3IwycS5JE/xLeG99aR6aLbT\n" +
           "xU2bC/CLyPWUhbJ71QIDAQAB";
    /**
     * 得到公钥
     * @param algorithm
     * @param bysKey
     * @return
     */
    private static PublicKey getPublicKeyFromX509(String algorithm,
                                                  String bysKey) throws NoSuchAlgorithmException, Exception {
        byte[] decodedKey = Base64.decode(bysKey,Base64.DEFAULT);
        X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodedKey);

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(x509);
    }

    /**
     * 使用公钥加密
     * @param content

     * @return
     */
    public static String encryptByPublic(String content) {
        try {
            PublicKey pubkey = getPublicKeyFromX509(ALGORITHM, RSA_PUBLICE);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubkey);

            byte plaintext[] = content.getBytes("UTF-8");
            byte[] output = cipher.doFinal(plaintext);

            String s = new String(Base64.encode(output,Base64.DEFAULT));

            return s;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 使用公钥解密
     * @param content 密文

     * @return 解密后的字符串
     */
    public static String decryptByPublic(String content) {
        try {
            PublicKey pubkey = getPublicKeyFromX509(ALGORITHM, RSA_PUBLICE);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, pubkey);
            InputStream ins = new ByteArrayInputStream(Base64.decode(content,Base64.DEFAULT));
            ByteArrayOutputStream writer = new ByteArrayOutputStream();
            byte[] buf = new byte[128];
            int bufl;
            while ((bufl = ins.read(buf)) != -1) {
                byte[] block = null;
                if (buf.length == bufl) {
                    block = buf;
                } else {
                    block = new byte[bufl];
                    for (int i = 0; i < bufl; i++) {
                        block[i] = buf[i];
                    }
                }
                writer.write(cipher.doFinal(block));
            }
            return new String(writer.toByteArray(), "utf-8");
        } catch (Exception e) {
            return null;
        }
    }

}