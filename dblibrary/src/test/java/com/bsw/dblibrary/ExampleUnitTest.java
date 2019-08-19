package com.bsw.dblibrary;

import android.content.Context;

import com.bsw.dblibrary.db.PrimaryKey;
import com.bsw.dblibrary.dbFilterList.BswDbFilterList;
import com.bsw.dblibrary.dbFilterList.BswDbListQuery;
import com.bsw.dblibrary.dbFilterList.BswDbListUpdate;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test() {
        final BswDbFilterList<Person> p = new BswDbFilterList<>();
        p.insert(new Person("张三", 5))
                .insert(new Person("张四", 6))
                .insert(new Person("张五", 7))
                .insert(new Person("张六", 8))
                .insert(new Person("张七", 9))
                .insert(new Person("王五", 10))
                .insert(new Person("赵六", 11));
//        p.synchronizedDb(null, Person.class);
        System.out.println("H1  " + p.has(new Person("张三", 99)));
        System.out.println("H2  " + p.has(new Person("张鱼", 99)));

        System.out.println("Q1  " + p.query().putParams(BswDbListQuery.PARAM_TYPE_CONTAINS, "name", "张").getAll().toString());

        System.out.println("Q2  " + p.query().putParams(BswDbListQuery.PARAM_TYPE_CONTAINS, "name", "张").getList(1, 2).toString());

        System.out.println("Q3  " + p.query(new BswDbListQuery.ListFilterAdapter<Person>() {
            @Override
            public boolean filter(Person person) {
                return person.name.contains("五");
            }
        }).getAll().toString());

        System.out.println("Q4  " + p.query(new BswDbListQuery.ListFilterAdapter<Person>() {
            @Override
            public boolean filter(Person person) {
                return person.name.contains("五");
            }
        }).putParams(BswDbListQuery.PARAM_TYPE_RANGE_IN, "age", 5, 7).setQueryType(BswDbListQuery.OR).getAll().toString());

        System.out.println("Q5  " + p.query(new BswDbListQuery.ListFilterAdapter<Person>() {
            public boolean filter(Person person) {
                return person.name.contains("五");
            }
        }).putParams(BswDbListQuery.PARAM_TYPE_RANGE_IN, "age", 5, 7).setQueryType(BswDbListQuery.AND).getAll().toString());

        System.out.println("U1  " + p.update(new Person("张三", 999999)));

        System.out.println("U2  " + p.update(new Person("李三", 999999), false));

        System.out.println("U3  " + p.update().putParams("name", "张七", "李七").run());

        System.out.println("U4  " + p.update(new BswDbListUpdate.ListUpdateAdapter<Person>() {
            @Override
            public Person update(Person item) {
                item.age += 10;
                return item;
            }
        }));
    }

    @Test
    public void equlsJudge() {
        Map<Person, String> personStringMap = new HashMap<>();
        Person person = new Person("张三", 55);
        personStringMap.put(person, "sfasdfsdfsdafsdafsd");
        person.name = "fsfsf";

        System.out.println(person.toString());
        System.out.println(personStringMap.get(person));
    }

    class Person {
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @PrimaryKey
        String name;
        int age;

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}