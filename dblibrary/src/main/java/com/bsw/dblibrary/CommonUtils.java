package com.bsw.dblibrary;

import com.bsw.dblibrary.db.DbQuery;

import java.util.List;

/**
 * @author leiming
 * @date 2020-3-19
 */
public class CommonUtils {

    public static <T> int judgeListNull(List queryList) {
        return null == queryList ? 0 : queryList.size();
    }
}
