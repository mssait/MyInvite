package com.hionstudios.db;

public interface DbConnection {

    default void read() {
        try {
            DbUtil.open();
            method();
        } finally {
            DbUtil.close();
        }
    }

    void method();
}
