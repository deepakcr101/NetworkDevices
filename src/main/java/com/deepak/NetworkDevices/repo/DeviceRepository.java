package com.deepak.NetworkDevices.repo;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DeviceRepository {
    private final Driver driver;

    public DeviceRepository(Driver driver) { this.driver = driver; }

    public String createDevice(Map<String,Object> params) {
        String cypher = """
      WITH randomUUID() AS did
      CREATE (d:Device {
        deviceId: did,
        deviceName: $deviceName,
        deviceType: $deviceType,
        partNumber: $partNumber,
        buildingName: $buildingName,
        numberOfShelfPositions: $numSP,
        isDeleted: false,
        createdAt: datetime(),
        updatedAt: datetime()
      })
      WITH d
      UNWIND range(1, $numSP) AS idx
      CREATE (p:ShelfPosition {
        shelfPositionId: randomUUID(),
        index: idx,
        deviceId: d.deviceId,
        shelfId: null,
        isOccupied: false,
        isDeleted: false,
        createdAt: datetime(),
        updatedAt: datetime()
      })
      CREATE (d)-[:HAS_POS]->(p)
      RETURN d.deviceId AS deviceId
      """;
        try (var session = driver.session(SessionConfig.forDatabase(params.getOrDefault("database", "neo4j").toString()))) {
            return session.executeWrite(tx -> tx.run(cypher, params).single().get("deviceId").asString());
        }
    }

    public Optional<Record> getDeviceSummary(String deviceId, String database) {
        String cypher = """
      MATCH (d:Device {deviceId:$deviceId}) WHERE d.isDeleted=false
      OPTIONAL MATCH (d)-[:HAS_POS]->(p:ShelfPosition) WHERE p.isDeleted=false
      OPTIONAL MATCH (p)-[:PLACED_ON]->(s:Shelf) WHERE s.isDeleted=false
      RETURN d,
        collect({
          shelfPositionId: p.shelfPositionId,
          index: p.index,
          isOccupied: p.isOccupied,
          shelf: CASE WHEN s IS NULL THEN null ELSE {shelfId:s.shelfId, shelfName:s.shelfName, partName:s.partName} END
        }) AS positions
      """;
        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeRead(tx -> {
                var res = tx.run(cypher, Map.of("deviceId", deviceId));
                return res.hasNext() ? Optional.of(res.single()) : Optional.empty();
            });
        }
    }

    public boolean updateDevice(String deviceId, Map<String,Object> updates, String database) {
        String cypher = """
      MATCH (d:Device {deviceId:$deviceId}) WHERE d.isDeleted=false
      SET d += $updates, d.updatedAt=datetime()
      RETURN d.deviceId AS id
      """;
        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeWrite(tx -> tx.run(cypher, Map.of("deviceId", deviceId, "updates", updates)).hasNext());
        }
    }

    public boolean softDeleteDevice(String deviceId, String database) {
        String cypher = """
      MATCH (d:Device {deviceId:$deviceId}) WHERE d.isDeleted=false
      SET d.isDeleted=true, d.updatedAt=datetime()
      WITH d
      OPTIONAL MATCH (d)-[:HAS_POS]->(p:ShelfPosition)
      SET p.isDeleted=true, p.isOccupied=false, p.shelfId=null, p.updatedAt=datetime()
      WITH d
      OPTIONAL MATCH (d)-[:HAS_POS]->(:ShelfPosition)-[r:PLACED_ON]->(s:Shelf)
      DELETE r
      SET s.updatedAt=datetime()
      RETURN d.deviceId AS deviceId
      """;
        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeWrite(tx -> tx.run(cypher, Map.of("deviceId", deviceId)).hasNext());
        }
    }

    public Optional<Record> allocate(String deviceId, String shelfPositionId, String shelfId, String database) {
        String cypher = """
      MATCH (d:Device {deviceId:$deviceId}) WHERE d.isDeleted=false
      MATCH (d)-[:HAS_POS]->(p:ShelfPosition {shelfPositionId:$spId})
        WHERE p.isDeleted=false AND p.isOccupied=false
      MATCH (s:Shelf {shelfId:$shelfId}) WHERE s.isDeleted=false
      AND NOT ( ()-[:PLACED_ON]->(s) )
      CREATE (p)-[:PLACED_ON]->(s)
      SET p.isOccupied=true, p.shelfId=s.shelfId, p.updatedAt=datetime(),
          d.updatedAt=datetime(), s.updatedAt=datetime()
      RETURN d.deviceId AS deviceId, p.shelfPositionId AS shelfPositionId, p.index AS index,
             s.shelfId AS shelfId, s.shelfName AS shelfName
      """;
        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeWrite(tx -> {
                var result = tx.run(cypher, Map.of("deviceId", deviceId, "spId", shelfPositionId, "shelfId", shelfId));
                return result.hasNext() ? Optional.of(result.single()) : Optional.empty();
            });
        }
    }

    public boolean free(String deviceId, String shelfPositionId, String database) {
        String cypher = """
      MATCH (d:Device {deviceId:$deviceId})-[:HAS_POS]->(p:ShelfPosition {shelfPositionId:$spId})
      MATCH (p)-[r:PLACED_ON]->(s:Shelf)
      DELETE r
      SET p.isOccupied=false, p.shelfId=null, p.updatedAt=datetime(),
          d.updatedAt=datetime(), s.updatedAt=datetime()
      RETURN d.deviceId AS deviceId
      """;
        try (var session = driver.session(SessionConfig.forDatabase(database))) {
            return session.executeWrite(tx -> tx.run(cypher, Map.of("deviceId", deviceId, "spId", shelfPositionId)).hasNext());
        }
    }
}