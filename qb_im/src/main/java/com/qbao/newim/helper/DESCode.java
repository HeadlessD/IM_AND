package com.qbao.newim.helper;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DESCode {
	 public static final String ALGORITHM = "DES";  
	  
	    /** 
	     * 转换密钥<br> 
	     *  
	     * @param key 
	     * @return 
	     * @throws Exception 
	     */  
	    private static Key toKey(byte[] key) throws Exception {  
	        DESKeySpec dks = new DESKeySpec(key);  
	        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);  
	        SecretKey secretKey = keyFactory.generateSecret(dks);  
	  
	        // 当使用其他对称加密算法时，如AES、Blowfish等算法时，用下述代码替换上述三行代码  
	        // SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);  
	  
	        return secretKey;  
	    }  
	  
	    /** 
	     * 解密 
	     *  
	     * @param data 
	     * @param key 
	     * @return 
	     * @throws Exception 
	     */  
	    public static byte[] decrypt(byte[] data, String key) throws Exception {  
	        Key k = toKey(Base64.decode(key));  
	  
	        Cipher cipher = Cipher.getInstance(ALGORITHM);  
	        cipher.init(Cipher.DECRYPT_MODE, k);  
	  
	        return cipher.doFinal(data);  
	    }  
	  
	    /** 
	     * 加密 
	     *  
	     * @param data 
	     * @param key 
	     * @return 
	     * @throws Exception 
	     */  
	    public static byte[] encrypt(byte[] data, String key) throws Exception {
	        Key k = toKey(Base64.decode(key));
	        Cipher cipher = Cipher.getInstance(ALGORITHM);  
	        cipher.init(Cipher.ENCRYPT_MODE, k);  
	  
	        return cipher.doFinal(data);  
	    }  

	/**
	 * 加密
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String content, String key) throws Exception {
		byte[] inputData = encrypt(content.getBytes(), key);
		String content_encrypt = new String(Base64.encodeBytes(inputData));
		return content_encrypt;
	}
	    
	    public static void main(String[] args) throws Exception {
	    	 String inputStr = "qianbao2432";  
	         String key = "8raoJQeu09k=";  
	         //System.out.println("原文:\t" + inputStr);  
	   
	         //System.out.println("密钥:\t" + key);  
	   
	         byte[] inputData = inputStr.getBytes();  
	         inputData = DESCode.encrypt(inputData, key);  
	   
	         //System.out.println("加密后:\t" + new String(Base64.encodeBytes(inputData)));  
	   
	         byte[] outputData = DESCode.decrypt(inputData, key);  
	         String outputStr = new String(outputData);  
	   
	         //System.out.println("解密后:\t" + outputStr);  

		}
	  
}
