package redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.*;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;


/**
 * @author liusha
 * @version 1.0
 * @date 2020/11/29 12:20
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)//也可以自己new一个类就可以了使用CacheProperties
public class RedisConfig extends CachingConfigurerSupport {
    @Autowired
    RedisProperties redisProperties;
    @Autowired
    CacheProperties cacheProperties;

    /**
     * key的生成策略
     * @return
     */
    @Bean(name = "simpleKeyGenerator")
    public KeyGenerator simpleKeyGenerator() {
        //这个方法并不是很好，因为这会导致参数太长，建议还是在注解的地方自定义key
        return (o, method, objects) -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(o.getClass().getSimpleName());
            stringBuilder.append(".");
            stringBuilder.append(method.getName());
            stringBuilder.append("[");
            for (Object obj : objects) {
                stringBuilder.append(obj.toString());

            }
            stringBuilder.append("]");
            return stringBuilder.toString();
        };
    }

    /**
     * redis缓存管理器配置
     * @param connectionFactory
     * @return
     */
    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
        Jackson2JsonRedisSerializer jacksonSeial = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(),ObjectMapper.DefaultTyping.NON_FINAL);
        jacksonSeial.setObjectMapper(om);

        //缓存参数配置
        RedisCacheConfiguration config = defaultCacheConfig()
//                .prefixKeysWith(cacheProperties.getRedis().getKeyPrefix())//这里可以配置key前缀
//                .computePrefixWith(cacheName -> "myprefix02::" + cacheName+"::")//带计算功能的key前缀
                .entryTtl(cacheProperties.getRedis().getTimeToLive())//缓存时间
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string())) //序列化key
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jacksonSeial)) //系列化value
                ;
        //缓存管理器
//        RedisCacheManager cm = RedisCacheManager.builder(connectionFactory)//创建没有多是manager
        RedisCacheManager cm = RedisCacheManager.builder(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory))//创建带有写锁的manager
                .cacheDefaults(config)
//                .withInitialCacheConfigurations(singletonMap("cache001", config))//差异化配置不同的cache
                .transactionAware()
                .build();
        return cm;
    }

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

}

