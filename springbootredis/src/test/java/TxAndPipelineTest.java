import org.demo.springbootRedis.RedisApp;
import org.demo.springbootRedis.Student;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 事务和非实务流水测试
 * @author chenrf
 * @version 1.0
 * @date 2020/11/23 0:04
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisApp.class)
public class TxAndPipelineTest {
    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void txTest(){
        String key = "class-3";
        Student student1 = new Student(1, "xiaoming", 80);
        Student student2 = new Student(1, "xiaohong", 85);
        Student student3 = new Student(1, "xiaowang", 90);
        HashOperations<String, String, Student> hashOperations = redisTemplate.opsForHash();
        //开启
        redisTemplate.watch(key);
        redisTemplate.multi();
        hashOperations.put(key, student1.getName(), student1);
        hashOperations.put(key, student2.getName(), student2);
        hashOperations.put(key, student3.getName(), student3);

        List<Object> result = redisTemplate.exec();
        if(result.isEmpty()){
            System.out.println("error: redis watch error");
        }else{
            System.out.println("success: " + result);
        }
        Student res = hashOperations.get(key, "xiaohong");
        System.out.println(res);
    }
    @Test
    public void pipeline(){
        String key = "class-3";
        Student student1 = new Student(1, "xiaoming", 80);
        Student student2 = new Student(1, "xiaohong", 85);
        Student student3 = new Student(1, "xiaowang", 90);
//        RedisCallback redisCallback = new RedisCallback<Student>() {
//            public Student doInRedis(RedisConnection connection) throws DataAccessException {
//                //使用这个执行一下客户端命令
//                //connection.hashCommands().
//                return null;
//            }
//        };
        SessionCallback sessionCallback = new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                HashOperations<String, String, Student> hashOperations = operations.opsForHash();
                hashOperations.put(key, student1.getName(), student1);
                hashOperations.put(key, student2.getName(), student2);
                hashOperations.put(key, student3.getName(), student3);
                return null;
            }
        };
        List<Object> result = redisTemplate.executePipelined(sessionCallback);
        System.out.println(result);
    }
}
