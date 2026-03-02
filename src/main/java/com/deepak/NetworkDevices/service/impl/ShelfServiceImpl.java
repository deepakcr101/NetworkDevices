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
                s.get("deviceId").asString(),
                s.get("shelfPositionId").asString(),
                s.get("status").asString(),
                null,null
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
    public List<ShelfDto> listShelvesWithStatus() {
        List<Record> rows = repo.listShelvesWithStatus(database);
        List<ShelfDto> list = new ArrayList<>();

        for (Record r : rows) {
            //System.out.println("RAW: " + r.get("shelfDto"));
            // Get the projected map as a Neo4j Value
            org.neo4j.driver.Value v = r.get("shelfDto");

            // Convert the Neo4j MapValue into a plain Java Map<String, String>
            Map<String, String> m = v.asMap(val -> val.isNull() ? null : val.asString());

            ShelfDto shelf = new ShelfDto(
                    m.get("shelfId"),
                    m.get("shelfName"),
                    m.get("partName"),
                    m.get("deviceId"),
                    m.get("shelfPositionId"),
                    m.get("status"),
                    null, // createdAt
                    null  // updatedAt
            );

            list.add(shelf);
        }
        return list;
    }

    @Override
    public List<ShelfLiteDto> listAvailableShelves() {
        List<Record> rows = repo.listAvailableShelves(database);
        List<ShelfLiteDto> list = new ArrayList<>();

        for (Record r : rows) {
            //System.out.println("RAW: " + r.get("shelfDto"));
            // Get the projected map as a Neo4j Value
            org.neo4j.driver.Value v = r.get("shelfDto");

            // Convert the Neo4j MapValue into a plain Java Map<String, String>
            Map<String, String> m = v.asMap(val -> val.isNull() ? null : val.asString());

            ShelfLiteDto shelf = new ShelfLiteDto(
                    m.get("shelfId"),
                    m.get("shelfName"),
                    m.get("partName")
            );

            list.add(shelf);
        }
        return list;
    }
}