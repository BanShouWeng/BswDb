package db.bsw.bswdb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.List;
import java.util.UUID;

import db.bsw.bswdb.DbDemoBean.Dog;
import db.bsw.bswdb.DbDemoBean.Person;
import db.bsw.dblibrary.db.DbBase;
import db.bsw.dblibrary.db.DbQuery;
import db.bsw.dblibrary.db.DbUtils;

public class MainActivity extends AppCompatActivity {

    private DbUtils dbUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbUtils = new DbUtils(getApplicationContext());
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
                dbUtils.update(new Person(Integer.valueOf(((EditText) findViewById(R.id.id)).getText().toString().trim())
                        , ((EditText) findViewById(R.id.name)).getText().toString().trim()
                        , Integer.valueOf(((EditText) findViewById(R.id.age)).getText().toString().trim())));
                dbUtils.update(new Dog(UUID.randomUUID()));
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
                    Log.i(getClass().getSimpleName(), "person is empty");
                } else {
                    Log.i(getClass().getSimpleName(), person.toString());
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
                for (Person p : persons) {
                    Log.i(getClass().getSimpleName(), p.toString());
                }

                List<Dog> dogs = dbUtils.where(Dog.class).getAll();
                for (Dog dog : dogs) {
                    Log.i(getClass().getSimpleName(), dog.toString());
                }
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
                for (Person p : persons) {
                    Log.i(getClass().getSimpleName(), p.toString());
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
}
