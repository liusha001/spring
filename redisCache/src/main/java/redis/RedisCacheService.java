package redis;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

/**
 * @author liusha
 * @version 1.0
 * @date 2020/11/29 20:20
 */
@Service
public class RedisCacheService {
    private static final String CacheName = "class-cache";

    /**
     * 查询缓存数据，有就用缓存数据，不更新redis
     * @param student
     * @return
     */
    @Cacheable(value = CacheName, key="'id-'+#student.id")
    public Student cacheAbleStudent(Student student) {
        // 这里可以操作数据库
        System.out.println("cacheAbleStudent doing");
        return student;
    }

    /**
     * 实现缓存与数据库的同步更新
     * cacheName: CacheName
     * key: id+student.getName()
     * value: student的json序列号EnableCaching
     * @param student
     * @return
     */
    //    @CachePut(value = CacheName, keyGenerator = "simpleKeyGenerator")
    @CachePut(value = CacheName, key = "'id-'+#student.id")
    public Student saveStudent(Student student) {
        // 这里可以操作数据库
        System.out.println("saveStudent doing");
        return student;
    }

    /**
     *  实现缓存与数据库的同步删除
     *  allEntries：默认false只删除匹配。true会删除所有
     *  beforeInvocation：在方法调用前还是调用后完成移除操作
     */
    @CacheEvict(value = CacheName, key="'id-'+#student.id")
    public Student deleteStudent(Student student) {
        // 这里可以操作数据库
        System.out.println("deleteStudent doing");
        return student;
    }

    /**
     * 保存多个key
     * @param student
     * @return
     */
    @Caching(put = {
                    @CachePut(value = CacheName, key = "#student.id"),
                    @CachePut(value = CacheName, key = "#student.name")
            })
     public Student saveMulti(Student student) {
        // 这里可以操作数据库
        System.out.println("saveMulti doing");
        return student;
    }

}

