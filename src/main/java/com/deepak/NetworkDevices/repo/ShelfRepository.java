package com.deepak.NetworkDevices.repo;


import com.deepak.NetworkDevices.dto.response.DeviceDto;
import com.deepak.NetworkDevices.dto.response.ShelfDto;
import org.neo4j.driver.*;

import org.neo4j.driver.Record;
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
      OPTIONAL MATCH (p:ShelfPosition)-[r:HAS]->(s) WHERE r.isDeleted=false
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
      SET r.isDeleted=true,p.isOccupied=false, p.shelfId=null, p.updatedAt=datetime()
      RETURN s.shelfId AS shelfId
      """;
        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeWrite(tx -> tx.run(cypher, Map.of("shelfId", shelfId)).hasNext());
        }
    }

    public List<ShelfDto> listShelvesWithStatus(String database) {
        String cypher = """
        MATCH (s:Shelf)
       WHERE s.isDeleted = false

       OPTIONAL MATCH (p:ShelfPosition)-[r:HAS {isDeleted: False}]->(s)
       WHERE p.isDeleted = false

       OPTIONAL MATCH (d:Device)-[:HAS]->(p)
       WHERE d.isDeleted = false

       WITH s, p, d,
         CASE
           WHEN d IS NULL OR p IS NULL THEN "Available"
           ELSE d.deviceName + " SP" + toString(p.index)
         END AS statusLabel
       ORDER BY s.shelfName ASC
      
      RETURN collect({
        shelfId: toString(s.shelfId),
        shelfName: toString(s.shelfName),
        partName: toString(s.partName),
        deviceId: CASE WHEN d IS NULL THEN null ELSE toString(d.deviceId) END,
        shelfPositionId: CASE WHEN p IS NULL THEN null ELSE toString(p.shelfPositionId) END,
        status: statusLabel,
        createdAt: s.createdAt,
        updatedAt: s.updatedAt
      }) AS shelfList
      """;
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result rs = tx.run(cypher);
                // Since Cypher used 'collect(d)', there is exactly one record containing the list
                if (rs.hasNext()) {
                    Record record = rs.single();
                    return record.get("shelfList").asList(v -> {
                        // Access properties directly from the Value 'v' to handle types and nulls safely
                        return new ShelfDto(
                                v.get("shelfId").asString(null),
                                v.get("shelfName").asString(null),
                                v.get("partName").asString(null),
                                v.get("deviceId").asString(null),
                                v.get("shelfPositionId").asString(null),
                                v.get("status").asString(null), // Safely returns null if property is missing/null
                                v.get("createdAt").isNull() ? null : v.get("createdAt").asOffsetDateTime(),
                                v.get("updatedAt").isNull() ? null : v.get("updatedAt").asOffsetDateTime()
                        );
                    });
                }
                return Collections.emptyList();
            });
        }
    }

    public List<Record> listAvailableShelves(String database) {
        String cypher = """
        MATCH (s:Shelf)
                                  WHERE s.isDeleted = false
                                    AND NOT EXISTS {
                                      MATCH (:ShelfPosition)-[r:HAS]->(s)
                                      WHERE r.isDeleted = false
                                    }
                                  RETURN {
                                    shelfId: toString(s.shelfId),
                                    shelfName: toString(s.shelfName),
                                    partName: toString(s.partName)
                                  } AS shelfDto
  
     """;
        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeRead(tx -> tx.run(cypher).list());
        }
    }
}