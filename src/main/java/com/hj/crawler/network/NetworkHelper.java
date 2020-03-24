package com.hj.crawler.network;

import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Log4j2
public class NetworkHelper {

    private OkHttpClient mOkHttpClient;

    private NetworkHelper() {
        //Proxy proxy = new Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved("127.0.0.1", 1080));

        mOkHttpClient = new OkHttpClient.Builder()
                //.proxy(proxy)
                .connectTimeout(3, TimeUnit.MINUTES)
                .readTimeout(3, TimeUnit.MINUTES)
                .writeTimeout(3, TimeUnit.MINUTES)
                .sslSocketFactory(getSSLSocketFactory(), getTrustManager())
                .hostnameVerifier(getHostnameVerifier())
                .retryOnConnectionFailure(true)
                .build();
    }

    private static SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{getTrustManager()}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            log.error(e);
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

    private static HostnameVerifier getHostnameVerifier() {
        return (s, sslSession) -> true;
    }

    private static class SingletonHolder {
        private static NetworkHelper mInstance = new NetworkHelper();
    }

    public static NetworkHelper getInstance() {
        return SingletonHolder.mInstance;
    }

    public void download(DownloadInfo downloadInfo, INetworkCallbackListener listener) {
        Request request = new Request.Builder()
                .url(downloadInfo.getUrl())
                .build();

        try {
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        saveToFile(downloadInfo.getDestDir(), downloadInfo.getDestName(), responseBody, listener);
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    log.error(e);
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                }
            });
        } catch (Exception ex) {
            log.error(ex);
            if (listener != null) {
                listener.onFailure(ex);
            }
        }
    }

    public boolean downloadExecute(DownloadInfo downloadInfo, INetworkCallbackListener listener) {
        Request request = new Request.Builder()
                .url(downloadInfo.getUrl())
                .build();

        try {
            Response response = mOkHttpClient.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                saveToFile(downloadInfo.getDestDir(), downloadInfo.getDestName(), responseBody, listener);
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            log.error(ex);
            if (listener != null) {
                listener.onFailure(ex);
            }
            return false;
        }
    }

    private void saveToFile(String destFileDir, String destFileName, ResponseBody responseBody, INetworkCallbackListener listener) {
        byte[] buffer = new byte[8192];
        int len;
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            long total = responseBody.contentLength();

            File destDir = new File(destFileDir);
            if (!destDir.exists() && !destDir.mkdirs()) {
                listener.onFailure(new RuntimeException("could not create dir: " + destDir.getAbsolutePath()));
                return;
            }

            File destFile = new File(destFileDir, destFileName);
            if (!destFile.createNewFile()) {
                listener.onFailure(new RuntimeException("could not create file: " + destFile.getAbsolutePath()));
            }

            is = responseBody.byteStream();
            fos = new FileOutputStream(destFile);
            long sum = 0;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                sum += len;
                int progress = (int) (sum * 1.0f / total * 100);
                if (listener != null) {
                    listener.onProgress(progress);
                }
            }
            fos.flush();
            if (listener != null) {
                listener.onSuccess(new File(destFileDir, destFileName).getAbsolutePath());
            }
        } catch (Exception ex) {
            log.error(ex);
            if (listener != null) {
                listener.onFailure(ex);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ignore) {

                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ignore) {

                }
            }
        }
    }
}
