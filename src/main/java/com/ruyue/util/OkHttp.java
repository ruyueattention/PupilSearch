package com.ruyue.util;

import com.ruyue.domain.InitialArgs;
import okhttp3.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class OkHttp {

    private OkHttpClient.Builder builder = new OkHttpClient.Builder();
    private OkHttpClient okHttpClient;
    public HashMap proxies;

    private Headers.Builder headerBuilder = new Headers.Builder();

    public OkHttp() {
        this.builder.sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager())
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier());
        this.okHttpClient = this.builder.build();

    }

    public OkHttp(InitialArgs initialArgs){
        if(initialArgs.proxy != null){

            String[] s = initialArgs.proxy.split(":");
            this.proxies = new HashMap();
            this.proxies.put("ip",s[0]);
            this.proxies.put("port",Integer.parseInt(s[1]));
            SocketAddress sa = new InetSocketAddress((String) this.proxies.get("ip"),
                    (Integer) this.proxies.get(
                    "port"));
            this.builder.proxy(new Proxy(Proxy.Type.HTTP,sa));
        }
        if(initialArgs.myHttpHeader !=null){
            this.headerBuilder.add(initialArgs.myHttpHeader);
        }

        this.headerBuilder.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36");
        this.headerBuilder.add("Accept", "application/json, text/plain, */*");
        this.headerBuilder.add("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");

        this.builder.connectTimeout(initialArgs.timeOut, TimeUnit.SECONDS);

        this.builder.sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager())
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier());
        this.okHttpClient = this.builder.build();


    }


    //长度转换 b to kb mb。。
    public String humConvert(double length){
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
        for(String unit: units){
            if ((length / 1024.0) < 1){
                return String.format("%.2f%s",length, unit );
            }
            length = length / 1024.0;
        }
        return length +"B";
    }

    public void systemlog(Response response){

        try{
            if(response.code()!=404){
                System.out.println("\033[32;4m"+response.code()+"  "+response.request().url()+"   "+humConvert(new Double(response.header("Content-Length")))+"\033" +
                        "[0m");
            }
            else {
                System.out.println("\033[31;4m"+response.code()+"  "+response.request().url()+"   "+humConvert(new Double(response.header("Content-Length")))+"\033[0m");

            }
        }catch (Exception e){

            if(response.code()!=404){
                System.out.println("\033[32;4m"+response.code()+"  "+response.request().url()+"   "+"\033" +
                        "[0m");
            }
            else {
                System.out.println("\033[31;4m"+response.code()+"  "+response.request().url()+"   "+"\033[0m");

            }

        }

    }

    public Response doGet(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .headers(this.headerBuilder.build())
                .build();
        Call call = this.okHttpClient.newCall(request);
        Response response = call.execute();
        systemlog(response);
        return response;
    }


    public Response doPost(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(null)
                .headers(this.headerBuilder.build())
                .build();
        Call call = this.okHttpClient.newCall(request);

        Response response = call.execute();
        systemlog(response);

        return response;

    }


    public static void main(String[] args) {
        String url = "https://www.baidu.com/";
    }

}
