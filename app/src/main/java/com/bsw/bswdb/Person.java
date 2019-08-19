package com.bsw.bswdb;

import com.bsw.dblibrary.db.DbClass;
import com.bsw.dblibrary.db.PrimaryKey;

@DbClass
class Person {
    private int age;
    @PrimaryKey
    private String name;
    private Boolean sex;

    public Person() {

    }

    public Person(String name, int age, Boolean sex) {
        this.age = age;
        this.name = name;
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "Person{" +
                "age=" + age +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                '}';
    }
}
