package kr.co.classact.rancher.apicore;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.classact.rancher.apicore._1_core.client.BasicAuthInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.net.ssl.*;
import java.io.Serializable;
import java.net.URL;
import java.security.cert.X509Certificate;

public abstract class Core {
    private transient Retrofit retrofit;
    private final Configuration configuration;

    /**
     * Instantiates a new Rancher.
     *
     * @param configuration the configuration
     */
    public Core(final Configuration configuration) {
        this.configuration = configuration;
    }

    protected ObjectMapper configureObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    protected Retrofit getRetrofit() {

        if (this.retrofit == null) {

            try {
                // Create a trust manager that does not validate certificate chains
                final TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) { }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) { }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        }
                };

                // Install the all-trusting trust manager
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                // Create an ssl socket factory with our all-trusting manager
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String s, SSLSession sslSession) {
                                return true;
                            }
                        })
                        .addInterceptor(
                                BasicAuthInterceptor.auth(configuration.getAccessKey(), configuration.getSecretKey())
                        )
                        .addInterceptor(chain -> {
                            Request request = chain.request().newBuilder()
                                    .addHeader("Accept", "application/json")
                                    .build();
                            return chain.proceed(request);
                        });

                this.retrofit = new Retrofit.Builder()
                        .baseUrl(this.configuration.getUrl().toString())
                        .client(builder.build())
                        .addConverterFactory(JacksonConverterFactory.create(configureObjectMapper()))
                        .build();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return this.retrofit;
    }

    public static class Configuration implements Serializable {

        private URL url;
        private String accessKey;
        private String secretKey;

        /**
         * Instantiates a new Config.
         *
         * @param url       the url
         * @param accessKey the access key
         * @param secretKey the secret key
         */
        public Configuration(URL url, String accessKey, String secretKey) {
            this.url = url;
            this.accessKey = accessKey;
            this.secretKey = secretKey;
        }

        /**
         * Gets url.
         *
         * @return the url
         */
        public URL getUrl() {
            return url;
        }

        /**
         * Sets url.
         *
         * @param url the url
         */
        public void setUrl(URL url) {
            this.url = url;
        }

        /**
         * Gets access key.
         *
         * @return the access key
         */
        public String getAccessKey() {
            return accessKey;
        }

        /**
         * Sets access key.
         *
         * @param accessKey the access key
         */
        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        /**
         * Gets secret key.
         *
         * @return the secret key
         */
        public String getSecretKey() {
            return secretKey;
        }

        /**
         * Sets secret key.
         *
         * @param secretKey the secret key
         */
        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
    }
}
