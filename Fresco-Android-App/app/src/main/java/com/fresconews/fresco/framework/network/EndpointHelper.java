package com.fresconews.fresco.framework.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.fresconews.fresco.BuildConfig;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by mauricewu on 11/14/16.
 */
public enum EndpointHelper {
    INSTANCE;

    private static final String SETTING_PREFERENCE = "FRESCO_SETTINGS";
    private static final String CURRENT_ENDPOINT = "CURRENT_ENDPOINT";

    private static final String BASE_URL_DEV = "https://api.dev.fresconews.com/v2/";
    private static final String BASE_URL_PROD = "https://api.fresconews.com/v2/";
    private static final String FRESCO_WEB_URL_DEV = "https://dev.fresconews.com/";
    private static final String FRESCO_WEB_URL_PROD = "https://fresconews.com/";
    //    private static final String FRESCO_CREDENTIALS_ID_DEV = "NDA0MDphbmRyb2lkdGVhbQ==";
    private static final String FRESCO_CREDENTIALS_ID_DEV = "dUczbzQ1dE1BY2tob2cwVEU3UzFCYllpQmlCREVzalhZZ0VWT2daa08yT0pnb3BSQkhxNXF6WTNGbkJwOmo4dUxqUzVNU3JOTVVhbWNzdWpmdWRRUEVQWlZ2NlJySHR1TnRSSnhmdHJQYkZkdnpHVll2TFpKa3BiOEhDWUdmVFRUajNMcmJ2WHJYZVdqUWM3V1hnWEV6NVk3ODlUaFdSUDd2S1pGSnV0TGRCZXUyQnBNUGNzZmpaY0dMSzlGcnJZblRGcEhEamNqOUFQRzd4cW1hSHA2SDRoNVRDNVZGcHg4RW1VRzVTRWdYWmRrdFRXRjRyYWVFZjlUcVU=";
    //    private static final String FRESCO_CREDENTIALS_PROD = "dUczbzQ1dE1BY2tob2cwVEU3UzFCYllpQmlCREVzalhZZ0VWT2daa08yT0pnb3BSQkhxNXF6WTNGbkJwOnR4VzJEaEZJUGhCcTBZeW1qVGNSYWU3ckZJSlFFZnU5dzBFQU9KeWhJNFpmSTFIeTlzUTVKcHZlUnBMQndpbDJuNnk0dkpWazFMVjdtNHRaM1FEYnNGTzlDRHVhQTNIdVRUUExKbVRGdnRtSUsxYlhpT01GNWVmUTJvSWpiMzRYNVdOOElzR2NHTThyeFFCVEJ4QzNJY0lpQWJiS0Fpemdvd0lOanN6MzJwTllibnd3S2pneVdxU2FKZkhza20=";
    private static final String FRESCO_CREDENTIALS_PROD = "b1oxSXhPTElicWg2UjI2em1ybkNCR1ZaZ3VqOW5hcEp2Z01xaW5WQXRheFhMUVBDaDlROEVON0daUFZjOmZXMWk4ZmlsNWFSdWxUeVJMa3MySmlPVG40Z3ZLZ0FETDhkU3J0MjRsdkNNRHprczZpRGdPdTJlRXVxS2ZqOHVyRFpteDRoNnZ2dGlXYTZOMHRCS1cwdUlrdVo4d1cwSkptUDNzelI1aHV0cXVXbXZrZTRkdkI0Z0p4cGptT3lJcEFRdlVVc0pUUXhvMzFiNmJ4ZW9WN3pOTUh0ZTlNempINkRRSkRBQ05Vd1lFUU5PNG9WaUIxREF6SU1zWTE=";
    private static final String STRIPE_KEY_DEV = "pk_test_o4pMXyj95Vqe5NgV3hb7qmdo";
    private static final String STRIPE_KEY_PROD = "pk_live_saSjliYnCbjFwYfriTzhTQiO";
    private static final String AMAZON_S3_ACCESS_KEY_DEV = "AKIAIFLQKVYXPMMMF7CQ";
    private static final String AMAZON_S3_ACCESS_KEY_PROD = "AKIAJRQQA26XTXPGVAKA";
    private static final String AMAZON_S3_SECRET_KEY_DEV = "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";
    private static final String AMAZON_S3_SECRET_KEY_PROD = "maStuGRQsr2xL0dyHjz6k127mGVRE2uMwESo7T+W";
    private static final String AMAZON_S3_BUCKET_DEV = "com.fresconews.dev";
    private static final String AMAZON_S3_BUCKET_PROD = "com.fresconews.v2.prod";
    private static final String SMOOCH_TOKEN = "bmk6otjwgrb5wyaiohse0qbr0";
    private static final String TWITTER_CONSUMER_KEY = "kT772ISFiuWQdVQblU4AmBWw3";
    private static final String TWITTER_CONSUMER_SECRET = "navenvTSRCcyUL7F4Ait3gACnxfc7YXWyaee2bAX1sWnYGe4oY";
    private static final String GOOGLE_PLUS_CREDENTIAL_ID_DEV = "1081849749650-a2hvet10rdatpphs3b1inrmnet5rqerd.apps.googleusercontent.com";
    private static final String GOOGLE_PLUS_CREDENTIAL_ID_PROD = "156075935258-fqct4j8fqnj6lccg45mtjilq3mb612rs.apps.googleusercontent.com";
    private static final String ZENDESK_UNIQUE_ID = "ca506e6c52eb2eca41150684af0269b6642facef5d23a84e";
    private static final String ZENDESK_ENDPOINT = "https://fresco.zendesk.com";
    private static final String ZENDESK_CLIENT = "mobile_sdk_client_6e930a7bb6123d229c39";

    public static Endpoint currentEndpoint;

    public void init(Context context) {
        currentEndpoint = new Endpoint(context);
    }

    public void setCurrentEndpoint(int endpoint) {
        currentEndpoint.setEndpoint(endpoint);
    }

    public void setCurrentEndpoint(String endpoint) {
        String[] endpointsArray = Fresco2.getContext().getResources().getStringArray(R.array.arrays_endpoints);
        List<String> endpointsList = Arrays.asList(endpointsArray);
        currentEndpoint.setEndpoint(endpointsList.indexOf(endpoint));
    }

    public void saveEndpoint(Context context, String endpoint) {
        SharedPreferences preferences = context.getSharedPreferences(SETTING_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CURRENT_ENDPOINT, endpoint);
        editor.apply();
        setCurrentEndpoint(endpoint);
    }

    public String getSavedEndpoint(Context context) {
        String[] endpoints = context.getResources().getStringArray(R.array.arrays_endpoints);
        SharedPreferences preferences = context.getSharedPreferences(SETTING_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getString(CURRENT_ENDPOINT, endpoints[0]);
    }

    public class Endpoint {
        public static final int DEV = 0;
        public static final int PROD = 1;

        public String endpointName;
        public String baseUrl;
        public String stripeKey;
        public String frescoWebsite;
        public String frescoClientId;
        public String twitterConsumerKey;
        public String twitterConsumerSecret;
        public String smoochToken;
        public String amazonS3AccessKey;
        public String amazonS3SecretKey;
        public String amazonS3Bucket;
        public String googleOauthServerCredentialId;
        public String zendeskUniqueId;
        public String zendeskEndpoint;
        public String zendeskClientSDK;

        public Endpoint(Context context) {
            if (BuildConfig.DEBUG) {
                setEndpoint(context, EndpointHelper.INSTANCE.getSavedEndpoint(context));
            }
            else {
                setEndpoint(PROD);
            }
        }

        public void setEndpoint(Context context, String endpoint) {
            String[] endpointsArray = context.getResources().getStringArray(R.array.arrays_endpoints);
            List<String> endpointsList = Arrays.asList(endpointsArray);
            setEndpoint(endpointsList.indexOf(endpoint));
        }

        public void setEndpoint(int endpoint) {
            switch (endpoint) {
                case DEV:
                    endpointName = "Development";
                    baseUrl = BASE_URL_DEV;
                    frescoWebsite = FRESCO_WEB_URL_DEV;
                    frescoClientId = FRESCO_CREDENTIALS_ID_DEV;
                    stripeKey = STRIPE_KEY_DEV;
                    twitterConsumerKey = TWITTER_CONSUMER_KEY;
                    twitterConsumerSecret = TWITTER_CONSUMER_SECRET;
                    smoochToken = SMOOCH_TOKEN;
                    amazonS3AccessKey = AMAZON_S3_ACCESS_KEY_DEV;
                    amazonS3SecretKey = AMAZON_S3_SECRET_KEY_DEV;
                    amazonS3Bucket = AMAZON_S3_BUCKET_DEV;
                    googleOauthServerCredentialId = GOOGLE_PLUS_CREDENTIAL_ID_DEV;
                    zendeskUniqueId = ZENDESK_UNIQUE_ID;
                    zendeskClientSDK = ZENDESK_CLIENT;
                    zendeskEndpoint = ZENDESK_ENDPOINT;
                    break;
                case PROD:
                    endpointName = "Production";
                    baseUrl = BASE_URL_PROD;
                    frescoWebsite = FRESCO_WEB_URL_PROD;
                    frescoClientId = FRESCO_CREDENTIALS_PROD;
                    stripeKey = STRIPE_KEY_PROD;
                    twitterConsumerKey = TWITTER_CONSUMER_KEY;
                    twitterConsumerSecret = TWITTER_CONSUMER_SECRET;
                    smoochToken = SMOOCH_TOKEN;
                    amazonS3AccessKey = AMAZON_S3_ACCESS_KEY_PROD;
                    amazonS3SecretKey = AMAZON_S3_SECRET_KEY_PROD;
                    amazonS3Bucket = AMAZON_S3_BUCKET_PROD;
                    googleOauthServerCredentialId = GOOGLE_PLUS_CREDENTIAL_ID_PROD;
                    zendeskUniqueId = ZENDESK_UNIQUE_ID;
                    zendeskClientSDK = ZENDESK_CLIENT;
                    zendeskEndpoint = ZENDESK_ENDPOINT;
                    break;
            }
        }
    }

}
