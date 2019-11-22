# A Netty For Http And WebSocket Demo

use:

netty 4

jdk 1.8

remark:

================

2019-11-13日 更新，已将启动整合至Server上

如何使用？

启动Server,然后调用Post请求:

http://127.0.0.1:8888/Organize/login

body:

{
	name:"Rock-Ayl",
	pwd:123456,
	isRole:false
}

看intf中的Organize接口和其实现的OrganizeService

照着这个class和方法继续写就可以各种实现请求和返回Json的业务了。

这是一个超轻量的伪·微服务架构系统，因为目前只有单机，很多地方都没有完善。

@author是新人，照着我的老大架构仿照着去写的项目，纯粹用来练手。

P`S:习惯真是很可怕的事情。

================

更新：增加另一个测试接口，测试后，发现了已知问题，那就是只能用固定的几个对象去接参数，很不科学，准备修复

=================

更新：已解决上一个固定参数的bug
新bug:上传处理器，下载处理器，虽然能够成功，但都存在不小的问题

=================

上传，下载趋于稳定

下载链接eg:127.0.0.1:8888/Download?fileId=1&fileName=ayl.doc&type=p

上传eg：http://127.0.0.1:8888/Upload

header：
FileSize：178940269
FileType：application/msword
FileName：MDc26IOW5aKp5q+N5a2Q5aSn572i5belLm1rdg==
Content-Type：application/x-www-form-urlencoded

form-data：选择一个文件

==================

2019-11-22

目前已经趋于成熟

拥有基础的service服务、下载，、上传、websocket处理。

DB三件套：mysql,redis,mongo

前后端完全分离。