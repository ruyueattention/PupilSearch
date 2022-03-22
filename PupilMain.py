import TrackUrl
from GlobalVariable import globalVariables as gbv
import re
import time
import argparse
from Log import logger

#

class ParaInit():
    def __init__(self, url, thread, trackdeep, mode, fuzz):
        # 该有的/不能少，比如目标域名是aaa.com/sso/ ，不能偷懒为aaa.com/sso
        self.url = url
        gbv.thread = thread
        gbv.track = []  # 动态添加
        gbv.track.append([])
        gbv.track[0].append(url)
        gbv.apiresult = []
        gbv.urlresult = []
        gbv.trackhistory = []  # 记录已经处理过的url，防止重复查询

        gbv.Sensitiveinformation = {"Jwt": [], "Ip": [], "Email": [], "ChinaIdCard": [], "AccessKey": [],
                                    "SecretKey": [], "AppId": [], "UserName": [], "PassWord": [],
                                    "SSHKey": [], "RSAKey": [], "GithubAccessKey": []}

        # 0则代表只针对当前url搜索，不回溯其他url，1则代表对当前url得出的结果的url进行回溯，2则是继续
        gbv.deep = trackdeep  # 允许深度
        gbv.trackdeep = 0  # 允许深度
        # 是否对发现的所有URL都进行遍历。慎选，时间有点长
        gbv.searchany = mode

        # 是否将接口加入回溯队列
        gbv.fuzz = fuzz

    def url_handler(self):
        # 填充url，url不能为 www.aaa.com  需要为www.aaa.com/ ,因为后续会删除掉第最后一个/的数据，用于js的串接。如果不加就会导致host被去掉
        pattern = re.compile('/')
        if len(re.findall(pattern, self.url[:])) == 2:
            self.url = self.url + "/"

    def main(self):
        self.url_handler()
        gbv.url=self.url
        return self.url


class Pupil:

    def main(self):
        start = TrackUrl.Track()
        start.main()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='')
    parser.add_argument('-u', '--url', type=str, help='-u https://www.bilibili.com/')
    parser.add_argument('-t', '--thread', type=int, help='-t 10    thread number', default=5)
    parser.add_argument('-d', '--deep', type=int, help='-d 2    search deep', default=2)
    parser.add_argument('-a', '--all', type=bool, help='-a False    add all url to check list', default=False)
    parser.add_argument('-f', '--fuzz', type=bool, help='-f False    add api to check list', default=False)
    parser.add_argument('-p', '--proxy', type=str, help='--proxy http://127.0.0.1:8080   ', default=False)
    parser.add_argument('-timeout', '--timeout', type=int, help='--timeout 10  超时时间设置', default=5)
    args = parser.parse_args()
    gbv.timeout = args.timeout
    gbv.proxy = args.proxy
    if(gbv.proxy and "https" in gbv.proxy):
        logger.info("无效的协议 {}".format(gbv.proxy))
        exit()
    url = ParaInit(args.url, args.thread, args.deep, args.all, args.fuzz).main()

    begin = time.time()
    Pupil().main()
    times = time.time() - begin
    logger.info("总耗时{} s".format(times))

# export excel
