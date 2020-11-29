import org.junit.Test;
import org.junit.runner.RunWith;
import org.liusha.redis.RedisApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenrf
 * @version 1.0
 * @date 2020/11/25 23:17
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisApp.class)
public class ExecuteLua {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void checkAndSet() {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Boolean.class);//类型需要是 Long, Boolean, List, or a deserialized value type
        Resource resource = new ClassPathResource("checkandset.lua");
//        System.out.println(resource.exists());
        redisScript.setScriptSource(new ResourceScriptSource(resource));
        List<String> keyList = new ArrayList();
        keyList.add("k1");
        List<String> valueList = new ArrayList();
        valueList.add("v1");
        valueList.add("v1");
        // 使用redisTemplate，value使用json序列化所以运行的时候会多一个引号
        // 使用stringRedisTemplate，value需要的字符串，所有不可以使用数组代替
        // lua脚本的调试可以通过 把值打印到redis的测试key上来调试程序
        Object res = stringRedisTemplate.execute(redisScript, keyList, "v1", "v2");//这个是可以的
        //下面这个写法或报错，因为args是可变参数，改成数组就变成一个参数，string序列化的时候序列化不了
//        Object res = stringRedisTemplate.execute(redisScript, keyList, Arrays.asList(valueList));
        System.out.println(res);
    }


}
