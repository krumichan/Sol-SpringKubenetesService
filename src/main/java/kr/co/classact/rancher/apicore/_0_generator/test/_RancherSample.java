package kr.co.classact.rancher.apicore._0_generator.test;

import kr.co.classact.rancher.apicore.Core;
import retrofit2.Retrofit;

import java.net.MalformedURLException;
import java.net.URL;

public class _RancherSample extends Core {

    private static final String ENDPOINT = "https://10.0.0.165/v3/";
    private static final String ACCESS_KEY = "token-qsgw9";
    private static final String SECRET_KEY = "42wgk4hm7nsppntzzhq755hsm2cmdhj8ltf2lclzwb4qptchdp4c7r";

    private final Retrofit retrofit;

    private static _RancherSample me;

    private _RancherSample(Configuration configuration) {
        super(configuration);
        retrofit = super.getRetrofit();
    }

    public synchronized static _RancherSample get() throws Exception {
        if (me == null) {
            try {
                _RancherSample.me = new _RancherSample(
                        new Configuration(
                                new URL(_RancherSample.ENDPOINT)
                                , _RancherSample.ACCESS_KEY
                                , _RancherSample.SECRET_KEY
                        )
                );
            } catch (MalformedURLException e) {
                throw new Exception(
                        "Failed to open rancher url." + "\n"
                        + "url : " + _RancherSample.ENDPOINT
                        + "reason : " + e.getMessage());
            }
        }

        return _RancherSample.me;
    }

    public <T> T type(Class<T> service) {
        return retrofit.create(service);
    }
}
