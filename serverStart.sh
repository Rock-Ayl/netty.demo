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