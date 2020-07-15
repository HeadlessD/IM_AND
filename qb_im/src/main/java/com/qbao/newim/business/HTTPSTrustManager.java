package com.qbao.newim.business;

import android.text.TextUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HTTPSTrustManager implements X509TrustManager {

	  private static TrustManager[] trustManagers;  
	  private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[] {};
	    
	    
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		
		if (false)
			return;
		
//		if (chain == null || chain.length < 2) {
//			 throw new CertificateException("非法证书,连接中止");
//		}
//
//		X509Certificate certificate = chain[0];
//		boolean checked = checkCN(certificate.getSubjectDN().getName());
//		if (checked == false) {
//			throw new CertificateException("非法证书,连接中止");
//		}
//
//		checked = checkQBPublicKey(Utils.bytesToHexString(certificate.getPublicKey().getEncoded()));
//		if (checked == false) {
//			throw new CertificateException("非法证书,连接中止");
//		}
//
//	   Certificate last = chain[chain.length - 1];
//	   checked = checkPublicKey(Utils.bytesToHexString(last.getPublicKey().getEncoded()));
//		if (checked == false) {
//			throw new CertificateException("非法证书,连接中止");
//		}
//
//		try {
//			certificate.verify(chain[1].getPublicKey());
//		} catch (Exception e) {
//			throw new CertificateException("非法证书,连接中止");
//		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		// TODO Auto-generated method stub
		return _AcceptedIssuers;
	}

	
    public boolean isClientTrusted(X509Certificate[] chain) {  
        return true;  
    }  
  
    public boolean isServerTrusted(X509Certificate[] chain) {  
        return true;  
    }
    
    public static void allowAllSSL() throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, KeyManagementException {
         
        SSLContext context = null;  
        if (trustManagers == null) {  
            trustManagers = new TrustManager[] { new HTTPSTrustManager() };  
        }  

        context = SSLContext.getInstance("TLS");  
        context.init(null,trustManagers, new SecureRandom());  
  
        HttpsURLConnection.setDefaultSSLSocketFactory(context  
                .getSocketFactory());  
    }  
    
    
    private static boolean checkQBPublicKey(String key)
    {
		if (TextUtils.isEmpty(key)) {
			return false;
		}  	
		
		String trustKeyString = "30820122300d06092a864886f70d01010105000382010f003082010a0282010100add" +
				"2a9df0bd051ec996efd9e5dac454bc356e3c3ab4ddf6d01342cc705b07ca244ce0d57627df" +
				"90700ee964c7752d7f1d0ee6561472b9c2c775bf30237aad0edd2b6b0b940d4e36b13347a70" +
				"da538e32f1c1039885649d10593cd2fecd2a16e8fa13433245d97ef3394496ea9125b6e3fb810779d33" +
				"29ef22792477198ef9d290d599dc1a01279501415c3e6c40b3508f0ee2262433593b85aea53851a9aea3e" +
				"ceda83fb5b179e51288db5187f496a3221c953c90d284d396a1ce7616d2d6519553fd05a6b46a4936775b4" +
				"cdf1efcb3ad8c50c3c059fade046daf95dbe1c99a74c65b57c62d45022c0afb9567127d4c7fe61b1f4e2ceb" +
				"665180bcc0d8731d9bf0203010001";
		return key.equalsIgnoreCase(trustKeyString);

	}

	private static boolean checkPublicKey(String key)
    {
		if (TextUtils.isEmpty(key)) {
			return false;
		}  	
		
		String trustKeyString = "30819f300d06092a864886f70d010101050003818d0030818902818100c15db" +
				"158670862eea09a2d1f086d911468980a1efeda046f13846221c3d17cce9f05e0b801f04e34ece" +
				"28a950464acf16b535f05b3cb6780bf42028efedd0109ece100144ffcfbf00cdd43ba5b2be11f80" +
				"709915579316f10f976ab7c268231ccc4d5930ac511e3baf2bd6ee63457bc5d95f50d2e3500f3a88e7bf14fde0c7b90203010001";
		return key.equalsIgnoreCase(trustKeyString);

	}

	private static boolean checkCN(String cn) {
		boolean bResult = false;
		if (TextUtils.isEmpty(cn)) {
			return false;
		}
		
		String[] dataString = cn.split(",");
		if (dataString == null)
			return  false;

		int size = dataString.length;
		for (int i = 0; i < size; i++) {
			String splitdata = dataString[i];
			if (TextUtils.isEmpty(splitdata)) {
				continue;
			}
			
			int k = splitdata.indexOf("=");
			if (k < 0)
				continue;
			
			String prefix = splitdata.substring(0,k);
			if (!prefix.equalsIgnoreCase("cn"))
				continue;
			
			String suffix = splitdata.substring(k+1);
			if (suffix.equalsIgnoreCase("*.qbao.com"))
				return true;
		}
		
		return bResult;
	}
}
