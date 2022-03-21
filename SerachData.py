# 数据处理，寻找敏感数据
from bs4 import BeautifulSoup
import re
from urllib import parse
from GlobalVariable import globalVariables as gbv
import threading
import os
import time

class DataHandler:

    def __init__(self, url, response):

        # 判断是否需要进行渲染
        # response.html.render(5)

        self.soup = BeautifulSoup(response.html.html, "lxml")
        # 获取url路径,①与search_js的结果拼接成完整url用于回溯②与search_url结果做比较，将当前域名的url保留做回溯。
        pattern = re.compile('/')
        self.baseurl = url
        self.url = re.split(pattern, url[::-1], 1)[1][::-1] + "/"
        self.response = response.html.html

    # 寻找数据函数
    def search_data(self, pattern,flag=0):
        tmplist = []
        # 遍历全部标签的string，同时去除了空行。
        item = self.response
        result = re.finditer(pattern, item)
        # 有的模块需要迭代器,有的不需要，根据这个flag来决定返回数据类型
        if flag:
            return result
        for match in result:
            tmplist.append(match.group())
        # 去重返回
        return set(tmplist)

    # 检查当前url是否可能为webpack的url，尝试获取map
    def webpack_map(self, url):
        # 疑是webpack打包的js，尝试获取map
        pattern = re.compile("sourceMappingURL=.+.map$")
        map = self.search_data(pattern)
        if map:
            sourceMappingURL = (re.compile("=").split(list(map)[0])[1])
            gbv.track[gbv.trackdeep].append(self.url + sourceMappingURL)
        elif url not in gbv.trackhistory:
            webpackjs = ["main", "vend", "app", "chunk"]
            for i in webpackjs:
                if i in url:
                    # map的地址可能是src.35edfdbe.map或者src.35edfdbe.js.map
                    gbv.track[gbv.trackdeep].append(url + ".map")

    # html自然存在script标签，而js文件不存在，所以只能提取到html中的js
    # 其他文件的js会由searchurl检测出来
    def search_js(self):
        scheme = parse.urlparse(self.url).scheme
        host = parse.urlparse(self.url).netloc
        if(host==""):
            host= parse.urlparse(self.baseurl).netloc


        # 设置find_all的过滤器，筛选js标签但是没有innerhtml的script脚本（没有innerhtml代表着是远程scritp）
        def is_script_but_no_innerhtml(tag):
            return tag.string is None and tag.name == "script" and tag.has_attr("src")

        self.webpack_map(self.baseurl)

        # 获取需要再次访问的js，list形式
        # 其中如果是http形式的大概率是外链，不予追踪回溯
        '''
        js分以下三种，需要针对性的进行填充
        ../../js/xxx.js       填充协议+域名+路径              ^(\.\.|\.)
        //aaaaa.com/js/xxx.js 填充协议              ^(//)
        /js/xxx.js    填充协议+域名                      ^(/)(?!/)
        js/xxx.js      填充协议+域名+路径+/       放到最后的elif 里面  
        '''
        pattern1 = re.compile('^(\.\.|\.)')
        pattern2 = re.compile('^(//)')
        pattern3 = re.compile('^(/)(?!/)')
        for script_item in self.soup.find_all(is_script_but_no_innerhtml):
            if re.search(pattern1, script_item.attrs["src"]):
                url = self.url + script_item.attrs["src"]
            elif re.search(pattern2, script_item.attrs["src"]):
                url = scheme + ":" + script_item.attrs["src"]
            elif re.search(pattern3, script_item.attrs["src"]):
                url = scheme + "://" + host + script_item.attrs["src"]
            elif "http" not in script_item.attrs["src"]:
                url = self.url + script_item.attrs["src"]
            # 非上述情况，那就基本上就只剩下带http的外联或者子域名存放js的情况了，这种情况下我们提取主域名或者根据具体情况具体处理（用户输入）
            # 或者我们不管了，全都查
            else:
                url = script_item.attrs["src"]
            gbv.track[gbv.trackdeep].append(url)

    # 寻找接口，主要是寻找js里面的，html里面不太准确，并且基本也没接口
    def search_api(self):
        blacklist = [".png", ".gif", ".css", ".jpg", ".video", ".mp4",".ico",".ttf",".svg",".woff2",".woff",".m4s",".js"]
        pattern = re.compile("[\'\"][/]+[-A-Za-z0-9+&@#/%?={}~_|!:.;]{,100}")
        apilist = self.search_data(pattern)
        # 将结果去除引号保存到全局api列表中
        for i in apilist:
            if i[1:]== "//" or i[1:]== "/#":
                continue
            _, file_suffix = os.path.splitext(parse.urlparse(i).path)
            if file_suffix in blacklist:
                continue
            gbv.apiresult.append(i[1:])
            if(gbv.fuzz):
                scheme = parse.urlparse(self.url).scheme
                host = parse.urlparse(self.url).netloc
                url=scheme+"://"+host+i[1:]
                gbv.track[gbv.trackdeep].append(url)

    # 寻找url（域名或者ip的都能匹配)
    # 将当前域名的url用于回溯，其他的url输出
    def search_url(self):
        # 提取url中的域名，后续用于判断url结果是否该用于回溯
        host = parse.urlparse(self.url).netloc
        blacklist = [".png", ".gif", ".css", ".jpg", ".video", ".mp4",".ico",".ttf",".svg",".woff2",".woff",".m4s",".svg&quot"]
        pattern = re.compile(r"(https?|ftp|file|ssh|smb|mysql)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]")
        urllist = self.search_data(pattern)
        # 去除一些外部站点的引用
        pattern2 = re.compile(
            "(lodash.com|feross.org|gov.cn|github.com|github.io|vuejs.org|w3.org|mozilla.org|nodejs.org|google.com|chromium.org|w3help.org|whatwg.org|stackoverflow.com|ecma-international.org|underscorejs.org|flow.org|wikipedia.org|baidu.com)")
        for i in urllist:
            # 在域名黑名单list中的不要
            if re.search(pattern2, i):
                continue
            # 去除图片、css等前端资源
            _, file_suffix = os.path.splitext(parse.urlparse(i).path)
            if file_suffix in blacklist:
                continue
            # 将当前域名的url保存到track列表中用于回溯
            # 开启全局搜索的话，则全部都加入回溯队列，否则只加入当前域名的
            gbv.urlresult.append(i)
            if gbv.searchany:
                gbv.track[gbv.trackdeep].append(i)
            elif host in i:
                gbv.track[gbv.trackdeep].append(i)


    # https://bacde.me/post/Extract-API-Keys-From-Regex/
    # https://www.cnblogs.com/timelesszhuang/p/5014595.html
    # accesskey、appid、password、proxy token、
    def search_other(self):
        # 使用正则捕获组来获取数据归属
        Jwt = "(?P<Jwt>(ey[A-Za-z0-9_-]{10,}\.[A-Za-z0-9._-]{10,}\.[A-Za-z0-9._-]{10,}|ey[A-Za-z0-9_\/+-]{10,}\.[A-Za-z0-9._\/+-]{10,}\.[A-Za-z0-9._-]{10,}))"
        Ip = "(?P<Ip>[\"\'\s/(]((2(5[0-5]|[0-4]\d))|[0-1]?\d{1,2})(\.((2(5[0-5]|[0-4]\d))|[0-1]?\d{1,2})){3})[\"\'\s/:\)]"
        Email = "(?P<Email>([a-zA-Z0-9][_|\.])*[a-zA-Z0-9]+@([a-zA-Z0-9][-|_|\.])*[a-zA-Z0-9]+\.((?!js|css|jpg|jpeg|png|ico|webp)[a-zA-Z]{2,}))"
        ChinaIdCard = "(?P<ChinaIdCard>[1-8][1-7]\d{4}(?:19|20)\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\d|3[01])\d{3}[\dX])"
        ChinaMobile = "(?P<ChinaMobile>[^\w]((?:(?:\+|00)86)?1(?:(?:3[\d])|(?:4[5-79])|(?:5[0-35-9])|(?:6[5-7])|(?:7[0-8])|(?:8[\d])|(?:9[189]))\d{8})[^\w])"
        AccessKey = "(?P<AccessKey>([Aa](ccess|CCESS)_?[Kk](ey|EY)|[Aa](ccess|CCESS)_?[sS](ecret|ECRET)|[Aa](ccess|CCESS)_?(id|ID|Id)|[Aa](ccess|CCESS)[A-Za-z0-9_-]{5,10})[A-Za-z0-9\"'\s]{0,10}[=:][\s\"\']{1,5}[A-Za-z0-9]{2,30}[\"'])"
        SecretKey = "(?P<SecretKey>([Ss](ecret|ECRET)_?[Kk](ey|EY)|[Ss](ecret|ECRET)_?(id|ID|Id)|[Ss](ecret|ECRET))[^)(|]{0,10}[=:][A-Za-z0-9\"'\s]{2,30}[\"'])"
        AppId = "(?P<AppId>([Aa](pp|PP)_?[Ii][dD]|[Aa](pp|PP)_?[Kk](ey|EY)|[Aa](pp|PP)_?[Ss](ecret|ECRET))[^)(|]{0,10}[=:][\s\"\']{1,5}[^):'\"+(|]{5,30}[\"'])"
        UserName = "(?P<UserName>([Uu](ser|SER)_?[Nn](ame|AME))[^)(|,\"'\+]{0,10}[=:][\s\"\']{1,5}[^)(|\s=,]{2,30}[\"'])"
        PassWord = "(?P<PassWord>([Pp](ass|ASS)_?[Ww](ord|ORD|D|d))[^)(|,\"'\+]{0,10}[=:][\s\"\']{1,5}[^)(|\s=]{2,30}[\"'])"

        # 未测试。
        SSHKey = "(?P<SSHKey>-----BEGIN PRIVATE KEY-----[a-zA-Z0-9\\S]{100,}-----END PRIVATE KEY——)"
        RSAKey = "(?P<RSAKey>-----BEGIN RSA PRIVATE KEY-----[a-zA-Z0-9\\S]{100,}-----END RSA PRIVATE KEY-----)"
        GithubAccessKey = "(?P<GithubAccessKey>[a-zA-Z0-9_-]*:[a-zA-Z0-9_\\-]+@github\\.com*)"

        pattern = re.compile("{Jwt}|{Ip}|{Email}|{ChinaIdCard}|{ChinaMobile}|{AccessKey}|{SecretKey}|{AppId}|{UserName}|{PassWord}|{SSHKey}|{RSAKey}|{GithubAccessKey}".format(Jwt=Jwt, Ip=Ip,Email = Email,ChinaIdCard=ChinaIdCard,ChinaMobile=ChinaMobile  , AccessKey=AccessKey, SecretKey=SecretKey, AppId=AppId, UserName=UserName,PassWord=PassWord ,SSHKey=SSHKey ,RSAKey=RSAKey , GithubAccessKey=GithubAccessKey))
        result = self.search_data(pattern,1)
        for i in result:
            if i.group("Jwt"):
                gbv.Sensitiveinformation["Jwt"].append(i.group("Jwt"))
            elif i.group("Ip"):
                gbv.Sensitiveinformation["Ip"].append(i.group("Ip")[1:])
            elif i.group("Email"):
                gbv.Sensitiveinformation["Email"].append(i.group("Email"))
            elif i.group("ChinaIdCard"):
                gbv.Sensitiveinformation["ChinaIdCard"].append(i.group("ChinaIdCard"))
            elif i.group("AccessKey"):
                gbv.Sensitiveinformation["AccessKey"].append(i.group("AccessKey"))
            elif i.group("SecretKey"):
                gbv.Sensitiveinformation["SecretKey"].append(i.group("SecretKey"))
            elif i.group("AppId"):
                gbv.Sensitiveinformation["AppId"].append(i.group("AppId"))
            elif i.group("UserName"):
                gbv.Sensitiveinformation["UserName"].append(i.group("UserName"))
            elif i.group("PassWord"):
                gbv.Sensitiveinformation["PassWord"].append(i.group("PassWord"))
            elif i.group("SSHKey"):
                gbv.Sensitiveinformation["SSHKey"].append(i.group("SSHKey"))
            elif i.group("RSAKey"):
                gbv.Sensitiveinformation["RSAKey"].append(i.group("RSAKey"))
            elif i.group("GithubAccessKey"):
                gbv.Sensitiveinformation["GithubAccessKey"].append(i.group("GithubAccessKey"))
            else:
                pass
                #print("no any think")


    def main(self):
        start = time.time()
        t = threading.Thread(target=self.search_url)
        t.start()
        t1 = threading.Thread(target=self.search_js)
        t1.start()
        t2 = threading.Thread(target=self.search_api)
        t2.start()
        t.join()
        t1.join()
        t2.join()
        t3 = threading.Thread(target=self.search_other)
        t3.start()
        t3.join()
        return len(self.response)
