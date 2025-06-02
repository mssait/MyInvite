package com.hionstudios;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapResponse extends HashMap<String, Object> {
    static String STATUS = "status";
    static String FAILED = "failed";
    static String SUCCESS = "success";
    static String MESSAGE = "message";

    @Override
    public MapResponse put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public MapResponse(@SuppressWarnings("rawtypes") Map map) {
        super(map);
    }

    public MapResponse(JSONObject json) {
        this(json.toMap());
    }

    public MapResponse() {
        super();
    }

    public MapResponse(int initialCapacity) {
        super(initialCapacity);
    }

    public static MapResponse success() {
        return new MapResponse().put(STATUS, SUCCESS);
    }

    public static MapResponse success(String message) {
        return success().put(MESSAGE, message);
    }

    public static MapResponse success(String key, Object data) {
        return success().put(key, data);
    }

    public static MapResponse failure() {
        return new MapResponse().put(STATUS, FAILED);
    }

    public static MapResponse failure(String message) {
        return failure().put(MESSAGE, message);
    }

    public Boolean getBoolean(String key) {
        return (Boolean) get(key);
    }

    public BigInteger getBigInteger(String key) {
        return (BigInteger) get(key);
    }

    public BigDecimal getBigDecimal(String key) {
        return (BigDecimal) get(key);
    }

    public Double getDouble(String key) {
        Object value = get(key);
        return value instanceof BigDecimal ? ((BigDecimal) value).doubleValue() : (Double) value;
    }

    public Float getFloat(String key) {
        return (Float) get(key);
    }

    public Integer getInt(String key) {
        return (Integer) get(key);
    }

    public Long getLong(String key) {
        return (Long) get(key);
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public Byte[] getBytes(String key) {
        return (Byte[]) get(key);
    }

    public ListResponse getList(String key) {
        Object object = get(key);
        return object instanceof ListResponse ? (ListResponse) object : new ListResponse((List<?>) object);
    }

    public MapResponse getMap(String key) {
        Object object = get(key);
        return object == null ? (MapResponse) null
                : object instanceof MapResponse ? (MapResponse) object : new MapResponse((Map<?, ?>) object);
    }
}
