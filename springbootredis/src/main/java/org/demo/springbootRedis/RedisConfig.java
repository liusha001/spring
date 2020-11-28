package org.demo.springbootRedis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.ReadFrom;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singletonMap;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;


/**
 * @author chenrf
 * @version 1.0
 * @date 2020/11/15 14:25
 */
@Configuration
@EnableTransactionManagement//试过了没有这也可以
public class RedisConfig {
    @Autowired
    RedisProperties redisProperties;

    /**
     * 配置单机版的redis服务器
     * @return
     */
    @Primary
    @Bean(name="singleRedisConnectionFactory")
    public LettuceConnectionFactory singleRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());
        config.setPort(redisProperties.getPort());
//        config.setDatabase(redisProperties.getDatabase());
//        config.setPassword(RedisPassword.of(redisProperties.getPassword()));
        return new LettuceConnectionFactory(config);
    }
    /**
     * 主写入，备读取，但是没有故障切换功能
     * RedisStandaloneConfiguration：适用于单节点
     * @return
     */
    @Bean(name="singleMasterReplicaRedisConnectionFactory")
    public LettuceConnectionFactory upReplicaRedisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build();
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
        return new LettuceConnectionFactory(config, clientConfig);
    }

    /**
     * 主写入，备读取，但是没有故障切换功能
     * 对于通过INFO命令发布的非公共的地址的环境（比如，使用AWS)，使用RedisStaticMasterReplicaConfiguration，而不是RedisStandaloneConfiguration
     * RedisStandaloneConfiguration：适用于单节点
     * RedisStaticMasterReplicaConfiguration：适用于主从模式，并且host固定不变，但不支持发布订阅
     * @return
     */
    @Bean(name="staticMasterReplicaRedisConnectionFactory")
    public LettuceConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build();
        RedisStaticMasterReplicaConfiguration config = new RedisStaticMasterReplicaConfiguration(redisProperties.getHost(), redisProperties.getPort());
        return new LettuceConnectionFactory(config, clientConfig);
    }
    /**
     * 哨兵模式，支持主备切换
     */
    @Bean(name="redisSentinelConfiguration")
    public LettuceConnectionFactory lettuceConnectionFactory() {
        // 这个需要配置的是sentinel的端口
        Set<String> setRedisNode = new HashSet<>();
        setRedisNode.add("192.168.1.102:16380");
        setRedisNode.add("192.168.1.102:26380");
        setRedisNode.add("192.168.1.104:36380");
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration("mymaster", setRedisNode);
        return new LettuceConnectionFactory(sentinelConfig);
    }

    @Bean
    public RedisTemplate redisTemplate(@Qualifier("singleRedisConnectionFactory") LettuceConnectionFactory factory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
        Jackson2JsonRedisSerializer jacksonSeial = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(),ObjectMapper.DefaultTyping.NON_FINAL);
        jacksonSeial.setObjectMapper(om);

        //使用StringRedisSerializer来序列化和反序列化redis的key值
        // json序列化Value
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(jacksonSeial);

        // 设置hash key 和value序列化模式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(jacksonSeial);
        redisTemplate.afterPropertiesSet();
        //启用事务
        redisTemplate.setEnableTransactionSupport(true);
        return redisTemplate;
    }

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory(GenericObjectPoolConfig genericObjectPoolConfig) {
        // 单机版配置
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName(redisProperties.getHost());
        serverConfig.setPort(redisProperties.getPort());
        // 集群版配置
//        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
//        String[] serverArray = clusterNodes.split(",");
//        Set<RedisNode> nodes = new HashSet<RedisNode>();
//        for (String ipPort : serverArray) {
//            String[] ipAndPort = ipPort.split(":");
//            nodes.add(new RedisNode(ipAndPort[0].trim(), Integer.valueOf(ipAndPort[1])));
//        }
//        redisClusterConfiguration.setPassword(RedisPassword.of(password));
//        redisClusterConfiguration.setClusterNodes(nodes);
//        redisClusterConfiguration.setMaxRedirects(maxRedirects);
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(redisProperties.getTimeout())
                .poolConfig(genericObjectPoolConfig)
                .build();
        if (redisProperties.isSsl()){
            clientConfig.isUseSsl();
        }
        LettuceConnectionFactory factory = new LettuceConnectionFactory(serverConfig, clientConfig);
        return factory;
    }

    @Bean
    public GenericObjectPoolConfig genericObjectPoolConfig() {
        RedisProperties.Pool pool = redisProperties.getLettuce().getPool();
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxIdle(pool.getMaxIdle());
        genericObjectPoolConfig.setMinIdle(pool.getMinIdle());
        genericObjectPoolConfig.setMaxTotal(pool.getMaxActive());
        genericObjectPoolConfig.setMaxWaitMillis(pool.getMaxWait().toMillis());
        return genericObjectPoolConfig;
    }
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        //缓存参数配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
//                .prefixKeysWith()//这里可以配置key前缀
//                .computePrefixWith()//带计算功能的key前缀比如(cacheName -> "redis" + cacheName)
                .entryTtl(Duration.ofSeconds(1))
                .disableCachingNullValues()
//                .serializeKeysWith() //序列化key
//                .serializeValuesWith() //系列化value
                ;
        //缓存管理器
//        RedisCacheManager cm = RedisCacheManager.builder(connectionFactory)//创建没有多是manager
        RedisCacheManager cm = RedisCacheManager.builder(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory))//创建带有写锁的manager
                .cacheDefaults(defaultCacheConfig())
                .withInitialCacheConfigurations(singletonMap("cache001", config))//差异化配置不同的cache
                .transactionAware()
                .build();
        return cm;
    }
}
