package com.bsw.bswdb.DbDemoBean;

import java.util.UUID;

import com.bsw.dblibrary.db.DbClass;
import com.bsw.dblibrary.db.PrimaryKey;
import com.bsw.dblibrary.db.Require;

/**
 * 数据库封装
 */
@DbClass
public class Dog {
    @PrimaryKey
    private String uuid;

    @Require(name = "name")
    private String dogName = "lucky";

    @Require
    private int dogWeight = 55;

    public Dog(UUID uuid) {
        this.uuid = uuid.toString();
    }

    public Dog() {
    }

    @Override
    public String toString() {
        return "Dog{" +
                "uuid='" + uuid + '\'' +
                ", dogName='" + dogName + '\'' +
                ", dogWeight=" + dogWeight +
                '}';
    }
}
