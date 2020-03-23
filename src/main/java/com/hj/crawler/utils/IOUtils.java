package com.hj.crawler.utils;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class IOUtils {

    public static List<String> readLines(String path) {
        try {
            return Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void download(final String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .sslSocketFactory(getSSLSocketFactory(), getTrustManager())
                .hostnameVerifier(getHostnameVerifier())
                .retryOnConnectionFailure(true)
                .build();

        try {
            okHttpClient.newCall(request).enqueue(callback);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean saveToPath(String savePath, String fileName, byte[] bytes) {
        try {
            File dir = new File(savePath);
            if (!dir.exists() && !dir.mkdirs()) {
                return false;
            }

            File file = new File(savePath, fileName);
            if (!file.exists() && !file.createNewFile()) {
                return false;
            }

            Files.write(Paths.get(file.getAbsolutePath()), bytes);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{getTrustManager()}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static X509TrustManager getTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }
        };
    }

    public static HostnameVerifier getHostnameVerifier() {
        return (s, sslSession) -> true;
    }
}
