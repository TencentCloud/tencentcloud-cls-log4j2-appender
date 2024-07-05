Tencent CLS Log4j2 Appender SDK 
---

### Log4j简介 - "来自百度百科"

Log4j是Apache的一个开源项目，通过使用Log4j，我们可以控制日志信息输送的目的地是控制台、文件、GUI组件，甚至是套接口服务器、NT的事件记录器、UNIX Syslog守护进程等；我们也可以控制每一条日志的输出格式；
通过定义每一条日志信息的级别，我们能够更加细致地控制日志的生成过程。最令人感兴趣的就是，这些可以通过一个配置文件来灵活地进行配置，而不需要修改应用的代码。

Log4j 由三个重要的组件构成：
- 日志信息的优先级  
日志信息的优先级从高到低分别为 ERROR、WARN、INFO和DEBUG，分别用来指定这条日志信息的重要程度；
- 日志信息的输出目的地  
日志信息的输出目的地指定了日志将打印到控制台还是文件中
- 日志信息的输出格式  
而输出格式则控制了日志信息的显示内容

### 使用 Tencent CLS Log4j2 Appender

通Tencent CLS Log4j2 Appender，您可以控制日志的输出目的地为腾讯云日志服务。
写到日志服务中的日志的样式如下：

![demo](./demo.png)

|  字段   | 简介  |
| :----:   |:----:   |
| \_\_SOURCE__  | 来源ip |
| \_\_FILENAME__  | 文件名称 |
|level|日志级别|
|location|日志打印语句的代码位置|
|message|日志内容|
|throwable|日志异常信息（只有记录了异常信息，这个字段才会出现）|
|thread|线程名称|
|time|日志打印时间（可以通过 timeFormat或timeZone配置time字段呈现的格式和时区|
|log|自定义日志格式|

#### 工程引入和配置

- maven 工程中引入依赖
```
<dependency>
    <groupId>com.tencentcloudapi.cls</groupId>
    <artifactId>tencentcloud-cls-log4j2-appender</artifactId>
    <version>1.0.3</version>
</dependency>
```

- 修改log4j配置文件

```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Loghub name="Loghub"
                topicId="${topicId}"
                endpoint="ap-guangzhou.cls.tencentcs.com"
                accessKeyId="${accessKeyId}"
                accessKeySecret="${accessKeySecret}"
                totalSizeInBytes="104857600"
                maxBlockMs="0"
                sendThreadCount="8"
                batchSizeThresholdInBytes="524288"
                batchCountThreshold="4096"
                lingerMs="2000"
                retries="10"
                baseRetryBackoffMs="100"
                maxRetryBackoffMs="100"
                source=""
                timeFormat="yyyy-MM-dd'T'HH:mmZ"
                timeZone="UTC"
                ignoreExceptions="true"
                mdcFields="THREAD_ID1">
            <PatternLayout
                    pattern="%d{HH:mm:ss.SSS} %-5level %class{36} %L %M - %msg%xEx"/>
        </Loghub>
    </Appenders>
    <Loggers>
        <Root level="warn">
            <AppenderRef ref="Loghub"/>
        </Root>
    </Loggers>
</Configuration>
```

### 功能优势

- 日志不落盘：产生数据通过网络发给服务端。
- 无需改造：对已使用Log4J应用，只需简单配置即可采集
- 异步非阻塞：高并发设计，后台异步发送，适合高并发写入
- 资源可控制：可以通过参数控制 producer 用于缓存待发送数据的内存大小，同时还可以配置用于执行数据发送任务的线程数量
- 自动重试： 对可重试的异常，支持配置重试次数
- 优雅关闭： 推出前会将日志全量进行发送
- 上下文还原： ""
- 感知日志上报结果："运行过程中产生的异常通过 org.apache.log4j.helpers.LogLog 记录下来，LogLog 在默认情况下会将信息输出到控制台"





