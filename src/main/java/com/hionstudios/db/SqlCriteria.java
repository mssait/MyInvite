package com.hionstudios.db;

import java.util.Arrays;
import java.util.List;

public class SqlCriteria {
    private String criteria;
    private List<Object> params;

    public SqlCriteria(String criteria, Object... params) {
        this(criteria, Arrays.asList(params));
    }

    public SqlCriteria(String criteria, List<Object> params) {
        this.criteria = criteria;
        this.params = params;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }
}
