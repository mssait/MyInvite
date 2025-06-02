package com.hionstudios.db;

public interface DbConnectionWrite {

    default void write() {
        try {
            DbUtil.openTransaction();
            method();
            DbUtil.commitTransaction();
        } catch (Exception e) {
            DbUtil.rollback();
            e.printStackTrace();
        } finally {
            DbUtil.close();
        }
    }

    void method();
}
