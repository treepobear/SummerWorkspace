package Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS)
            .readTimeout(10000,TimeUnit.MILLISECONDS)
            .writeTimeout(10000, TimeUnit.MILLISECONDS).build();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    public static final String rootUrl="http://47.104.232.108/";

    // post请求通用
    public static void sendPostRequest(String url, RequestBody requestBody, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    //获取推荐视频列表，请求参数 账号：account 刷新次数refreshNum
    public static void getRecommendVideo(String account, int refreshNum, Callback callback){
        String url=rootUrl+"getRecommendedVideo";
        String num=String.valueOf(refreshNum);
        RequestBody body = new FormBody.Builder()
                .add("account",account)
                .add("refreshNum", num)
                .build();
        Request request  = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    //获取用户的相关视频 账号：account
    public static void getRelevantVideo(String account, Callback callback){
        String url=rootUrl+"userNew";
        RequestBody body = new FormBody.Builder()
                .add("account",account)
                .build();
        Request request  = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }


}
