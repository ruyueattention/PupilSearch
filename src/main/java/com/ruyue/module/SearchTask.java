package com.ruyue.module;


import com.ruyue.domain.InitialArgs;
import com.ruyue.domain.TrackURL;
import com.ruyue.domain.TreeNodeData;
import com.ruyue.util.TreeNode;
import com.ruyue.util.UniqueLinkedBlockingQueue;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchTask {
    private Document doc ;
    private URL url ;
    public TreeNode<TreeNodeData> parentNode;
    public LinkedBlockingQueue trackUrlLinkedBlockingQueue;
    public InitialArgs initialArgs ;

    public SearchTask(TrackURL url, String html, LinkedBlockingQueue trackUrlLinkedBlockingQueue,
                      TreeNode<TreeNodeData> parentNode, InitialArgs initialArgs)  {
    this.url= url.getUrl();
    this.doc = Jsoup.parse(html);
    this.parentNode = parentNode;
    this.trackUrlLinkedBlockingQueue = trackUrlLinkedBlockingQueue;
    this.initialArgs = initialArgs;
    }

    public void getWebpackMap() throws MalformedURLException, InterruptedException {


        String[] webpackjs = new String[]{"main", "vend", "app", "chunk", "runtime"};
        for (String i : webpackjs){
            if(url.getPath().substring(url.getPath().lastIndexOf("/")+1).contains(i)){

                TrackURL tmpUrl = new TrackURL(this.url.toString()+".map");

                if(!Boolean.valueOf(this.initialArgs.allURL)){
                    if(!sameDomain(tmpUrl)){
                        break;
                    }
                }
                if(!inTrackUrlQuee(tmpUrl) && !inTree(tmpUrl)){
                    this.trackUrlLinkedBlockingQueue.put(tmpUrl);
                    this.parentNode.addChild(new TreeNodeData(tmpUrl));
                }
                break;
            }
        }
    }

    //html自然存在script标签，而js文件不存在，所以只提取主页面中的js
    //其他文件的js交由getURL检测
    public void getJavaScript() throws MalformedURLException, InterruptedException {
        String willTrackUrl=null;
        //筛选script标签，遍历Elements,只保留目标js。
        for (Element element : this.doc.select("script,link")) {
            if(element.text().isEmpty()){
                //形如  http(s)://xx  直接获取。
                if (Pattern.matches("^(http|https)[\\S].*\\.js",element.attr("src"))){
                    willTrackUrl = element.attr("src");
                }
                //形如 js/../xxx.js      填充协议+域名+路径+/
                else if(Pattern.matches("^([a-zA-Z])[\\S].*\\.js",element.attr("src"))){
                    willTrackUrl = this.url.getProtocol()+"://"+ this.url.getHost()+(this.url.getPort()==-1?"":":"+url.getPort())+this.url.getPath().substring(0,this.url.getPath().lastIndexOf("/"))+"/"+element.attr("src");
                }
                //形如 //aaaaa.com/../xxx.js 填充协议
                else if (Pattern.matches("^(//)[\\S].*\\.js",element.attr("src"))){
                    willTrackUrl = this.url.getProtocol()+":"+element.attr("src");
                }
                //形如 /js/../xxx.js    填充协议+域名
                else if (Pattern.matches("^(/)[\\S].*\\.js",element.attr("src"))){
                    willTrackUrl = this.url.getProtocol()+"://"+ this.url.getHost()+(this.url.getPort()==-1?"":":"+url.getPort())+element.attr("src");
                }
                else if (Pattern.matches("^(http|https)[\\S].*\\.js",element.attr("href"))){
                    willTrackUrl = element.attr("href");
                }
                //形如 js/../xxx.js      填充协议+域名+路径+/
                else if(Pattern.matches("^([a-zA-Z])[\\S].*\\.js",element.attr("href"))){
                    willTrackUrl = this.url.getProtocol()+"://"+ this.url.getHost()+(this.url.getPort()==-1?"":":"+url.getPort())+this.url.getPath().substring(0,this.url.getPath().lastIndexOf("/"))+"/"+element.attr("href");
                }
                //形如 //aaaaa.com/../xxx.js 填充协议
                else if (Pattern.matches("^(//)[\\S].*\\.js",element.attr("href"))){
                    willTrackUrl = this.url.getProtocol()+":"+element.attr("href");
                }
                //形如 /js/../xxx.js    填充协议+域名
                else if (Pattern.matches("^(/)[\\S].*\\.js",element.attr("href"))){
                    willTrackUrl = this.url.getProtocol()+"://"+ this.url.getHost()+(this.url.getPort()==-1?"":":"+url.getPort())+element.attr("href");
                }






                if(willTrackUrl != null){
                    TrackURL tmpUrl = new TrackURL(willTrackUrl);
                    //拼接好后，判断是否回溯过这个js，否的话就入库加入回溯队列

                    if(!Boolean.valueOf(this.initialArgs.allURL)){
                        if(!sameDomain(tmpUrl)){
                            break;
                        }
                    }
                    if(!inTrackUrlQuee(tmpUrl) && !inTree(tmpUrl)){
                        this.trackUrlLinkedBlockingQueue.put(tmpUrl);
                        this.parentNode.addChild(new TreeNodeData(tmpUrl));
                    }
                }



            }
        }
    }

    //寻找url（域名或者ip的都能匹配)
    //将当前域名的url用于回溯，其他的url输出
    public void getUrl() throws MalformedURLException, InterruptedException {
        //需要操作多次，第一次匹配出全部url，第二次去掉垃圾后缀，第三次check域名。
        String pattern = "https?://[-A-Za-z0-9\\u4e00-\\u9fa5/-_\\.]{5,100}";
        String pattern2 = "^(?!.*[.](jpg|jpeg|mp4|gif|png|ico|css|video|ttf|svf|woff2|woff|m4s|)$).*$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(this.doc.toString());
        while (m.find()){
            //不以pattern2中的后缀结尾
            if(Pattern.matches(pattern2,m.group())){

                TrackURL tmpUrl =  new TrackURL(m.group());

                //功能已删:不判断url和当前host相同了，全都要，尽可能多数据.
                //if(this.url.getHost().equals(new URL(m.group()).getHost())){
                String pattern3 = "(lodash.com|feross.org|gov.cn|github.com|github.io|vuejs.org|w3.org|mozilla.org|nodejs.org|google.com|chromium.org|w3help.org|whatwg.org|stackoverflow.com|ecma-international.org|underscorejs.org|flow.org|wikipedia.org|amap.com|jsdelivr.net)";
                Pattern r2 = Pattern.compile(pattern3);
                Matcher m2 = r2.matcher(tmpUrl.getUrl().getHost());
                if(!m2.find()){
                    //System.out.println(m.group());
                    //判断是否回溯过，否，进入回溯队列
                    if(!Boolean.valueOf(this.initialArgs.allURL)){
                        if(!sameDomain(tmpUrl)){
                            break;
                        }
                    }
                    if(!inTrackUrlQuee(tmpUrl) && !inTree(tmpUrl)){
                        this.trackUrlLinkedBlockingQueue.put(tmpUrl);
                        this.parentNode.addChild(new TreeNodeData(tmpUrl));
                    }
                    //把结果封装进节点
                    this.parentNode.nodeData.urlQuee.put(m.group());

                }
            }
        }
    }


    //有很多接口可能需要post发包
    public void getApi() throws InterruptedException, MalformedURLException {
        //api类型的可能需要post发包，所以对其区分
        //  [\'\"][/]+[-A-Za-z0-9\u4e00-\u9fa5/-_]{4,100}\.(html|cgi|jsp|php|shtml|htm|asp|aspx|do)[\'\"]  匹配的是html等页面
        //  [\'\"][/]+[-A-Za-z0-9\u4e00-\u9fa5/-_]{4,100}[\'\"]   匹配的是  "无后缀的接口，常见于springboot项目"

        String patternApi = "(?<page>[\\'\\\"][/]+[-A-Za-z0-9\\u4e00-\\u9fa5/-_]{4,100}\\.(html|cgi|jsp|php|shtml|htm|asp|aspx|do)[\\'\\\"])";
        String patternPage = "(?<api>[\\'\\\"][/]+[-A-Za-z0-9\\u4e00-\\u9fa5/-_]{4,100}[\\'\\\"])";
        String pattern = patternApi+"|"+patternPage;
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(this.doc.toString());

        while(m.find()){
            //这里需要填充！！！！
            //去除前后的引号
            String tmpData = m.group().substring(0,m.group().length() - 1).substring(1);

            TrackURL tmpUrl = new TrackURL(url.getProtocol()+"://"+ url.getHost()+(url.getPort()==-1?"":":"+url.getPort())+tmpData);
            //api类型的需要测试post发包
            if(m.group("api")!=null){
                tmpUrl.postMethod = true;
            }
            this.parentNode.nodeData.apiQuee.put(tmpData);

            if(!Boolean.valueOf(this.initialArgs.allURL)){
                if(!sameDomain(tmpUrl)){
                    break;
                }
            }

            if(!inTrackUrlQuee(tmpUrl) && !inTree(tmpUrl)){
                this.trackUrlLinkedBlockingQueue.put(tmpUrl);
                this.parentNode.addChild(new TreeNodeData(tmpUrl));
            }

        }



    }


    public void getSensitiveInfo() throws InterruptedException {
        String Ip ="(?<Ip>[\\\"\\'\\s/(]((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}[\\\"\\'\\s/:\\)])" ;
        String NetProtocol = "(?<NetProtocol>(ftp|file|ssh|smb|mysql)://[-A-Za-z0-9@#/:?=~_\\.]{5,150})";
        String ChinaMobile = "(?<ChinaMobile>[^\\w]((?:(?:\\+|00)86)?1(?:(?:3[\\d])|(?:4[5-79])|(?:5[0-35-9])|(?:6[5-7])|(?:7[0-8])|(?:8[\\d])|(?:9[189]))\\d{8})[^\\w])";
        String ChinaIdCard = "(?<ChinaIdCard>[1-8][1-7]\\d{4}(?:19|20)\\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\\d|3[01])\\d{3}[\\dX])";
        String Email = "(?<Email>([a-zA-Z0-9][_|\\.])*[a-zA-Z0-9]+@([a-zA-Z0-9][-|_|\\.])*[a-zA-Z0-9]+\\.((?!js|css|jpg|jpeg|png|ico|webp)[a-zA-Z]{2,}))";
        String Jwt = "(?<Jwt>(ey[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}|ey[A-Za-z0-9_\\/+-]{10,}\\.[A-Za-z0-9_\\/+-]{10,}\\.[A-Za-z0-9_-]{10,}))";

        String AccessKey = "(?<AccessKey>([Aa](ccess|CCESS)_?[Kk](ey|EY)|[Aa](ccess|CCESS)_?[sS](ecret|ECRET)|[Aa](ccess|CCESS)_?(id|ID|Id)|[Aa](ccess|CCESS)[A-Za-z0-9_-]{5,10})[A-Za-z0-9\\\"'\\s]{0,10}[=:][\\s\\\"\\']{1,5}[A-Za-z0-9]{2,30}[\\\"'])";
        String SecretKey = "(?<SecretKey>([Ss](ecret|ECRET)_?[Kk](ey|EY)|[Ss](ecret|ECRET)_?(id|ID|Id)|[Ss](ecret|ECRET))[^)(|]{0,10}[=:][A-Za-z0-9\\\"'\\s]{2,30}[\\\"'])";
        String AppId = "(?<AppId>([Aa](pp|PP)_?[Ii][dD]|[Aa](pp|PP)_?[Kk](ey|EY)|[Aa](pp|PP)_?[Ss](ecret|ECRET))[^),(|]{0,10}[=:][\\s\\\"\\']{1,5}[^):',\\.\\\"+(|]{5,30}[\\\"'])";
        String UserName = "(?<UserName>([Uu](ser|SER)_?[Nn](ame|AME))[^)(|,\\\"'\\+]{0,10}[=:][\\s\\\"\\']{1,5}[^)(|\\s=,]{2,30}[\\\"'])";
        String PassWord = "(?<PassWord>([Pp](ass|ASS)_?[Ww](ord|ORD|D|d))[^)(|,\\\"'\\+]{0,10}[=:][\\s\\\"\\']{1,5}[^)(|\\s=]{2,30}[\\\"'])";

        //未测试。
        String SSHKey = "(?<SSHKey>-----BEGIN PRIVATE KEY-----[a-zA-Z0-9\\\\S]{100,}-----END PRIVATE KEY——)";
        String RSAKey = "(?<RSAKey>-----BEGIN RSA PRIVATE KEY-----[a-zA-Z0-9\\\\S]{100,}-----END RSA PRIVATE KEY-----)";
        String GithubAccessKey = "(?<GithubAccessKey>[a-zA-Z0-9_-]*:[a-zA-Z0-9_\\\\-]+@github\\\\.com*)";

        String pattern =  String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s", Ip,NetProtocol,ChinaMobile,ChinaIdCard,Email,Jwt,AccessKey,SecretKey,AppId,UserName,PassWord,SSHKey,RSAKey,GithubAccessKey );
        Pattern r = Pattern.compile(pattern);

        Matcher m = r.matcher(this.doc.toString());
        UniqueLinkedBlockingQueue ipQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue netProtocolQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue chinaMobileQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue chinaIdCardQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue emailQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue jwtQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue accessKeyQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue secretKeyQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue appIdQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue userNameQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue passWordQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue sshKeyQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue rsaKeyQuee = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue githubAccessKeyQuee = new UniqueLinkedBlockingQueue();


        while (m.find()){
            if(m.group("Ip")!=null){
                ipQuee.put(m.group("Ip"));
            }
            else if (m.group("NetProtocol")!=null){
                netProtocolQuee.put(m.group("NetProtocol"));
            }
            else if (m.group("ChinaMobile")!=null){
                chinaMobileQuee.put(m.group("ChinaMobile"));
            }
            else if (m.group("ChinaIdCard")!=null){
                chinaIdCardQuee.put(m.group("ChinaIdCard"));
            }
            else if (m.group("Email")!=null){
                emailQuee.put(m.group("Email"));
            }
            else if (m.group("Jwt")!=null){
                jwtQuee.put(m.group("Jwt"));
            }
            else if (m.group("AccessKey")!=null){
                accessKeyQuee.put(m.group("AccessKey"));
            }
            else if (m.group("SecretKey")!=null){
                secretKeyQuee.put(m.group("SecretKey"));
            }
            else if (m.group("AppId")!=null){
                appIdQuee.put(m.group("AppId"));
            }
            else if (m.group("UserName")!=null){
                userNameQuee.put(m.group("UserName"));
            }
            else if (m.group("PassWord")!=null){
                passWordQuee.put(m.group("PassWord"));
            }
            else if (m.group("SSHKey")!=null){
                sshKeyQuee.put(m.group("SSHKey"));
            }
            else if (m.group("RSAKey")!=null){
                rsaKeyQuee.put(m.group("RSAKey"));
            }
            else if (m.group("GithubAccessKey")!=null){
                githubAccessKeyQuee.put(m.group("GithubAccessKey"));
            }
        }
        if(ipQuee.size()+netProtocolQuee.size()+chinaMobileQuee.size()+chinaIdCardQuee.size()+emailQuee.size()+jwtQuee.size()+accessKeyQuee.size()+secretKeyQuee.size()+appIdQuee.size()+ userNameQuee.size()+ passWordQuee.size()+ sshKeyQuee.size()+ rsaKeyQuee.size()+ githubAccessKeyQuee.size()>0){
            this.parentNode.nodeData.sensitiveInfoDict.put("Ip",ipQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("NetProtocol",netProtocolQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("ChinaMobile",chinaMobileQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("ChinaIdCard",chinaIdCardQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("Email",emailQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("Jwt",jwtQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("AccessKey",accessKeyQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("SecretKey",secretKeyQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("AppId",appIdQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("UserName",userNameQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("PassWord",passWordQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("SSHKey",sshKeyQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("RSAKey",rsaKeyQuee);
            this.parentNode.nodeData.sensitiveInfoDict.put("GithubAccessKey",githubAccessKeyQuee);
        }
        else{
            this.parentNode.nodeData.sensitiveInfoDict = null;
        }
    }
    //遍历这颗树，看看数据是否在里面了
    public Boolean inTree(TrackURL compareUrl){
        TreeNode<TreeNodeData> root = this.parentNode.getRoot();
        for (TreeNode<TreeNodeData> node : root.elementsIndex) {
            if(node.nodeData.trackURL.getCompileUrl().equals(compareUrl.getCompileUrl())){
                return true;
            }
        }
        return false;
    }
    //防止当前页面出现两个相同的url，就会一并加进去
    //这会导致参数不同的情况下访问相同url，无所谓？
    public Boolean inTrackUrlQuee(TrackURL compareUrl){
        for (Object element : this.trackUrlLinkedBlockingQueue) {
            if(((TrackURL) element).getCompileUrl().equals(compareUrl.getCompileUrl())){
                return true;
            }
        }
        return false;
    }

    public Boolean sameDomain(TrackURL compareUrl){
        String pattern = "[^.]*\\.[a-z]*$";
        Pattern r = Pattern.compile(pattern);
        TreeNode<TreeNodeData> rootNode = this.parentNode.getRoot();
        Matcher m = r.matcher(rootNode.nodeData.trackURL.getUrl().getHost());
        while (m.find()) {
            if(compareUrl.getUrl().getHost().contains(m.group())){
                return true;
            }
        }
        return false;
    }

    public void runStart() throws MalformedURLException, InterruptedException {
        //做判断，只有.js文件才check webpackmap  并且.js文件不搜索里面的js标签(因为没有,webpack地图后缀是.map，所以是能正常进入到js的搜索的)
        if(Pattern.matches("\\S*(.js)$",this.url.getPath())){
            this.getWebpackMap();
        }
        else {
            this.getJavaScript();
        }

        this.getUrl();
        this.getSensitiveInfo();
        this.getApi();
        /*
        if(this.parentNode.nodeData.sensitiveInfoDict.size()!=0){
            System.out.println(this.parentNode.nodeData.sensitiveInfoDict);
        }
        */

    }
}


