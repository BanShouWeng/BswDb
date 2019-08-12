package com.bsw.dblibrary;

import com.bsw.dblibrary.db.PrimaryKey;
import com.bsw.dblibrary.dbFilterList.BswDbFilterList;
import com.bsw.dblibrary.dbFilterList.BswDbListQuery;

import org.junit.Test;

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
        BswDbFilterList<Person> p = new BswDbFilterList<>();
        p.insert(new Person("张三", 5))
                .insert(new Person("张四", 6))
                .insert(new Person("张五", 7))
                .insert(new Person("张六", 8))
                .insert(new Person("张七", 9))
                .insert(new Person("王五", 10))
                .insert(new Person("赵六", 11));
        System.out.println(p.query().putParams(BswDbListQuery.PARAM_TYPE_CONTAINS, "name", "张").getAll().toString());
        System.out.println(p.query().putParams(BswDbListQuery.PARAM_TYPE_CONTAINS, "name", "张").getList(1, 2).toString());

        System.out.println(p.query(new BswDbListQuery.ListFilterAdapter<Person>() {
            @Override
            public boolean filter(Person person) {
                return person.name.contains("五");
            }
        }).getAll().toString());

        System.out.println(p.query(new BswDbListQuery.ListFilterAdapter<Person>() {
            @Override
            public boolean filter(Person person) {
                return person.name.contains("五");
            }
        }).putParams(BswDbListQuery.PARAM_TYPE_RANGE_IN, "age", 5, 7).setQueryType(BswDbListQuery.OR).getAll().toString());

        System.out.println(p.query(new BswDbListQuery.ListFilterAdapter<Person>() {
            public boolean filter(Person person) {
                return person.name.contains("五");
            }
        }).putParams(BswDbListQuery.PARAM_TYPE_RANGE_IN, "age", 5, 7).setQueryType(BswDbListQuery.AND).getAll().toString());
    }

    @Test
    public void doubleTest() {
        Boolean a = null;

        System.out.println(!a);

//        change(a);
//        change(b);
//        change(c);
//        change(d);
//        change(e);
    }

    private Double change(Object o) {
        return Double.valueOf(o.toString());
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