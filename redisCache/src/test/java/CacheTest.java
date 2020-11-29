import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.RedisApp;
import redis.RedisCacheService;
import redis.Student;

/**
 * @author liusha
 * @version 1.0
 * @date 2020/11/29 20:17
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisApp.class)
public class CacheTest {
    @Autowired
    RedisCacheService cacheService;

    @Test
    public void cacheAble(){
        Student student = new Student();
        student.setId(3);
        student.setName("xiaowang-1");
        student.setScore(90);
        Student res = cacheService.cacheAbleStudent(student);
        System.out.println(res);
        student.setName("xiaowang-2");
        res = cacheService.cacheAbleStudent(student);
        System.out.println(res);
    }

    @Test
    public void add(){
        Student student = new Student();
        student.setId(1);
        student.setName("xiaoming-1");
        student.setScore(90);
        Student res = cacheService.saveStudent(student);
        System.out.println(res);
        student.setName("xiaoming-2");
        student.setScore(90);
        res = cacheService.saveStudent(student);
        System.out.println(res);
    }

    @Test
    public void del(){
        Student student = new Student();
        student.setId(2);
        student.setName("xiaohong");
        student.setScore(90);
        cacheService.deleteStudent(student);
    }


    @Test
    public void setMulti(){
        Student student = new Student();
        student.setId(3);
        student.setName("xiaochen");
        student.setScore(90);
        cacheService.saveMulti(student);
    }
}
