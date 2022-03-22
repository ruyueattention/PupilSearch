# 处理输出的，-o xml（execl）
from GlobalVariable import globalVariables as gbv
import re
import json
import os
from urllib import parse
from Log import logger
from xlwt import *


class Export:



    def save_excel(self, ldata,data):
        # 定义输出execel的一些东西
        file = Workbook(encoding='utf-8')
        table = file.add_sheet('data')  # sheet名
        work_dir = os.path.split(os.path.realpath(__file__))[0]
        filename = parse.urlparse(gbv.url).netloc
        filename = re.split(":", filename)[0]
        for i, p in enumerate(ldata):
            # 将数据写入文件,i是enumerate()函数返回的序号数
            for j, q in enumerate(p):
                table.write(i, j, q)
        for i in range(len(data)):
            for j in range(len(data[i])):
                table.write(j + 1, i, data[i][j])
        file.save(work_dir + "\\result\\" + filename + ".xls")

    def handler_sensitiveinformation(self, data):
        """
        清除字典中值为空的键值对
        去重
        """
        for k in list(data.keys()):
            if not data[k]:
                del data[k]
        for k in data.keys():
            data[k] = list(set(data[k]))

    # 去除匹配到的ip中的脏数据
    # 首先匹配内网ip，然后对外网ip进行筛选，只要有两段位数是超过10的，就认为是ip。
    def handler_ip(self):
        a_pattern = "10\.(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])\.(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])\.(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])"
        b_pattern = "172\.(1[6789]|2[0-9]|3[01])\.(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])\.(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])"
        c_pattern = "192\.168\.(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])\.(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])"
        d_pattern = "127.0.0.1"
        pattern = re.compile("{}|{}|{}|{}".format(a_pattern, b_pattern, c_pattern, d_pattern))
        removeip = []
        for ip in gbv.Sensitiveinformation["Ip"]:
            # 非内网ip，进行处理
            if not re.search(pattern, ip):
                sign = 0
                for number in re.split("\.", ip):
                    # 去除形如008.054.012.095，084.043.126.085之类的ip1
                    if ((len(number)) > 0 and int(number[0]) == 0):
                        removeip.append(ip)
                        break
                    if int(number) > 10:
                        sign = sign + 1
                if (sign < 2):
                    removeip.append(ip)
        removeip = list(set(removeip))
        for ip in removeip:
            gbv.Sensitiveinformation["Ip"].remove(ip)

    def logger_function(self,datalist):
        for i in datalist:
            logger.info(i)

    def main(self):

        self.handler_sensitiveinformation(gbv.Sensitiveinformation)
        try:
            self.handler_ip()
        except:
            pass

        gbv.apiresult=list(set(gbv.apiresult))
        gbv.urlresult = list(set(gbv.urlresult))
        logger.debug("api接口")
        self.logger_function(gbv.apiresult)
        logger.debug("存在的url")
        self.logger_function(gbv.urlresult)
        logger.debug("敏感信息")
        logger.info(gbv.Sensitiveinformation)


        data = [gbv.apiresult, gbv.urlresult]

        ldata = [["Api接口", "URL"]]
        for key in gbv.Sensitiveinformation:
            ldata[0].append(key)
            data.append(gbv.Sensitiveinformation[key])

        self.save_excel(ldata,data)

