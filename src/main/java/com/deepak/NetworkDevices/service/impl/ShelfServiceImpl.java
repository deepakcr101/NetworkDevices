package com.deepak.NetworkDevices.service.impl;

import com.deepak.NetworkDevices.dto.request.*;
import com.deepak.NetworkDevices.dto.response.*;
import com.deepak.NetworkDevices.exception.NotFoundException;
import com.deepak.NetworkDevices.repo.ShelfRepository;
import com.deepak.NetworkDevices.service.ShelfService;
import org.neo4j.driver.Record;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShelfServiceImpl implements ShelfService {

    private final ShelfRepository repo;
    private final String database;

    public ShelfServiceImpl(ShelfRepository repo,
                            @Value("${neo4j.database:neo4j}") String database) {
        this.repo = repo; this.database = database;
    }

    @Override
    public String createShelf(CreateShelfRequest req) {
        return repo.createShelf(req.shelfName(), req.partName(), database);
    }

    @Override
    public ShelfDto getShelf(String shelfId) {
        var rec = repo.getShelf(shelfId, database)
                .orElseThrow(() -> new NotFoundException("Shelf not found: " + shelfId));
        var s = rec.get("s").asNode();
        return new ShelfDto(
                s.get("shelfId").asString(),
                s.get("shelfName").asString(),
                s.get("partName").asString(),
                s.get("isDeleted").asBoolean(),
                null, null
        );
    }

    @Override
    public void updateShelf(String shelfId, UpdateShelfRequest req) {
        Map<String,Object> updates = new HashMap<>();
        if (req.shelfName() != null) updates.put("shelfName", req.shelfName());
        if (req.partName() != null) updates.put("partName", req.partName());
        if (updates.isEmpty()) return;
        boolean ok = repo.updateShelf(shelfId, updates, database);
        if (!ok) throw new NotFoundException("Shelf not found or deleted: " + shelfId);
    }

    @Override
    public void softDeleteShelf(String shelfId) {
        boolean ok = repo.softDeleteShelf(shelfId, database);
        if (!ok) throw new NotFoundException("Shelf not found or already deleted: " + shelfId);
    }

    @Override
    public List<ShelfWithStatusDto> listShelvesWithStatus(int page, int size, boolean includeDeleted) {
        int skip = page * size;
        var rows = repo.listShelvesWithStatus(skip, size, includeDeleted, database);
        List<ShelfWithStatusDto> list = new ArrayList<>();
        for (Record r : rows) {
            var s = r.get("s").asNode();
            ShelfDto shelf = new ShelfDto(
                    s.get("shelfId").asString(),
                    s.get("shelfName").asString(),
                    s.get("partName").asString(),
                    s.get("isDeleted").asBoolean(),
                    null, null
            );
            list.add(new ShelfWithStatusDto(shelf, r.get("status").asString()));
        }
        return list;
    }
}