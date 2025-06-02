package com.hionstudios.app.controller;

import javax.annotation.security.PermitAll;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hionstudios.CachedSelect;
import com.hionstudios.MapResponse;
import com.hionstudios.db.DbTransaction;

@RestController
@RequestMapping("api")
public class ApiController {
        @GetMapping("select/{select}")
    @PermitAll
    public ResponseEntity<MapResponse> select(@PathVariable String select) {
        return ((DbTransaction) () -> new CachedSelect().select(select)).read();
    }
}
