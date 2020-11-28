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
import java.util.Map;
import java.util.Set;

/**
 * @author chenrf
 * @version 1.0
 * @date 2020/11/21 13:15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisApp.class)
public class BoundKeyOperationsTest {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void keyStringObject(){
        String key = "class-1";
        Student student1 = new Student(1, "xiaoming", 80);
        Student student2 = new Student(2, "xiaohong", 85);
        Student student3 = new Student(3, "xiaowang", 90);
        BoundValueOperations<String, Student> boundValueOperations = redisTemplate.boundValueOps(key);
        boundValueOperations.set(student1);
        Student res = boundValueOperations.get();
        System.out.println(res);
        boundValueOperations.set(student2);
        res = boundValueOperations.get();
        System.out.println(res);

    }

    @Test
    public void keyStringHashValue() {
        String key = "class-2";
        Student student1 = new Student(1, "xiaoming", 80);
        Student student2 = new Student(2, "xiaohong", 85);
        Student student3 = new Student(3, "xiaowang", 90);
        BoundHashOperations<String, String, Student> boundHashOperations =  redisTemplate.boundHashOps(key);
        boundHashOperations.put(student1.getName(), student1);
        boundHashOperations.put(student2.getName(), student2);
        boundHashOperations.put(student3.getName(), student3);
        Map<String, Student> maps = boundHashOperations.entries();
        System.out.println(maps);
    }
    @Test
    public void keyStringListValue() {
        String key = "class-3";
        Student student1 = new Student(1, "xiaoming", 80);
        Student student2 = new Student(2, "xiaohong", 85);
        Student student3 = new Student(3, "xiaowang", 90);
        BoundListOperations<String, Student>  boundListOperations = redisTemplate.boundListOps(key);
        boundListOperations.leftPush(student1);
        boundListOperations.rightPush(student3);
        boundListOperations.rightPush(student2);
        List<Student> lists = boundListOperations.range(0, -1);
        System.out.println(lists);
    }
    @Test
    public void keyStringSetValue(){
        String key = "class-4";
        Student student1 = new Student(1, "xiaoming", 80);
        Student student2 = new Student(2, "xiaohong", 85);
        Student student3 = new Student(3, "xiaowang", 90);
        BoundSetOperations<String, Student> boundSetOps = redisTemplate.boundSetOps(key);
        boundSetOps.add(student2);
        boundSetOps.add(student1, student2, student3);
        Set<Student> studentSet = boundSetOps.members();
        System.out.println(studentSet);
    }
    @Test
    public void keyStringZSetValue(){
        String key = "class-5";
        Student student1 = new Student(1, "xiaoming", 80);
        Student student2 = new Student(2, "xiaohong", 85);
        Student student3 = new Student(3, "xiaowang", 90);
        BoundZSetOperations<String, Student>  boundZSetOperations = redisTemplate.boundZSetOps(key);
        //方法1
        boundZSetOperations.add(student1, student1.getScore() );
        System.out.println(boundZSetOperations.range(0, -1));
        // 方法2
        Set<ZSetOperations.TypedTuple<Student>> typedTupleSet = new HashSet<>();
        ZSetOperations.TypedTuple<Student> typedTuple2 = new DefaultTypedTuple<>(student2, Double.valueOf(student2.getScore()));
        ZSetOperations.TypedTuple<Student> typedTuple3 = new DefaultTypedTuple<>(student3, Double.valueOf(student3.getScore()));
        typedTupleSet.add(typedTuple2);
        typedTupleSet.add(typedTuple3);
        boundZSetOperations.add(typedTupleSet);
        System.out.println(boundZSetOperations.range(0, -1));
    }
}
