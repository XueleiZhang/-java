package com.example.mac.cartoon.utils;

import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static okhttp3.internal.Internal.instance;


/**
 * date 2017/9/6
 * 依赖 implementation 'com.squareup.okhttp3:okhttp:3.9.0'
 * author:张学雷(Administrator)
 * functinn:okhttp中级封装，实现两个功能，从服务端下载数据；从客户端提交数据
 * 封装优秀的OKHttp：okhttpUtils,OKGO(更深入的封装，研究OKGO)
 * 1.节约内存，使所有的网络请求都用一个OKhttpClick和Handler对象
 * 2.解决okhttp，网络请求成功，代码在子线程的问题，把请求成功后的逻辑代码，放到主线程中执行
 * 3.简化代码
 * 这次封装用到哪些知识点？
 * 1.单例模式（内存）
 * 2.handler
 * 3.接口
 * (解决主线程的问题)
 * 4.okhttp
 * 我们在使用单例模式时，构造方法一般权限为私有，这样保证了对象的唯一性（EventBus，如果看源码的话，他的构造方法时public所以一方面可以通过单例方法拿到对象，一方面可以通过new的方式拿到）
 */


public class OKhttpManager {



    private OkHttpClient mClient;
    private static Handler mHandler;
    private volatile static OKhttpManager sManager = null;  //防止多个线程同时访问，volatile
    private static Gson gson;
//    private String clientData="{\"osVer\":\""+SystemUtil.getSystemVersion()+"\",\"phoneType\":\""+SystemUtil.getSystemModel()+"\",\"deviceId\":\"7424ef0294f7be35\",\"uuid\":\"867394036429354\",\"mac\":\"0f607264fc6318a92b9e13c65db7cd3c\",\"clientType\":1,\"appVer\":\"2.1.1\"}";


    //使用构造方法完成初始化
    private OKhttpManager() {
        mClient = new OkHttpClient();
        mHandler = new Handler();


    }

    //使用单例模式通过获取的方式拿到对象

    public static OKhttpManager getInstance() {

        if (sManager == null) {
            synchronized (OKhttpManager.class) {

                if (instance == null) {

                    sManager = new OKhttpManager();

                }

            }

        }
        return sManager;
    }

    //定义接口
    public interface Func1 {
        String onResponse(String result);

        void onErrorResponse();

    }

    public interface Func2 {
        void onResponse(byte[] result);

    }

    interface Func3 {
        void onResponse(JSONObject jsonObject);

    }


    public static void onErrorMethod(final Func1 func1) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (func1 != null) {
                    func1.onErrorResponse();
                }

            }
        });
    }

    //使用handler，接口，保证处理数据的逻辑在主线程

    //处理请求网络成功的方法，返回的结果是Json字符串
    private static void OnSuccessJsonStringMethod(final String jsonValue, final Func1 callBack) {

        //这里我用的是mHandler.post方法把数据放到主线程中，你们以后可以用EventBus或RxJava的线程调度器
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {

                    try {
                        callBack.onResponse(jsonValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

            }
        });


    }

    private static void OnSuccessJsonStringMethodImage(final byte[] jsonValue, final Func2 callBack) {

        //这里我用的是mHandler.post方法把数据放到主线程中，你们以后可以用EventBus或RxJava的线程调度器
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {

                    try {
                        callBack.onResponse(jsonValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

            }
        });


    }

    //加载网络图片
    public void downImage(String url, final Func2 callBack) {

        //监简化代码
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //判断这个response是否有对象
                if (response != null && response.isSuccessful()) {
                    OnSuccessJsonStringMethodImage(response.body().bytes(), callBack);
                }
            }
        });


    }

    //暴露提供给外界调用的方法

    /**
     * 请求指定的URL返回的结果是Json字符串
     */
    public void asyncJsonStringByURL(String url, final Func1 callBack) {

        //监简化代码
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onErrorMethod(callBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                //判断这个response是否有对象
                if (response != null && response.isSuccessful()) {
                    OnSuccessJsonStringMethod(response.body().string(), callBack);
                }
            }
        });


    }

//    public void asyncJsonStringByURLMarket(String url, final Func1 callBack) {
//
//        //监简化代码
//        Request request = new Request.Builder().url(url).header("clientData",clientData).header("site", "MAIN").header("lang", "zh_CN").header("timezone", "8").build();
//        mClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                onErrorMethod(callBack);
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//
//                //判断这个response是否有对象
//                if (response != null && response.isSuccessful()) {
//                    OnSuccessJsonStringMethod(response.body().string(), callBack);
//                }
//            }
//        });
//
//
//    }

    /**
     * 提交json
     */
    public void sendJsonPost(String url, String json, final Func1 callBack) {
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    OnSuccessJsonStringMethod(response.body().string(), callBack);
                }
            }
        });


    }


    /**
     * 提交表单
     */
    public void sendComplexForm(String url, Map<String, String> params, final Func1 callBack) {

        //表单对象
        FormBody.Builder form_builder = new FormBody.Builder();
        //键值非空判断
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                form_builder.add(entry.getKey(), entry.getValue());

            }

        }
        FormBody request_body = form_builder.build();
        final Request request = new Request.Builder().url(url).post(request_body).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response != null && response.isSuccessful()) {
                    OnSuccessJsonStringMethod(response.body().string(), callBack);
                }
            }
        });


    }


    //将JSON字符串转换成javabean
    public static <T> T parsr(String json, Class<T> tClass) {
        //判读字符串是否为空
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        if (gson == null) {
            gson = new Gson();
        }
        return gson.fromJson(json, tClass);
    }

    //将javabean转换成JSON字符串
    public static String converJavaBeanToJson(Object obj) {
        if (obj == null) {
            return "";
        }
        if (gson == null) {
            gson = new Gson();
        }
        String beanstr = gson.toJson(obj);
        if (!TextUtils.isEmpty(beanstr)) {
            return beanstr;
        }
        return "";
    }

    /**
     * 统一为请求添加头信息
     *
     * @return
     */
    private Request.Builder addHeaders() {
        Request.Builder builder = new Request.Builder()
                .addHeader("site", "MAIN");
        return builder;
    }
}
