package org.demo.springbootRedis;

/**
 * @author chenrf
 * @version 1.0
 * @date 2020/11/19 20:37
 */
public class Student {
    private int id;
    private String name;
    private int score;
    public Student(){
        //缺少默认构造，序列化的时候会报错
    }
    public Student(int id, String name, int score){
        this.id = id;
        this.name = name;
        this.score = score;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", score=" + score +
                '}';
    }
}
