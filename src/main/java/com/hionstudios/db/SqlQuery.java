package com.hionstudios.db;

public class SqlQuery {
    private String query;
    private SqlCriteria criteria;

    public SqlQuery(String query) {
        this.query = query;
    }

    public SqlQuery(String query, SqlCriteria criteria) {
        this.query = query;
        this.criteria = criteria;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public SqlCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(SqlCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public String toString() {
        return query
                + (criteria != null && !criteria.getCriteria().trim().toLowerCase().startsWith("where") ? " Where " : "")
                + (criteria != null && criteria.getCriteria() != null ? " " + criteria.getCriteria() : "");
    }
}
