package com.bsw.bswdb;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.bsw.bswdb.DbDemoBean.Person;
import com.bsw.dblibrary.Logger;
import com.bsw.dblibrary.db.DbBase;
import com.bsw.dblibrary.db.DbQuery;
import com.bsw.dblibrary.db.DbUtils;
import com.bsw.dblibrary.dbFilterList.BswDbFilterList;
import com.bsw.dblibrary.dbFilterList.BswDbListQuery;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DbUtils dbUtils;

    private Logger logger = new Logger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbUtils = new DbUtils(getApplicationContext());

//        KBswFilterList<KotlinPerson> people = new KBswFilterList<>();
//        people.add(new KotlinPerson("john", 5, true));
//        people.add(new KotlinPerson("tony", 95, true));
//        people.add(new KotlinPerson("jerry", 20, false));
//        people.add(new KotlinPerson("lina", 26, false));
//
//        Log.i(getClass().getSimpleName(), people.query().sort("age", KBswListQuery.DESC).getAll().toString());
//        Log.i(getClass().getSimpleName(), people.query().putParams("age", 5).putParams("sex", false).setQueryType(KBswListQuery.OR).getAll().toString());
//        Log.i(getClass().getSimpleName(), people.query().putParams("age", 5).putParams("sex", false).setQueryType(KBswListQuery.OR).getFirst().toString());

        BswDbFilterList<KotlinPerson> people = new BswDbFilterList<>();
        people.add(new KotlinPerson("john", 5, true));
        people.add(new KotlinPerson("tony", 95, true));
        people.add(new KotlinPerson("jerry", 20, false));
        people.add(new KotlinPerson("lina", 26, false));

        Log.i(getClass().getSimpleName(), people.query().sort("age", BswDbListQuery.DESC).getAll().toString());
        Log.i(getClass().getSimpleName(), people.query().putParams("age", 5).putParams("sex", false).setQueryType(BswDbListQuery.OR).getAll().toString());
        Log.i(getClass().getSimpleName(), people.query().putParams("age", 5).putParams("sex", false).setQueryType(BswDbListQuery.OR).getFirst().toString());

    }

    /**
     * 插入/更新数据
     *
     * @param view 被点击控件
     */
    public void insert(View view) {
        dbUtils.executeTransaction(new DbUtils.OnTransaction() {
            @Override
            public void execute(DbUtils dbUtils) {
                Person person = new Person(Integer.valueOf(((EditText) findViewById(R.id.id)).getText().toString().trim())
                        , ((EditText) findViewById(R.id.name)).getText().toString().trim()
                        , Integer.valueOf(((EditText) findViewById(R.id.age)).getText().toString().trim()));
                logger.i((dbUtils.update(person) ? "创建成功:" : "修改成功") + person.toString());
//                dbUtils.update(new Dog(UUID.randomUUID()));
            }
        });
    }

    /**
     * 单数据获取
     *
     * @param view 被点击控件
     */
    public void get(View view) {
        dbUtils.executeTransaction(new DbUtils.OnTransaction() {
            @Override
            public void execute(DbUtils dbUtils) {
                Person person = dbUtils.where(Person.class).getFirst();
                if (null == person) {
                    logger.i(getClass().getSimpleName(), "person is empty");
                } else {
                    logger.i(getClass().getSimpleName(), person.toString());
                }
            }
        });
    }

    /**
     * 获取全部数据
     *
     * @param view 被点击控件
     */
    public void getAll(View view) {
        dbUtils.executeTransaction(new DbUtils.OnTransaction() {
            @Override
            public void execute(DbUtils dbUtils) {
                List<Person> persons = dbUtils.where(Person.class).getAll();
                if (null == persons || persons.size() == 0) {
                    logger.e("没有人");
                    return;
                }
                for (Person p : persons) {
                    logger.i(getClass().getSimpleName(), p.toString());
                }



//                List<Dog> dogs = dbUtils.where(Dog.class).getAll();
//                if (null == dogs || dogs.size() == 0) {
//                    logger.e("没有狗");
//                    return;
//                }
//                for (Dog dog : dogs) {
//                    logger.i(getClass().getSimpleName(), dog.toString());
//                }
            }
        });
    }

    /**
     * 根据条件搜索
     *
     * @param view 被点击控件
     */
    public void search(View view) {
        dbUtils.executeTransaction(new DbUtils.OnTransaction() {
            @Override
            public void execute(DbUtils dbUtils) {
                String searchName = ((EditText) findViewById(R.id.searchName)).toString().trim();
                String[] searchNames = searchName.split(",");
                DbQuery<Person> personQuery = dbUtils.where(Person.class);
                boolean isAnd = ((CheckBox) findViewById(R.id.isAnd)).isChecked();
                // 排序
                personQuery.sort("id", DbBase.DESC);
                // 与或
                personQuery.setQueryType(isAnd ? DbBase.AND : DbBase.OR);
                // 搜索参数
                for (String name : searchNames) {
                    personQuery.putParams("name", name);
                }
                personQuery.putParams("id", 1);
                List<Person> persons = personQuery.getAll();
                if (null == persons || persons.size() == 0) {
                    logger.e("没有人");
                    return;
                }
                for (Person p : persons) {
                    logger.i(getClass().getSimpleName(), p.toString());
                }
            }
        });
    }

    /**
     * 更新数据
     *
     * @param view 被点击控件
     */
    public void update(View view) {
        dbUtils.executeTransaction(new DbUtils.OnTransaction() {
            @Override
            public void execute(DbUtils dbUtils) {
                String searchName = ((EditText) findViewById(R.id.updateName)).toString().trim();
                Person person = dbUtils.where(Person.class).getFirst();
                person.setName(searchName);
                dbUtils.update(person);
            }
        });
    }

    /**
     * 删除
     *
     * @param view 被点击控件
     */
    public void delete(View view) {
        dbUtils.executeTransaction(new DbUtils.OnTransaction() {
            @Override
            public void execute(DbUtils dbUtils) {
                Person person = dbUtils.where(Person.class).getFirst();
                dbUtils.delete(person);
            }
        });
    }

    /**
     * 清空表
     *
     * @param view 被点击控件
     */
    public void clear(View view) {
        dbUtils.executeTransaction(new DbUtils.OnTransaction() {
            @Override
            public void execute(DbUtils dbUtils) {
                dbUtils.clear(Person.class);
            }
        });
    }

    class KotlinPerson {
        private int age;
        private String name;
        private Boolean sex;

        public KotlinPerson(String name, int age, Boolean sex) {
            this.age = age;
            this.name = name;
            this.sex = sex;
        }

        @Override
        public String toString() {
            return "KotlinPerson{" +
                    "age=" + age +
                    ", name='" + name + '\'' +
                    ", sex=" + sex +
                    '}';
        }
    }
}