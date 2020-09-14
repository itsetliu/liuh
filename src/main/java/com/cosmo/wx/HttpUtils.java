package com.cosmo.wx;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;

public class HttpUtils {


    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final int CONNECT_TIME_OUT = 5000; //链接超时时间3秒

    public static final RequestConfig REQUEST_CONFIG = RequestConfig.custom().setConnectTimeout(CONNECT_TIME_OUT).build();

    public static SSLContext wx_ssl_context = null; //微信支付ssl证书


    static{
        Resource resource = new ClassPathResource("cert/apiclient_cert.p12");//证书存放的路径
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            char[] keyPassword = "1587411501".toCharArray(); //证书密码，默认为你的商户号id
            keystore.load(resource.getInputStream(), keyPassword);
            wx_ssl_context = SSLContexts.custom().loadKeyMaterial(keystore, keyPassword).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}