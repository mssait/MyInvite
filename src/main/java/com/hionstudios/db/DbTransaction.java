package com.hionstudios.db;

import org.springframework.http.ResponseEntity;

import com.hionstudios.MapResponse;

public interface DbTransaction {

    default ResponseEntity<MapResponse> read() {
        try {
            DbUtil.open();
            return ResponseEntity.ok(method());
        } finally {
            DbUtil.close();
        }
    }

    default ResponseEntity<MapResponse> write() {
        MapResponse o;
        try {
            DbUtil.openTransaction();
            o = method();
            if ("failed".equals(o.getString("status"))) {
                String m = o.getString("message");
                throw new Exception(m != null ? m : "");
            }
            DbUtil.commitTransaction();
        } catch (Exception e) {
            DbUtil.rollback();
            e.printStackTrace();
            o = MapResponse.failure(e.getMessage());
        } finally {
            DbUtil.close();
        }
        return ResponseEntity.ok(o);
    }

    MapResponse method();
}
