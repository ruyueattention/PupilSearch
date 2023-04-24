package com.ruyue.domain;

import java.net.MalformedURLException;
import java.net.URL;

//url对象
public class TrackURL {


    public Boolean postMethod = false;
    public URL url;
    public TrackURL(String urlString) throws MalformedURLException {
        url = new URL(urlString);
    }

    public URL getUrl() {
        return url;
    }

    //处理URL，去掉参数部分
    public String getCompileUrl(){
        if((url.getPort()==-1)?true:false){
            return  url.getProtocol()+"://"+url.getHost()+url.getPath();
        }
        else{
            return  url.getProtocol()+"://"+url.getHost()+":"+url.getPort()+url.getPath();
        }
    }



}
