# Elastic-Job 学习笔记

## 前言

官网：http://elasticjob.io/index_zh.html

maven 地址：https://mvnrepository.com/artifact/com.dangdang/elastic-job-lite-core

maven 地址：https://mvnrepository.com/artifact/com.dangdang/elastic-job-lite-spring

官方配置手册：http://elasticjob.io/docs/elastic-job-lite/02-guide/config-manual/

## 初识 Elastic-Job 

Elastic-Job 是一个分布式调度解决方案，由两个相互独立的子项目 Elastic-Job-Lite 和 Elastic-Job-Cloud 组成。

Elastic-Job-Lite 定位为轻量级无中心化解决方案，使用 jar 包的形式提供分布式任务的协调服务；

Elastic-Job-Cloud 采用自研 Mesos Framework 的解决方案，额外提供资源治理、应用分发以及进程隔离等功能。

### 基本概念

* **分片概念：** 任务的分布式执行，需要将一个任务拆分为多个独立的任务项，然后由分布式的服务器分别执行某一个或几个分片项。
* **分片项与业务处理解耦： ** Elastic-Job 并不直接提供数据处理的功能，框架只会将分片项分配至各个运行中的作业服务器，开发者需要自行处理分片项与真实数据的对应关系。
* **个性化参数的适用场景：** 个性化参数即 shardingItemParameter，可以和分片项匹配对应关系，用于将分片项的数字转换为更加可读的业务代码。

### 核心理念

* **分布式调度：** Elastic-Job-Lite 并无作业调度中心节点，而是基于部署作业框架的程序在到达相应时间点时各自触发调度。注册中心仅用于作业注册和监控信息存储。而主作业节点仅用于处理分片和清理等功能。
* **作业高可用：**  Elastic-Job-Lite 提供最安全的方式执行作业。将分片总数设置为1，并使用多于1台的服务器执行作业，作业将会以 1 主 n 从的方式执行。一旦执行作业的服务器崩溃，等待执行的服务器将会在下次作业启动时替补执行。开启失效转移功能效果更好，可以保证在本次作业执行时崩溃，备机立即启动替补执行。
* **最大限度利用资源：** Elastic-Job-Lite也提供最灵活的方式，最大限度的提高执行作业的吞吐量。将分片项设置为大于服务器的数量，最好是大于服务器倍数的数量，作业将会合理的利用分布式资源，动态的分配分片项。

### 整体架构图

![elastic_job_lite](img/elastic_job_lite.png)

## zookeeper 作为注册中心

### zookeeper 安装与配置

参考链接：[https://blog.csdn.net/SIMBA1949/article/details/91374031](https://blog.csdn.net/SIMBA1949/article/details/91374031)

## Elastic-Job 入门

### 作业类型说明

Elastic-Job 提供 Simple、Dataflow 和 Script 三种作业类型。方法参数 shardingContext 包含作业配置、分片和运行时信息。可通过 getShardingTotalCount() 、getShardingItem() 等方法分别获取分片总数，运行在本作业服务器的分片序列号等。

* **Simple 类型作业：** 意为简单实现，未经任何封装的类型。需要实现 SimpleJob 接口。该接口仅提供单一方法用于实现，此方法讲定时执行。与 Quartz 原生接口相似，但提供了弹性扩缩容和分片等功能。
* **Dataflow 类型作业：** Dataflow 类型用于处理数据流，需要实现 DataflowJob 接口。该接口提供两个方法，可供实现，分别用于抓取（fetchData）和处理（processData）数据。
* **Script 类型作业：** 意为脚本类型作业，支持 shell、python、perl 等所有类型脚本。只需要通过控制台或者代码配置 scriptCommandLine 即可，无需编码。执行脚本路径可包含参数，参数传递完毕后，作业框架会自动追加最后一个参数为作业运行时信息。

### Elastic-Job 官方配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:reg="http://www.dangdang.com/schema/ddframe/reg"
       xmlns:job="http://www.dangdang.com/schema/ddframe/job"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                           http://www.springframework.org/schema/beans/spring-beans.xsd 
                           http://www.dangdang.com/schema/ddframe/reg 
                           http://www.dangdang.com/schema/ddframe/reg/reg.xsd 
                           http://www.dangdang.com/schema/ddframe/job 
                           http://www.dangdang.com/schema/ddframe/job/job.xsd 
                           ">
    <!--配置作业注册中心 -->
    <reg:zookeeper id="regCenter" server-lists="yourhost:2181" namespace="dd-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />

    <!-- 配置简单作业-->
    <job:simple id="simpleElasticJob" class="xxx.MySimpleElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />

    <bean id="yourRefJobBeanId" class="xxx.MySimpleRefElasticJob">
        <property name="fooService" ref="xxx.FooService"/>
    </bean>

    <!-- 配置关联Bean作业-->
    <job:simple id="simpleRefElasticJob" job-ref="yourRefJobBeanId" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />

    <!-- 配置数据流作业-->
    <job:dataflow id="throughputDataflow" class="xxx.MyThroughputDataflowElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />

    <!-- 配置脚本作业-->
    <job:script id="scriptElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" script-command-line="/your/file/path/demo.sh" />

    <!-- 配置带监听的简单作业-->
    <job:simple id="listenerElasticJob" class="xxx.MySimpleListenerElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C">
        <job:listener class="xx.MySimpleJobListener"/>
        <job:distributed-listener class="xx.MyOnceSimpleJobListener" started-timeout-milliseconds="1000" completed-timeout-milliseconds="2000" />
    </job:simple>

    <!-- 配置带作业数据库事件追踪的简单作业-->
    <job:simple id="eventTraceElasticJob" class="xxx.MySimpleListenerElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" event-trace-rdb-data-source="yourDataSource">
    </job:simple>
</beans>
```

### Simple 作业开发

#### pom依赖

```xml
<!-- https://mvnrepository.com/artifact/com.dangdang/elastic-job-lite-core -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-lite-core</artifactId>
    <version>2.1.5</version>
</dependency>
<!-- https://mvnrepository.com/artifact/com.dangdang/elastic-job-lite-spring -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-lite-spring</artifactId>
    <version>2.1.5</version>
</dependency>
```

#### springboot 配置文件

```properties
spring.application.name=elastic-job-simple
```

#### 配置文件 application-elastic-job.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:reg="http://www.dangdang.com/schema/ddframe/reg"
       xmlns:job="http://www.dangdang.com/schema/ddframe/job"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.dangdang.com/schema/ddframe/reg
                        http://www.dangdang.com/schema/ddframe/reg/reg.xsd
                        http://www.dangdang.com/schema/ddframe/job
                        http://www.dangdang.com/schema/ddframe/job/job.xsd
                        ">
<!--配置作业注册中心
    server-lists ：zookeeper 服务地址:端口
    namespace ： zookeeper 命名空间
    max-retries ：最大尝试连接 zookeeper 次数
    base-sleep-time-milliseconds 等待重试的时间毫秒数（初始值）
    max-sleep-time-milliseconds ：等待重试的最大时间毫秒数
-->
<reg:zookeeper id="regCenter" server-lists="192.168.8.130:2181" namespace="dd-job"
               base-sleep-time-milliseconds="1000"
               max-sleep-time-milliseconds="3000"
               max-retries="3" />

<!-- 配置作业
    id ：作业名称
    class ：作业全限定名
    registry-center-ref ： 指定注册中心
    cron ： cron 表达式
    sharding-total-count ：分片总数
    sharding-item-parameters ：分片项参数值—— 分片项索引号=参数值，多个参数使用逗号隔开
-->
<job:simple id="oneOffElasticJob" class="top.simba1949.job.MySimpleJob" registry-center-ref="regCenter"
            cron="0/10 * * * * ?"
            sharding-total-count="4"
            sharding-item-parameters="0=A,1=B,2=C,3=D" />
</beans>
```

#### 导入配置文件的Java类

```java
package top.simba1949.config;

import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

/**
 * @Author SIMBA1949
 * @Date 2019/9/7 19:56
 */
@Component
@ImportResource({"classpath:application-elastic-job.xml"})
public class JobConfig {
}
```

#### 执行分片任务的Java类

```java
package top.simba1949.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;

/**
 * 执行分片操作定时任务调度
 * 
 * @Author SIMBA1949
 * @Date 2019/9/7 19:39
 */
public class MySimpleJob implements SimpleJob {

    @Override
    public void execute(ShardingContext shardingContext) {
        //
        String jobName = shardingContext.getJobName();
        //
        String jobParameter = shardingContext.getJobParameter();
        // 获取分片项
        int shardingItem = shardingContext.getShardingItem();
        // 获取分片项参数
        String shardingParameter = shardingContext.getShardingParameter();
        // 获取分片总数
        int shardingTotalCount = shardingContext.getShardingTotalCount();
        //
        String taskId = shardingContext.getTaskId();

        switch (shardingItem){
            case 0:
                System.err.println("shardingTotalCount=" + shardingTotalCount + "\tshardingItem" + shardingItem + "shardingParameter=" + shardingParameter);
                break;
            case 1:
                System.err.println("shardingTotalCount=" + shardingTotalCount + "\tshardingItem" + shardingItem + "shardingParameter=" + shardingParameter);
                break;
            case 2:
                System.err.println("shardingTotalCount=" + shardingTotalCount + "\tshardingItem" + shardingItem + "shardingParameter=" + shardingParameter);
                break;
            case 3:
                System.err.println("shardingTotalCount=" + shardingTotalCount + "\tshardingItem" + shardingItem + "shardingParameter=" + shardingParameter);
                break;
        }

    }
}
```

#### springboot 启动类

```java
package top.simba1949;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author SIMBA1949
 * @Date 2019/9/7 19:38
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Dataflow 类型作业

#### pom 依赖

```xml
<!-- https://mvnrepository.com/artifact/com.dangdang/elastic-job-lite-core -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-lite-core</artifactId>
    <version>2.1.5</version>
</dependency>
<!-- https://mvnrepository.com/artifact/com.dangdang/elastic-job-lite-spring -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-lite-spring</artifactId>
    <version>2.1.5</version>
</dependency>
```

#### springboot 配置文件

```properties
spring.application.name=elastic-job-dataflow
```

#### 配置文件 application-elastic-job.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:reg="http://www.dangdang.com/schema/ddframe/reg"
       xmlns:job="http://www.dangdang.com/schema/ddframe/job"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.dangdang.com/schema/ddframe/reg
                        http://www.dangdang.com/schema/ddframe/reg/reg.xsd
                        http://www.dangdang.com/schema/ddframe/job
                        http://www.dangdang.com/schema/ddframe/job/job.xsd
                        ">
<!--配置作业注册中心
    server-lists ：zookeeper 服务地址:端口
    namespace ： zookeeper 命名空间
    max-retries ：最大尝试连接 zookeeper 次数
    base-sleep-time-milliseconds 等待重试的时间毫秒数（初始值）
    max-sleep-time-milliseconds ：等待重试的最大时间毫秒数
-->
<reg:zookeeper id="regCenter" server-lists="192.168.8.130:2181" namespace="dd-job"
               base-sleep-time-milliseconds="1000"
               max-sleep-time-milliseconds="3000"
               max-retries="3" />

<!-- 配置作业
    id ：作业名称
    class ：作业全限定名
    registry-center-ref ： 指定注册中心
    cron ： cron 表达式
    sharding-total-count ：分片总数
    sharding-item-parameters ：分片项参数值—— 分片项索引号=参数值，多个参数使用逗号隔开
-->
<job:dataflow id="myDataflowJob" class="top.simba1949.job.MyDataflowJob" registry-center-ref="regCenter"
            cron="0/10 * * * * ?"
            sharding-total-count="4"
            sharding-item-parameters="0=A,1=B,2=C,3=D" />
</beans>
```

#### 导入配置文件的 Java 类

```java
package top.simba1949.conf;

import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

/**
 * @Author SIMBA1949
 * @Date 2019/9/7 20:34
 */
@Component
@ImportResource({"classpath:application-elastic-job.xml"})
public class ElasticJobConfig {
}
```

#### 执行分片任务的 Java 类

```java
package top.simba1949.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import org.springframework.beans.factory.annotation.Autowired;
import top.simba1949.common.User;
import top.simba1949.service.UserService;

import java.util.List;

/**
 * Dataflow类型用于处理数据流，需实现DataflowJob接口。
 * @Author SIMBA1949
 * @Date 2019/9/7 20:35
 */
public class MyDataflowJob implements DataflowJob<User> {

    @Autowired
    private UserService userService;
    /**
     *
     * @param shardingContext
     * @return
     */
    @Override
    public List<User> fetchData(ShardingContext shardingContext) {
        int shardingItem = shardingContext.getShardingItem();
        switch (shardingItem){
            case 0:
                // get data from database by sharding item 0
                List<User> data0 = userService.oneList();
                return data0;
            case 1:
                // get data from database by sharding item 0
                List<User> data1 = userService.twoList();
                return data1;
            case 2:
                // get data from database by sharding item 0
                List<User> data2 = userService.threeList();
                return data2;
            case 3:
                // get data from database by sharding item 0
                List<User> data3 = userService.fourList();
                return data3;
            default:
                ;
        }
        return null;
    }

    /**
     *
     * @param shardingContext
     * @param list
     */
    @Override
    public void processData(ShardingContext shardingContext, List<User> list) {
        // list 是每个分片对应获取的数据
        System.err.println("getShardingItem" + shardingContext.getShardingItem());
        list.forEach(item -> {
            System.out.print(item);
        });
        System.out.println("");
    }
}
```

#### User 实体类

```java
package top.simba1949.common;

import lombok.Data;

import java.util.Date;

/**
 * @Author SIMBA1949
 * @Date 2019/9/7 20:35
 */
@Data
public class User {
    private Long id;
    private String username;
    private Date birthday;
    private Integer status;
}
```

#### 虚拟从数据库中取出数据的业务层

```java
package top.simba1949.service;

import org.springframework.stereotype.Service;
import top.simba1949.common.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author SIMBA1949
 * @Date 2019/9/7 20:50
 */
@Service
public class UserService {

    private List<User> list = new ArrayList<>(10);
    private static final int UN_DELETE = 0;
    private static final int DELETE = 1;

    public List<User> oneList(){
        List<User> users = new ArrayList<>(10);
        for (int i = 0; i < 10; i++){
            User user = new User();
            user.setId(Long.valueOf(i));
            user.setUsername("00-李白" + i);
            user.setBirthday(new Date());
            user.setStatus(UN_DELETE);

            users.add(user);
        }

        return users;
    }

    public List<User> twoList(){
        List<User> users = new ArrayList<>(10);
        for (int i = 0; i < 10; i++){
            User user = new User();
            user.setId(Long.valueOf(i));
            user.setUsername("01-杜甫" + i);
            user.setBirthday(new Date());
            user.setStatus(UN_DELETE);

            users.add(user);
        }

        return users;
    }

    public List<User> threeList(){
        List<User> users = new ArrayList<>(10);
        for (int i = 0; i < 10; i++){
            User user = new User();
            user.setId(Long.valueOf(i));
            user.setUsername("02-白居易" + i);
            user.setBirthday(new Date());
            user.setStatus(UN_DELETE);

            users.add(user);
        }

        return users;
    }

    public List<User> fourList(){
        List<User> users = new ArrayList<>(10);
        for (int i = 0; i < 10; i++){
            User user = new User();
            user.setId(Long.valueOf(i));
            user.setUsername("03-孟浩然" + i);
            user.setBirthday(new Date());
            user.setStatus(UN_DELETE);

            users.add(user);
        }

        return users;
    }
}
```

#### springboot 启动类

```java
package top.simba1949;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author SIMBA1949
 * @Date 2019/9/7 20:34
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Script 类型作业

#### pom 依赖

```xml
<!-- https://mvnrepository.com/artifact/com.dangdang/elastic-job-lite-core -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-lite-core</artifactId>
    <version>2.1.5</version>
</dependency>
<!-- https://mvnrepository.com/artifact/com.dangdang/elastic-job-lite-spring -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-lite-spring</artifactId>
    <version>2.1.5</version>
</dependency>
```

#### springboot 配置文件

```properties
spring.application.name=elastic-job-script
```

#### 作业配置文件 application-elastic-job.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:reg="http://www.dangdang.com/schema/ddframe/reg"
       xmlns:job="http://www.dangdang.com/schema/ddframe/job"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.dangdang.com/schema/ddframe/reg
                        http://www.dangdang.com/schema/ddframe/reg/reg.xsd
                        http://www.dangdang.com/schema/ddframe/job
                        http://www.dangdang.com/schema/ddframe/job/job.xsd
                        ">
<!--配置作业注册中心
    server-lists ：zookeeper 服务地址:端口
    namespace ： zookeeper 命名空间
    max-retries ：最大尝试连接 zookeeper 次数
    base-sleep-time-milliseconds 等待重试的时间毫秒数（初始值）
    max-sleep-time-milliseconds ：等待重试的最大时间毫秒数
-->
<reg:zookeeper id="regCenter" server-lists="192.168.8.131:2181" namespace="dd-job"
               base-sleep-time-milliseconds="1000"
               max-sleep-time-milliseconds="3000"
               max-retries="3" />

<!-- 配置作业
    id ：作业名称
    registry-center-ref ： 指定注册中心
    cron ： cron 表达式
    sharding-total-count ：分片总数
    sharding-item-parameters ：分片项参数值—— 分片项索引号=参数值，多个参数使用逗号隔开
    script-command-line : 要执行的脚本
-->
<job:script id="oneOffElasticJob"  registry-center-ref="regCenter"
            cron="0/10 * * * * ?"
            sharding-total-count="4"
            sharding-item-parameters="0=A,1=B,2=C,3=D"
            script-command-line="D:\IDE\IDEA\Workspace\Learn\elastic-job-learn\elastic-job-script\src\main\resources\print.bat"/>
</beans>
```

#### 脚本文件 print.bat 

```bash
echo "君不见黄河之水天上来"
```

#### 导入作业配置文件的 Java 类

```java
package top.simba1949.config;

import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

/**
 * @Author SIMBA1949
 * @Date 2019/9/8 8:15
 */
@Component
@ImportResource({"classpath:application-elastic-job.xml"})
public class ElasticJobConfig {
}
```

#### springboot 启动类

```java
package top.simba1949;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author SIMBA1949
 * @Date 2019/9/8 8:02
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```



