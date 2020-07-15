package com.qbao.newim.helper;

import com.qbao.newim.configure.GlobalVariable;

import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.security.KeyStore;

/**
 * Created by chenjian on 2017/3/27.
 */

public class HttpClientFactory {
    private static DefaultHttpClient defaultHttpClient;
    private static DefaultHttpClient redirectHttpClient;

    public static void init() {
        defaultHttpClient = null;
        redirectHttpClient = null;
    }

    public static DefaultHttpClient getDefaultHttpClient() throws NetException {
        if (defaultHttpClient == null) {
            defaultHttpClient = generateHttpClient(false);
        }
        return defaultHttpClient;
    }

    public static DefaultHttpClient getRedirectHttpClient() throws NetException {
        if (redirectHttpClient == null) {
            redirectHttpClient = generateHttpClient(true);
        }
        return redirectHttpClient;
    }

    public static DefaultHttpClient generateHttpClient(boolean redirect) throws NetException {
        if (true) {
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);

                SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", sf, 443));

                HttpParams params = generateHttpParams(redirect);
                //ClientConnectionManager cm = new SingleClientConnManager(params, registry);
                // add by xiaojie.huang 2013.4.10
                // 使用线程安全的connManager. 支持app中使用同一个client
                ClientConnectionManager cm = new ThreadSafeClientConnManager(params, registry);
                return new DefaultHttpClient(cm, params);
            } catch (Exception e) {
                throw new NetException();
            }
        } else {
            return new DefaultHttpClient(generateHttpParams(redirect));
        }
    }

    private static HttpParams generateHttpParams(boolean redirect) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
        //HttpConnectionParams.setSoTimeout(params, 30 * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpClientParams.setRedirecting(params, redirect);
        HttpProtocolParams.setUserAgent(params, GlobalVariable.USER_AGENT);
        return params;
    }

    public static HttpParams generateHttpParams() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setUserAgent(params, GlobalVariable.USER_AGENT);
        return params;
    }
}
