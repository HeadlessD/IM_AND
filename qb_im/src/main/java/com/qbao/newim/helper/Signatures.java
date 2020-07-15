package com.qbao.newim.helper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by chenjian on 2017/4/18.
 */

public class Signatures {
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final String ENCODING = "UTF-8";

    /**
     * 生成签名数据
     *
     * @param encryptText 待加密的数据
     * @param encryptKey  加密使用的key
     * @return 生成编码的字符串
     */
    public static String getSignature(String encryptText, String encryptKey)  {
        String result = null;
        try {
            byte[] data = encryptKey.getBytes(ENCODING);
            SecretKeySpec signingKey = new SecretKeySpec(data, HMAC_SHA1);
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(signingKey);
            byte[] text = encryptText.getBytes();
            result = byteArrayToHexString(mac.doFinal(text));
        } catch (Exception e) {

        }
        return result;
    }
    // 将字节转换为十六进制字符串
    private static String byteToHexString(byte ib) {
        char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
                'b', 'c', 'd', 'e', 'f' };
        char[] ob = new char[2];
        ob[0] = Digit[(ib >>> 4) & 0X0F];
        ob[1] = Digit[ib & 0X0F];
        String s = new String(ob);
        return s;
    }

    // 将字节数组转换为十六进制字符串
    private static String byteArrayToHexString(byte[] bytearray) {
        String strDigest = "";
        for (int i = 0; i < bytearray.length; i++) {
            strDigest += byteToHexString(bytearray[i]);
        }
        return strDigest;
    }
}
