package com.bsw.bswdb;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

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

//        KBswFilterList<Person> people = new KBswFilterList<>();
//        people.add(new Person("john", 5, true));
//        people.add(new Person("tony", 95, true));
//        people.add(new Person("jerry", 20, false));
//        people.add(new Person("lina", 26, false));
//
//        Log.i(getClass().getSimpleName(), people.query().sort("age", KBswListQuery.DESC).getAll().toString());
//        Log.i(getClass().getSimpleName(), people.query().putParams("age", 5).putParams("sex", false).setQueryType(KBswListQuery.OR).getAll().toString());
//        Log.i(getClass().getSimpleName(), people.query().putParams("age", 5).putParams("sex", false).setQueryType(KBswListQuery.OR).getFirst().toString());

        BswDbFilterList<Person> people = new BswDbFilterList<>();
        people.add(new Person("john", 5, true));
        people.add(new Person("tony", 95, true));
        people.add(new Person("jerry", 20, false));
        people.add(new Person("lina", 26, false));
//        people.synchronizedDb(this,Person.class);

        Log.i(getClass().getSimpleName(), people.query().sort("age", BswDbListQuery.DESC).getAll().toString());
        Log.i(getClass().getSimpleName(), people.query().putParams("age", 5).putParams("sex", false).setQueryType(BswDbListQuery.OR).getAll().toString());
        Log.i(getClass().getSimpleName(), people.query().putParams("age", 5).putParams("sex", false).setQueryType(BswDbListQuery.OR).getFirst().toString());

        people.update().putParams("name", "jerry", "999").run().synchronizedDb(this, Person.class);
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
                com.bsw.bswdb.DbDemoBean.Person person = new com.bsw.bswdb.DbDemoBean.Person(Integer.valueOf(((EditText) findViewById(R.id.id)).getText().toString().trim())
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
                com.bsw.bswdb.DbDemoBean.Person person = dbUtils.where(com.bsw.bswdb.DbDemoBean.Person.class).getFirst();
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
                List<com.bsw.bswdb.DbDemoBean.Person> persons = dbUtils.where(com.bsw.bswdb.DbDemoBean.Person.class).getAll();
                if (null == persons || persons.size() == 0) {
                    logger.e("没有人");
                    return;
                }
                for (com.bsw.bswdb.DbDemoBean.Person p : persons) {
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
                String searchName = findViewById(R.id.searchName).toString().trim();
                String[] searchNames = searchName.split(",");
                DbQuery<com.bsw.bswdb.DbDemoBean.Person> personQuery = dbUtils.where(com.bsw.bswdb.DbDemoBean.Person.class);
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
                List<com.bsw.bswdb.DbDemoBean.Person> persons = personQuery.getAll();
                if (null == persons || persons.size() == 0) {
                    logger.e("没有人");
                    return;
                }
                for (com.bsw.bswdb.DbDemoBean.Person p : persons) {
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
                String searchName = findViewById(R.id.updateName).toString().trim();
                com.bsw.bswdb.DbDemoBean.Person person = dbUtils.where(com.bsw.bswdb.DbDemoBean.Person.class).getFirst();
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
                com.bsw.bswdb.DbDemoBean.Person person = dbUtils.where(com.bsw.bswdb.DbDemoBean.Person.class).getFirst();
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
                dbUtils.clear(com.bsw.bswdb.DbDemoBean.Person.class);
            }
        });
    }
}