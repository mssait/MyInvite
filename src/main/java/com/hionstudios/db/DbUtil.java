package com.hionstudios.db;

import java.sql.SQLException;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.connection_config.DBConfiguration;

public class DbUtil {
    static {
        DBConfiguration.loadConfiguration("/database.properties");
    }

    public static void openTransaction() throws SQLException {
        open();
        Base.connection().setAutoCommit(false);
        Base.openTransaction();
    }

    public static void open() {
        if (!Base.hasConnection()) {
            Base.open();
        }
    }

    public static void commitTransaction() {
        if (Base.hasConnection()) {
            Base.commitTransaction();
        }
    }

    public static void close() {
        if (Base.hasConnection()) {
            Base.close();
        }
    }

    public static void rollback() {
        if (Base.hasConnection()) {
            Base.rollbackTransaction();
        }
    }
}