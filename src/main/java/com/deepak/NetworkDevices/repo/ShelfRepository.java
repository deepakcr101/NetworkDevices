package com.deepak.NetworkDevices.repo;


import org.neo4j.driver.Record;
import org.neo4j.driver.Driver;
import org.neo4j.driver.SessionConfig;

import org.springframework.stereotype.Repository;


import java.util.*;

@Repository
public class ShelfRepository {
    private final Driver driver;

    public ShelfRepository(Driver driver) { this.driver = driver; }

    public String createShelf(String shelfName, String partName, String database) {
        String cypher = """
      MERGE (s:Shelf {
        shelfId: randomUUID(),
        shelfName: $shelfName,
        partName: $partName,
        isDeleted: false,
        createdAt: datetime(),
        updatedAt: datetime()
      })
      RETURN s.shelfId AS shelfId
      """;
        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeWrite(tx -> tx.run(cypher, Map.of("shelfName", shelfName, "partName", partName))
                    .single().get("shelfId").asString());
        }
    }

    public Optional<Record> getShelf(String shelfId, String database) {
        String cypher = """
      MATCH (s:Shelf {shelfId:$shelfId}) WHERE s.isDeleted=false
      OPTIONAL MATCH (p:ShelfPosition)-[:HAS]->(s)
      OPTIONAL MATCH (d:Device)-[:HAS]->(p)
      RETURN s, d, p
      """;
        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeRead(tx -> {
                var r = tx.run(cypher, Map.of("shelfId", shelfId));
                return r.hasNext() ? Optional.of(r.single()) : Optional.empty();
            });
        }
    }

    public boolean updateShelf(String shelfId, Map<String,Object> updates, String database) {
        String cypher = """
      MATCH (s:Shelf {shelfId:$shelfId}) WHERE s.isDeleted=false
      SET s += $updates, s.updatedAt=datetime()
      RETURN s.shelfId AS id
      """;
        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeWrite(tx -> tx.run(cypher, Map.of("shelfId", shelfId, "updates", updates)).hasNext());
        }
    }

    public boolean softDeleteShelf(String shelfId, String database) {
        String cypher = """
      MATCH (s:Shelf {shelfId:$shelfId}) WHERE s.isDeleted=false
      SET s.isDeleted=true, s.updatedAt=datetime()
      WITH s
      OPTIONAL MATCH (p:ShelfPosition)-[r:HAS]->(s)
      DELETE r
      SET p.isOccupied=false, p.shelfId=null, p.updatedAt=datetime()
      RETURN s.shelfId AS shelfId
      """;
        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeWrite(tx -> tx.run(cypher, Map.of("shelfId", shelfId)).hasNext());
        }
    }

    public List<Record> listShelvesWithStatus(String database) {
        String cypher = """
        MATCH (s:Shelf)
       WHERE s.isDeleted = false

       OPTIONAL MATCH (p:ShelfPosition)-[:HAS]->(s)
       WHERE p.isDeleted = false

       OPTIONAL MATCH (d:Device)-[:HAS]->(p)
       WHERE d.isDeleted = false

       WITH s, p, d,
         CASE
           WHEN d IS NULL OR p IS NULL THEN null
           ELSE d.deviceName + " " + toString(p.index)
         END AS statusLabel
       ORDER BY s.shelfName ASC
      
      RETURN {
        shelfId: toString(s.shelfId),
        shelfName: toString(s.shelfName),
        partName: toString(s.partName),
        deviceId: CASE WHEN d IS NULL THEN null ELSE toString(d.deviceId) END,
        shelfPositionId: CASE WHEN p IS NULL THEN null ELSE toString(p.shelfPositionId) END,
        status: CASE WHEN statusLabel IS NULL THEN null ELSE toString(statusLabel) END,
        createdAt: null,
        updatedAt: null
      } AS shelfDto
      """;
        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeRead(tx -> tx.run(cypher).list());
        }
    }
}