package com.qbao.newim.helper;

//import sun.misc.BASE64Encoder;
//import sun.misc.BASE64Decoder;

import com.qbao.newim.util.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by IntelliJ IDEA.
 * User: zhongjiaren
 * Date: 2013-4-28
 * Time: 13:22:09
 * To change this template use File | Settings | File Templates.
 */
public class AES {

   static String e = "9238513401340235";

    // 加密
    public static String Encrypt(String src, String key) throws Exception {
        if (key == null) {
            //System.out.print("Key为空null");
            return null;
        }
        // 判断Key是否为16位
        if (key.length() != 16) {
            //System.out.print("Key长度不是16位");
            return null;
        }
        byte[] raw = key.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"0102030405060708
        IvParameterSpec iv = new IvParameterSpec(e.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        byte[] enc = cipher.doFinal(src.getBytes("utf-8"));
        return Base64.encodeBytes(enc);//此处使用BASE64做转码。
        // 此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

    // 解密
    public static String Decrypt(String src, String key) throws Exception {
        try {
            // 判断Key是否正确
            if (key == null) {
            	Logger.debug("AES","Key为空null");
                return null;
            }
            // 判断Key是否为16位
            if (key.length() != 16) {
                Logger.debug("AES","Key长度不是16位");
                return null;
            }
            byte[] raw = key.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(e.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = Base64.decode(src);//先用base64解密
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original, "utf-8");
                return originalString;
            } catch (Exception e) {
                Logger.debug("AES",e.toString());
                return null;
            }
        } catch (Exception ex) {
            Logger.debug("AES",ex.toString());
            return null;
        }
    }
}