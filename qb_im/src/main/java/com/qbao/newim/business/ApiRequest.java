package com.qbao.newim.business;

import android.content.Context;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.qbao.newim.configure.Constants;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.model.NIM_UploadInfo;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.DeviceUuid;
import com.qbao.newim.util.NetUtils;
import com.qbao.newim.util.Utils;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

/**
 * Created by chenjian on 2017/4/18.
 */

public class ApiRequest {

    public static ApiQbao getApiQbao() {
        return retrofit().create(ApiQbao.class);
    }

    public interface ApiQbao {

        @GET
        Call<ResponseBody> getRandomCode(@Url String url);

        @POST
        @FormUrlEncoded
        Call<ResponseBody> getToken(@Url String url, @FieldMap Map<String, String> maps);

        @POST
        @FormUrlEncoded
        Call<ResponseBody> getST(@Url String url, @FieldMap Map<String, String> maps);

        @POST
        @FormUrlEncoded
        Call<ResponseBody> login(@Url String url, @FieldMap Map<String, String> maps);

        @POST
        Call<ResponseBody> sendPostRequest(@Url String url);

        @POST
        @FormUrlEncoded
        Call<ResponseBody> getUserInfo(@Url String url, @FieldMap Map<String, String> maps);

        @GET
        Call<ResponseBody> sendGetRequest(@Url String url);

        // 用于上传文件到IM服务器，这里地址是拼接可变的，所以当做参数来传
        @POST
        @Multipart
        Call<NIM_UploadInfo> uploadFile(@Url String url, @Part MultipartBody.Part file);

        @GET
        Call<ResponseBody> downloadFile(@Url String url);
    }

    static Retrofit mRetrofit;

    public static Retrofit retrofit() {
        if (mRetrofit == null) {

            // 持久化cookie管理
            ClearableCookieJar cookieJar =
                    new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(AppUtil.GetContext()));

            OkHttpClient.Builder builder = new OkHttpClient.Builder().cookieJar(cookieJar);
            setCache(builder);
//			setParam(builder);
//			setCookies(builder);
            setConnect(builder);
            setHeader(builder);
            setSSLSocket(builder);
            OkHttpClient okHttpClient = builder.build();
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.QB_SERVICE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return mRetrofit;
    }

    public static void setSSLSocket(OkHttpClient.Builder builder) {
        TrustManagerFactory trustManagerFactory;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore tks = KeyStore.getInstance(KeyStore.getDefaultType());
            tks.load(null, null);
            trustManagerFactory.init(tks);
            TrustManager[] trustManagers = new TrustManager[] { new HTTPSTrustManager() };
//            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
//            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
//                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
//            }
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(sslSocketFactory);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setCache(OkHttpClient.Builder builder) {
        File cacheFile = new File(AppUtil.GetContext().getExternalCacheDir(), "qianbao");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 50);
        Interceptor cacheInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                if (!NetUtils.NetAvailable(AppUtil.GetContext())) {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }
                Response response = chain.proceed(request);
                if (NetUtils.NetAvailable(AppUtil.GetContext())) {
                    int maxAge = 0;
                    // 有网络时 设置缓存超时时间0个小时
                    response.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .removeHeader("device")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                            .build();
                } else {
                    // 无网络时，设置超时为4周
                    int maxStale = 60 * 60 * 24 * 28;
                    response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("nyn")
                            .build();
                }
                return response;
            }
        };
        builder.cache(cache).addInterceptor(cacheInterceptor);
    }

    // 主要设置一些公共属性参数，比如http://www.baidu.com/?param=xxx， 其中？param=xxx就是接入的参数
    public static void setParam(OkHttpClient.Builder builder) {
        Interceptor addQueryParameterInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request request;
                HttpUrl modifiedUrl = originalRequest.url().newBuilder()
                        // Provide your custom parameter here
                        .addQueryParameter("device", "")
                        .build();
                request = originalRequest.newBuilder().url(modifiedUrl).build();
                return chain.proceed(request);
            }
        };
        builder.addInterceptor(addQueryParameterInterceptor);
    }

    // 设置头信息
    public static void setHeader(OkHttpClient.Builder builder) {
        final Context context = AppUtil.GetContext();
        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder();
                Request request = builder
                        .addHeader("accept", "*/*")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("connection", "Keep-Alive")
                        .addHeader("envId", DeviceUuid.getInstance().getDeviceUuid(context))
                        .addHeader("devId", Utils.getImei(context))
                        .addHeader("version", Utils.getVersion(context))
                        .addHeader("versionCode", "" + Utils.getVersionCode(context))
                        .addHeader("channel", Utils.getChannel(context))
                        .addHeader("requestType", "json")
                        .addHeader("device", android.os.Build.MODEL)
                        .addHeader("city_code", "289")
                        .addHeader("lat", "31.207423")
                        .addHeader("lon", "121.633267")
                        .addHeader("devType", "android")
                        .addHeader("sourceType", "client")
                        .addHeader("appVersion", "qbao")
                        .addHeader("User-Agent", GlobalVariable.USER_AGENT)
                        .build();
                return chain.proceed(request);
            }
        };
        builder.addInterceptor(headerInterceptor );
    }

    public static void setConnect(OkHttpClient.Builder builder) {
        //设置超时
        builder.connectTimeout(5, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(true);
    }
}
