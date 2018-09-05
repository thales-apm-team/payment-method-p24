package com.payline.payment.p24.utils;

import okhttp3.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

abstract class AbstractHttpClient {

    public static final String CONTENT_TYPE = "Content-Type";
    private OkHttpClient client;

    /**
     * Default constructor
     */
    public AbstractHttpClient() throws GeneralSecurityException {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .sslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory(), getX509TrustManager())
                .build();
    }

    /**
     * From example in {@link okhttp3.OkHttpClient.Builder#sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager)}
     *
     * @return the default X509TrustManager
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    public X509TrustManager getX509TrustManager() throws NoSuchAlgorithmException, KeyStoreException {
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        final X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
        return trustManager;
    }

    /**
     * @param scheme      URI schenme [http | https]
     * @param host
     * @param path
     * @param requestBody
     * @param contentType
     * @return okhttp3 response
     * @throws IOException
     */
    public Response doPost(String scheme, String host, String path, RequestBody requestBody, String contentType) throws IOException {

        // create url
        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
                .addPathSegment(path)
                .build();

        // create request
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader(CONTENT_TYPE, contentType)
                .build();

        // do the request
        return client.newCall(request).execute();
    }


}
