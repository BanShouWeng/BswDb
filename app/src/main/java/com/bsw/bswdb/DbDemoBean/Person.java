package com.bsw.bswdb.DbDemoBean;

import com.bsw.dblibrary.db.DbClass;
import com.bsw.dblibrary.db.Ignore;
import com.bsw.dblibrary.db.PrimaryKey;
import com.bsw.dblibrary.db.Require;

/**
 * 必须有一个空构造方法
 *
 * @author bsw
 * @date 2019/1/2.
 */
@DbClass
public class Person {
    @PrimaryKey
    private int id;
    @Require
    private String name;
    @Require
    private int age;
    @Require
    private boolean sex = true;
    @Require
    private long time = System.currentTimeMillis();
    @Require
    private float pay = 15.5f;
    @Require
    private double earn = 77.745;
    @Require
    private byte type = 3;

    @Ignore
    private String ignore = "ignore";

    private String defaultString = "defaultString";

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    /**
     * 必须有一个空构造方法
     */
    public Person() {

    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", sex=" + sex +
                ", time=" + time +
                ", pay=" + pay +
                ", earn=" + earn +
                ", type=" + type +
                '}';
    }
}
