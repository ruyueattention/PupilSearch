# PupilSearch

PupilSearch本质上就是一个爬虫，能够自动帮我们查询搜索web前端中泄露的信息。包括api接口、url、各种敏感信息（JWT TOKEN、IP、Email、Phone、身份证、AccessKey、SecretKey、APPID、UserName、Password、SSHKey、RSAKey）。

该爬虫已应用于Kunyu ，感谢风起。

https://github.com/knownsec/Kunyu



其工作流程大致如下:



![image-20220316151115942](https://ruyue-1258558004.cos.ap-guangzhou.myqcloud.com/note/image-20220316151115942.png)



# 功能特点

1. 递归回溯，根据输入参数设定追溯深度，自动递归回溯获取到的资源，再从中匹配敏感信息。
2. 文件各模块功能清晰，简介易改。
3. 保存有访问历史，根据访问历史自动去除追溯队列中的重复请求。
4. 初次访问会调用chromium进行渲染页面，防止部分数据是由js动态渲染添加的，导致识别不到的问题。
5. 按域名去重复保存结果
6. 判断当前js是否为webpack打包的js，对于疑是webpack打包的js，尝试获取map。
17. 多线程，不值一提。
17. 根据正则匹配大量敏感信息（可自行修改正则），目前支撑匹配JWT TOKEN、IP、Email、Phone、身份证、AccessKey、SecretKey、APPID、UserName、Password、SSHKey、RSAKey等等。

# 使用方法

## 环境准备

```shell
pip install -r requirement.txt
```

## 快速使用

### 输入目标

```shell
python3 PupilMain.py -u http://target.com 
```

其他参数不设置的话，就默认追溯深度为2、线程为5、不对接口进行追溯、不对非当前host的url进行追溯。

如果要设置请参考各个参数的功能，合理设置。

### 自定义参数

当前支持参数有 -t 线程   -d 追溯深度 -a 对所有抓取到的url进行追溯  -f 对获取到的接口进行追溯

特别说明：

   -u 目标：该有的/不能少，比如目标域名是aaa.com/sso/ ，不能偷懒为aaa.com/sso  。并且url中不能存在#。

​	-d 追溯深度：0则代表只针对当前url搜索，不追溯其他url，1则代表对当前url得出的js等结果进行追溯，2则是对它的结果的结果进行追溯。一般的网站追溯到3就没数据了。

​    -a：默认情况下是不会对抓取到的所有url都进行追溯的，只会追溯当前host的url。开启后，则追溯全部的url，所以该参数慎用。在开启这个功能的情况下，推荐追溯深度调小。

   -f：默认情况下不对获取到的接口进行追溯。

   --proxy ： 代理设置。


```shell
python3 PupilMain.py -u http://target.com -d 3 -a True -f True --proxy=http://127.0.0.1:8080
```

![image-20220316163054510](https://ruyue-1258558004.cos.ap-guangzhou.myqcloud.com/note/image-20220316163054510.png)





### 输出结果

结果会print在控制台，同时也会保存在result目录下。

![image-20220321172935254](https://ruyue-1258558004.cos.ap-guangzhou.myqcloud.com/note/image-20220321172935254.png)

![image-20220321172848541](https://ruyue-1258558004.cos.ap-guangzhou.myqcloud.com/note/image-20220321172848541.png)




# 已知缺陷

1. 数据是经正则进行匹配的，可能会导致部分数据匹配不成功或者匹配到脏数据。尤其是ip和password和username等信息。

   可根据自己的具体使用进行正则修改，在SearhData.py中。

![image-20220316163201802](https://ruyue-1258558004.cos.ap-guangzhou.myqcloud.com/note/image-20220316163201802.png)



2.部分页面在渲染页面的时候会有一些小bug，这里在遇到bug的时候会逃过渲染，这可能导致结果不准确。



# 各个文件的作用

PupilMain.py

入口文件，初始化各种参数，同时对一些参数进行简单的处理。

TrackUrl.py

追溯文件，主要是发送requests请求。

SearchData.py

数据获取文件，将requests获取到的数据在这里进行匹配处理。

在这里可以修改各种正则匹配式，匹配自己想要的数据。

ExportHandler.py

数据的输出，主要是对数据做一些简单的去重处理。

这里可以修改数据的返回形式。

GlobalVariable.py

设置跨文件全局变量。






# 联系作者

mail: ruyuezhuanzhu@qq.com	

