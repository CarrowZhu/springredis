# springredis
基于spring-data-redis的注解实现redis缓存操作
## requirement
JDK6 <br>
Spring4<br>

## 原理&实现
1）AOP<br>
2）实现参考自Spring的Cache注解<br>
区别：<br>
1）支持TTL <br>
2）支持Hash <br>

## 配置说明
### XML配置文件
xsi:schemaLocation="http://www.siyuan.com/schema/springredis <br>
       http://www.siyuan.com/schema/springredis/springredis.xsd" <br>
<springRedis:annotation-driven /> <br>
属性说明 <br>
redisTemplate：Advice中将使用的redisTemplate，默认为"redisTemplate" <br>
order：Advice的执行顺序,默认优先级最高(Ordered.HIGHEST_PRECEDENCE) <br>
exceptionHandler：beanId,操作异常处理器，必须实现接口com.siyuan.springredis.interceptor.SpringRedisExceptionHandler,默认为com.siyuan.springredis.interceptor.LoggerExceptionHandler <br>

### 注解
1）@SpringRedisConfig：Class级别配置 <br>
属性说明 <br>
value：等同于redisTemplate <br>
redisTemplate：（String）Advice中将使用的redisTemplate <br>

2）@SpringRedisValueCache：方法级别，操作的数据类型为String <br>
对应操作流程：读cache，hit返回，miss -> 获取数据 -> cache <br>
属性说明 <br> 
value：等同于key <br>
redisTemplate：（String）Advice中将使用的redisTemplate <br>
condition：（String）支持SpringEL，缓存操作条件 <br>
timeout：（long）TTL，<=0表示永不过期，默认为0 <br>
timeUnit：（TimeUnit）TTL单位，默认为TimeUnit.MILLISECONDS <br>
key：（String）支持SpringEL，缓存对应的key值，必须提供 <br>
refreshTTL：（boolean）缓存命中时是否刷新TTL，默认为false <br>

3）@SpringRedisValueEvict ：方法级别，操作的数据类型为String <br>
对应的流程：清除缓存 <br>
属性说明 <br> 
value：等同于key <br>
redisTemplate：（String）Advice中将使用的redisTemplate <br>
condition：（String）支持SpringEL，缓存操作条件 <br>
key：（String）支持SpringEL，缓存对应的key值，必须提供 <br>

4）@SpringRedisHashCache：方法级别，操作的数据类型为Hash <br>
对应操作流程：与 @SpringRedisValueCache 类似<br>
属性说明 <br> 
value：等同于key <br>
redisTemplate：（String）Advice中将使用的redisTemplate <br>
condition：（String）支持SpringEL，缓存操作条件 <br>
timeout：（long）TTL，<=0表示永不过期，默认为0 <br>
timeUnit：（TimeUnit）TTL单位，默认为TimeUnit.MILLISECONDS <br>
key：（String）支持SpringEL，缓存对应的key值，必须提供 <br>
refreshTTL：（boolean）缓存命中时是否刷新TTL，默认为false <br>
hashKey：（String）支持SpringEL，缓存对应的hashKey值，必须提供 <br>

5）@SpringRedisHashEvict ：方法级别，操作的数据类型为Hash <br>
对应的流程：与 @SpringRedisValueEvict 类似<br>
属性说明 <br> 
value：等同于key <br>
redisTemplate：（String）Advice中将使用的redisTemplate <br>
condition：（String）支持SpringEL，缓存操作条件 <br>
key：（String）支持SpringEL，缓存对应的key值，必须提供 <br>
hashKey：（String）支持SpringEL，缓存对应的hashKey值，必须提供 <br>

## 示例 <br>
参考 /src/test 路径下的测试用例 <br>
