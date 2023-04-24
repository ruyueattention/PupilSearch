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


# 更新说明

1. 因为是个爬虫，所以索性把socket代理去了，只保留了一个http代理，用来给bp做调试。真要用到的时候建议由bp来挂socket。
2. 使用树结构和广度优先遍历算法来去掉了冗余的查询，并且现在能知道敏感数据是来自哪个页面的。
3. 使用阻塞队列来存储控制任务列表，防止任务重复查询及防止线程竞争死锁。
3. 对部分正则表达式进行了优化。
4. 针对一些api类型的接口进行了post尝试。
5. 支持批量扫描。
6. 支持自定义http头，能够进行登录态后的扫描。


PS:重构了挺多内容的，比起python的效率高很多，建议使用这个版本的。


# 使用方法

## 快速使用

### 输入目标

```shell
java -jar xxx.jar  -u http://target.com 
```
最简单使用就是传入参数-u或者-f 即可。
如果结果不理想，建议加上-a true 参数，这样可能会有误报，注意识别哪些是误报。

PS:第一个http请求是不走代理的，由htmlunit对主页面进行渲染，以获取更多结果。后续的请求都会由OKhttp发起，并且会走代理，若想修改，可在com.ruyue.util.Rendering文件中修改。


### 自定义参数

输入-h可以看到所有参数介绍。
特别说明：

​	-d 追溯深度：0则代表只针对当前url搜索，不追溯其他url，1则代表对当前url得出的js等结果进行追溯，2则是对它的结果的结果进行追溯。一般的网站追溯到3就没数据了。

​   -a：默认情况下是不会对抓取到的所有url都进行追溯的，只会追溯当前host的url。开启后，则追溯全部的url，所以该参数慎用。在开启这个功能的情况下，推荐追溯深度调小。

   -f：传入url列表

   -p ： 代理设置。




# 已知缺陷

1. 数据是经正则进行匹配的，可能会导致部分数据匹配不成功或者匹配到脏数据。尤其是ip和password和username等信息。

   可根据自己的具体使用进行正则修改，在SearchTask中。







# 联系作者

mail: ruyuezhuanzhu@qq.com	

