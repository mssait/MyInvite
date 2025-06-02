package com.hionstudios;

import org.json.JSONArray;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ListResponse extends ArrayList<Object> {
    public ListResponse() {
    }

    public ListResponse(List<?> list) {
        super(list);
    }

    public ListResponse(JSONArray array) {
        this(array.toList());
    }

    public ListResponse(Object... objects) {
        this(Arrays.asList(objects));
    }

    public ListResponse put(Object obj) {
        this.add(obj);
        return this;
    }

    public Boolean getBoolean(int index) {
        return (Boolean) get(index);
    }

    public BigInteger getBigInteger(int index) {
        return (BigInteger) get(index);
    }

    public BigDecimal getBigDecimal(int index) {
        return (BigDecimal) get(index);
    }

    public Double getDouble(int index) {
        return (Double) get(index);
    }

    public Float getFloat(int index) {
        return (Float) get(index);
    }

    public Integer getInt(int index) {
        return (Integer) get(index);
    }

    public Long getLong(int index) {
        return (Long) get(index);
    }

    public String getString(int index) {
        return (String) get(index);
    }

    public ListResponse getList(int index) {
        return new ListResponse((List<?>) get(index));
    }

    public MapResponse getMap(int index) {
        return new MapResponse((Map<?, ?>) get(index));
    }
}
