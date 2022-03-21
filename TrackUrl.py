# 接收数据处理模块反馈的url，持续追踪index所得的js，交由数据提取模块
# 在追踪url的时候，只追踪同域名的，外地的就不追踪了好把。ip可以全部追踪。

from concurrent.futures import ThreadPoolExecutor, as_completed
from requests_html import HTMLSession
import SerachData
from GlobalVariable import globalVariables as gbv
from requests.packages.urllib3.exceptions import InsecureRequestWarning
import requests
from urllib import parse
from Log import logger


requests.packages.urllib3.disable_warnings()
import ExportHandler


class Track:
    def __init__(self):

        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4495.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
            'Accept-Encoding': 'gzip, deflate',
            'Accept-Language': 'zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2',
            'Upgrade-Insecure-Requests': '1'
        }
        self.proxy = gbv.proxy

        if self.proxy:
            #print(self.proxy)
            scheme = parse.urlparse(gbv.proxy).scheme
            self.proxy={scheme:self.proxy}
        self.requests = HTMLSession(verify=False)

    # requests请求方法
    def req(self, url):
        try:

            response = self.requests.get(url=url, headers=self.headers, proxies=self.proxy,timeout=5)
            # 将成功查询的url记录进history表，一方面输出做结果，另一方面防止重复查询
            gbv.trackhistory.append(url)
            content=SerachData.DataHandler(url, response).main()

            logger.info('req is {}  {}  {}'.format(url,response.status_code,content))


            return response
        except Exception as e:
            logger.warning(url + "访问失败")
            gbv.trackhistory.append(url)
            return False

    # 根据回溯规则进行回溯
    def track_search(self):
        # 遍历tarck list进行回溯，中途动态添加url，
        # gbv.track list 是这种形式[[第0层],[第一层追溯],[第二层]]

        for deepurl in gbv.track:
            # 虽然还没到最大追溯深度，但已经追溯不到新数据了
            if not deepurl:
                logger.info("定格于深度{}已无法再找到新数据".format(gbv.trackdeep - 1))
                return False
            # 判断回溯深度是否到达最大回溯深度
            if gbv.trackdeep > gbv.deep:
                return False
            # 没到就可以继续追溯。
            gbv.trackdeep = gbv.trackdeep + 1
            gbv.track.append([])
            # 第一次需要渲染，防止有些页面是由js形成的，没加载出来。这在webpack中特别常见
            # 因为这个渲染是异步操作，不好用线程，所以单独提取出来，显得有点冗余。
            if (gbv.trackdeep == 1):
                url = deepurl[0]
                response = self.requests.get(url=url, headers=self.headers, proxies=self.proxy, verify=False, timeout=5)
                #response = self.requests.get(url=url, headers=self.headers, verify=False,timeout=5)
                #渲染的疑难杂症，不行就不渲染了。
                try:
                    response.html.render(timeout=5)
                except:
                    pass
                gbv.trackhistory.append(url)
                SerachData.DataHandler(url, response).main()

            # 罗志祥模式（多线程）
            else:
                with ThreadPoolExecutor(max_workers=gbv.thread) as thread:
                    for url in deepurl:
                        # 避免重复查询
                        if url in gbv.trackhistory:
                            continue
                        thread.submit(self.req, url)

    def main(self):
        self.track_search()
        ExportHandler.Export().main()
