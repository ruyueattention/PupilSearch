package com.ruyue.domain;

import com.beust.jcommander.Parameter;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;



public class InitialArgs {
    @Parameter(names = {"-h","--help"}, help = true)
    public boolean help;

    @Parameter(names = {"-d","--deep"},
            help = true,
            required = false,
            description = "爬虫追溯的深度,0代表只爬输入的url,默认深度为2 -d 2")
    public int deep = 2;


    @Parameter(names = {"-t","--thread"},
            help = true,
            required = false,
            description = "线程池核心线程数,默认线程数为3 -t 3")
    public int threadNumber = 3;

    @Parameter(names = {"-timeout","--timeout"},
            help = true,
            required = false,
            description = "连接超时,默认为10 --timeout 10")
    public int timeOut = 10;

    @Parameter(names = {"-u","--url"},
            help = true,
            required = false,
            description = "需要爬取的URL,-u https://www.baidu.com")
    public String url;


    @Parameter(names = {"-f","--file"},
            help = true,
            required = false,
            description = "目标URL文件,-f D:\\F\\JavaPupil\\target.txt")
    public String targetFile;


    @Parameter(names = {"-o","--out"},
            help = true,
            required = false,
            description = "结果保存位置,-o D:\\result.xlsx")
    public String fileRoad ;



    @Parameter(names = {"-p","--proxy"},
            help = true,
            required = false,
            description = "设置代理，只支持http代理服务器(如需要挂socks，建议bp前置),-p 127.0.0.1:8080")
    public String proxy;

    @Parameter(names = {"-c","--cookie"},
            help = true,
            required = false,
            description = "自定义http头,示例为设置Cookie头 -c Cookie:uuid_tt_dd=10_9893553000-1677564610858-676702")
    public String myHttpHeader;

    @Parameter(names = {"-a","--all"},
            help = true,
            required = false,
            description = "是否对捕获到的其他域名都进行追溯, -a true")
    public String allURL = "false";

    private void setFileRoad() throws MalformedURLException {

        if(this.fileRoad==null){
            this.fileRoad = new URL(this.url).getHost()+".xlsx";
        }else if (this.targetFile!=null){
            this.fileRoad = new URL(this.url).getHost()+".xlsx";
        }

    }


    public void init() throws MalformedURLException {
        setFileRoad();
    }
}
