# 基于netty的轻量服务器架构

## 技术栈

- JDK 1.8
- netty 4.X
- MariaDB(MySql) 10.4.10
- Redis 5.0.7
- Mongo 4.0.9

## 支持协议

- Http
- WebSocket

## 支持服务

- WebSocket聊天室
- 业务接口,目前支持四种请求,Get Post Put Delete
- 普通文件、静态文件的上传下载预览及相关浏览器策略定制,支持Range

## 作者相关

邮箱：AnYongLiang4869@163.com

非常欢迎对该框架有问题的小伙伴发邮件给我,我会给每一封邮件解答问题.

# 已知bug或使用时要注意的问题

1.对外接口在参数暴露/解析上,会有优先级,比如传个num=5,既可以用Integer去接,也可以用Long去接,甚至可以用String去接,但程序会有一个优先级判定,请自行斟酌修改对应逻辑

2.使用postman上传文件可能会出现异常的问题,但仅是Postman,其他方式无碍,尚不知原因.

## Add dependencies to build.gradle.

```
   //依赖
   dependencies {
   
       //netty-4
           compile group: 'io.netty', name: 'netty-all', version: '4.1.19.Final'
           //mongo-Bson
           compile group: 'org.mongodb', name: 'mongo-java-driver', version: '3.8.2'
           //google-Gson
           compile group: 'com.google.code.gson', name: 'gson', version: '2.8.5'
           //apache-commons工具包
           compile group: 'org.apache.commons', name: 'commons-dbcp2', version: '2.1.1'
           compile group: 'org.apache.commons', name: 'commons-lang3', version: '3.6'
           compile group: 'org.apache.commons', name: 'commons-collections4', version: '4.4'
           compile group: 'commons-io', name: 'commons-io', version: '2.5'
           compile group: 'commons-codec', name: 'commons-codec', version: '1.10'
           //日志-slf4j
           compile group: 'org.slf4j', name: 'slf4j-log4j12', version: '1.7.25'
           //gitHub-超轻量级的Java类路径和模块路径扫描器
           compile group: 'io.github.lukehutch', name: 'fast-classpath-scanner', version: '2.18.1'
           //谷歌-Gson
           compile group: 'com.google.code.gson', name: 'gson', version: '2.8.5'
           //定时器-quartz
           compile group: 'org.quartz-scheduler', name: 'quartz', version: '2.3.0'
           //mongo-Bson
           compile group: 'org.mongodb', name: 'mongo-java-driver', version: '3.8.2'
           //mysql-jdbc
           compile group: 'mysql', name: 'mysql-connector-java', version: '6.0.6'
           //阿里 java连接池-druid
           compile group: 'com.alibaba', name: 'druid', version: '1.1.21'
           //redis、redisson
           compile group: 'redis.clients', name: 'jedis', version: '2.9.0'
           compile group: 'org.redisson', name: 'redisson', version: '3.5.4'
           //Etcd-连接池
           compile group: 'org.mousio', name: 'etcd4j', version: '2.17.0'
           //ElasticSearch-7.9.1
           compile group: 'org.elasticsearch', name: 'elasticsearch', version: '7.9.1'
           compile group: 'org.elasticsearch.client', name: 'elasticsearch-rest-high-level-client', version: '7.9.1'
           //neo4j-服务器开发依赖
           compile group: 'org.neo4j.driver', name: 'neo4j-java-driver', version: '1.5.1'
           //apache tika-文本抽取
           compile group: 'org.apache.tika', name: 'tika-parsers', version: '1.24.1'
   
   }
   
```

## 1.定义接口

>通过注解`@Service`和继承`IMicroService`暴露服务

>通过`@Method`暴露方法,方法备注,是否需要验证身份(默认不需要),请求类型(默认是Post)

>通过`@Param`暴露参数,参数备注,参数是否可选(默认必传)


``` java

@Service(desc = "用户")
public interface User extends IMicroService {

    @Method(desc = "测试", auth = true, command = RequestMethod.Get)
    JsonObject test(
            @Param(value = "关键词", optional = true) String keyword
    );

    @Method(desc = "获取用户列表", auth = true)
    JsonObject readUserList(
            @Param(value = "关键词", optional = true) String keyword,
            @Param(value = "第几页", optional = true) Integer pageIndex,
            @Param(value = "每页几条数据", optional = true) Integer pageSize
    );

    @Method(desc = "用户登录")
    JsonObject login(
            @Param("账户") String account,
            @Param("密码") String password
    );

}

 ```
 
 ## 2.实现接口
 
 >继承`Context`并实现要实现的接口
 
 ``` java
public class UserService extends Context implements User {

    @Override
    public JsonObject readUserList(String keyword, Integer pageIndex, Integer pageSize) {
        //验证权限为root
        if (!UserCommons.isRoot(this.ctxUserId)) {
            return Const.Json_No_Permission;
        }
        //验证关键词
        if (StringUtils.isEmpty(keyword)) {
            //缺省
            keyword = "";
        } else if (!PatternUtils.isUserName(keyword)) {
            return Const.Json_Not_Keyword;
        }
        //查询并返回
        return UserCommons.readUserList(keyword, pageIndex, pageSize);
    }

    @Override
    public JsonObject login(String account, String password) {
        //如果key不是手机号
        if (!PatternUtils.isMobile(account)) {
            return Const.Json_Not_Mobile;
        }
        //如果key不是密码
        if (!PatternUtils.isUserName(password)) {
            return Const.Json_Not_Password;
        }
        //获取用户信息
        JsonObject userInfo = UserCommons.readUserInfo(account, password);
        //判空
        if (userInfo == null) {
            return Const.Json_No_User;
        }
        //生成用户cookieId
        String cookieId = IdUtils.newId();
        //组装至用户信息
        userInfo.append("cookieId", cookieId);
        //删除密码
        userInfo.remove("password");
        //将用户登录缓存写入redis中
        Redis.user.set(cookieId, userInfo.toString());
        //返回用户数据
        return JsonObject.Success().append(Const.Data, userInfo);
    }

}

 ```

## 3.快速启动

================

 >IDE中直接用`Server`的`main`启动
 
 
 ``` java
public class Server {

    public static void main(String[] args) {
        //启动定时器线程
        Scheduler.startup();
        //扫描所有服务已存在
        RegistryEntry.scanServices();
        //扫描之后，可以启动netty监听
        SocketServer.SocketServer.startup();
    }

}

 ```
 
 >服务器中用脚本`serverStart.sh` (注意配置变量)

 ``` Bash
#!/bin/bash

#jar包路径(基于当前路径)
APP_NAME="build/libs/netty.demo-1.0.jar"

#进程PID
pid=0

Help() {
    echo "case: sh run.sh [start|stop|restart|status]"
    echo "请类似这样执行 ./*.sh start   or  ./*sh restart"
    exit 1
}

# 判断当前服务是否已经启动的函数
checkPID(){
    #根据PID
    pid=`ps -ef|grep $APP_NAME|grep -v grep|awk '{print $2}' `
    #判断pid是否为空 0:存在进程 1:不存在进程
    if [ -z "${pid}" ]; then
        return 1
    else
        return 0
    fi
}

# 启动
start(){
     echo "#######进程启动##########"
    checkPID
     # [$? -eq "0"] pid存在 说明服务正在运行中，将进程号打印出来
    if [ $? -eq "0" ]; then
        echo "${APP_NAME} 已经启动,PID:${pid}"
    else
        # pid为空 执行java -jar 命令启动服务
        nohup java -jar $APP_NAME >/dev/null 2>&1 &
        echo "${APP_NAME} 正在启动了."
    fi
    echo "#########End#############"
}

# 停止
stop(){
     echo "#######进程停止##########"
    checkPID
     # [$? -eq "0"] 说明pid不等于空 说明服务正在运行中，将进程号杀死
    if [ $? -eq "0" ]; then
        kill -9 $pid
        echo "${pid} 进程被停止."
    else
        echo "${APP_NAME} 没有启动."
    fi
    echo "#########End#############"
}

# 查看状态
status(){
    echo "#######查看状态##########"
    checkPID
    if [ $? -eq "0" ]; then
        echo "${APP_NAME} 正在启动,PID:${pid}"
    else
        echo "${APP_NAME} 没有启动"
    fi
    echo "#########End#############"
}


# 重启
restart(){
    stop
    start
}


# 命令表
case "$1" in
    "start")
        start
        ;;
    "stop")
        stop
        ;;
    "status")
        status
        ;;
    "restart")
        restart
        ;;
    *)
    Help
    ;;
esac

 ```

## 4.可配置信息所在

- 配置文件:setting.properties
- 代码中的常量:Const.Java
- 日志配置文件:log4j.properties

## 5.其他相关文件

/netty.demo/src/main/resources目录下

- 文件的上传的html
- WebSocket聊天室的html
- postman接口调用导出json(postman的form-data上传有bug,最好使用页面上传测试)

## 6.备注

```

该框架尚有很多未完善的地方,但可以作为一个学习netty的demo.

非常适合那些刚学会netty4官网的小伙伴,通过该项目

你可以学习到如何自己搭建一个单机版本的服务器框架

该框架优点:注释多,除了netty基本啥都没有,超轻量,不需要依赖Spring全家桶

```