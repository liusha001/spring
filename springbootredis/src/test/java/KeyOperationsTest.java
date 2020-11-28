import io.lettuce.core.RedisException;
import org.demo.springbootRedis.RedisApp;
import org.demo.springbootRedis.Student;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author chenrf
 * @version 1.0
 * @date 2020/11/19 20:39
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisApp.class)
public class KeyOperationsTest {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void keyStringValue(){
        String key = "k1";
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set("k1", "v1");//存入redis的数据实际是: "v1"
        String value = (String) valueOperations.get(key);
        System.out.println(value); // 输出: v1
        value = stringRedisTemplate.opsForValue().get(key);
        System.out.println(value);// 输出: "v1"
    }
    @Test
    public void keyStringObject(){
        Student student = new Student(1, "xiaoming", 80);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(student.getName(), student);
        Student res = (Student)valueOperations.get(student.getName());
        System.out.println(res);
    }
    @Test
    public void keyHashValue(){
        String key = "class-3";
        Student student1 = new Student(1, "xiaoming", 80);
        Student student2 = new Student(1, "xiaohong", 85);
        Student student3 = new Student(1, "xiaowang", 90);
        HashOperations<String, String, Student> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(key, student1.getName(), student1);
        hashOperations.put(key, student2.getName(), student2);
        hashOperations.put(key, student3.getName(), student3);
        Student res = hashOperations.get(key, "xiaohong");
        System.out.println(res);
    }
    @Test
    public void keyListValue(){
        String key = "class-4";
        Student student1 = new Student(1, "xiaoming", 80);
        Student student2 = new Student(1, "xiaohong", 85);
        Student student3 = new Student(1, "xiaowang", 90);
        ListOperations<String, Student> listOperations = redisTemplate.opsForList();
        listOperations.leftPush(key, student1);
        listOperations.rightPush(key, student2);
        listOperations.leftPush(key, student3);
        // 列出所有的
        List<Student> studentList = listOperations.range(key, 0, -1);
        System.out.println(studentList);
        // 取出，会从redis中删除
        Student leftStudent1 =listOperations.leftPop(key);
        System.out.println(leftStudent1);
        Student leftStudent2 =listOperations.leftPop(key);
        System.out.println(leftStudent2);
    }

    @Test
    public void keySetValue(){
        String key = "class-5";
        Student student1 = new Student(1, "xiaoming", 80);
        Student student2 = new Student(1, "xiaohong", 85);
        Student student3 = new Student(1, "xiaowang", 90);
        SetOperations<String, Object> setOperations = redisTemplate.opsForSet();
        setOperations.add(key, student1);
        setOperations.add(key, student2);
        setOperations.add(key, student3);
        // 随机取出一个,并且从队列中删除
        Object pop1 = setOperations.pop(key);
        System.out.println(pop1);
        // 只是取出，不会删除
        Set<Object> res = setOperations.members(key);
        System.out.println(res);
    }
    @Test
    public void keyZSetValue(){
        String key = "class-6";
        Student student1 = new Student(1, "xiaoming", 85);
        Student student2 = new Student(1, "xiaohong", 80);
        Student student3 = new Student(1, "xiaowang", 90);
        ZSetOperations<String, Student> zsetOperations = redisTemplate.opsForZSet();
        //方法1
        zsetOperations.add(key, student1, student1.getScore() );
        System.out.println(zsetOperations.range(key, 0, -1));
        // 方法2
        Set<ZSetOperations.TypedTuple<Student>> typedTupleSet = new HashSet<>();
        ZSetOperations.TypedTuple<Student> typedTuple2 = new DefaultTypedTuple<>(student2, Double.valueOf(student2.getScore()));
        ZSetOperations.TypedTuple<Student> typedTuple3 = new DefaultTypedTuple<>(student3, Double.valueOf(student3.getScore()));
        typedTupleSet.add(typedTuple2);
        typedTupleSet.add(typedTuple3);
        zsetOperations.add(key, typedTupleSet);
        System.out.println(zsetOperations.range(key, 0, -1));

    }
}
