package com.deepak.NetworkDevices.repo;

import com.deepak.NetworkDevices.dto.response.DeviceDto;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DeviceRepository {
    private final Driver driver;

    public DeviceRepository(Driver driver) { this.driver = driver; }

    public Value createDevice(Map<String,Object> params) {
        String cypher = """
      WITH randomUUID() AS dId
      MERGE (d:Device {
        deviceId: dId,
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
        isOccupied: false,
        isDeleted: false,
        createdAt: datetime(),
        updatedAt: datetime()
      })
      CREATE (d)-[:HAS]->(p)
      WITH d
      RETURN d.deviceId AS deviceId LIMIT 1
    """;
        try (Session session = driver.session()) {
            return session.executeWrite(tx ->{
                Result result=tx.run(cypher, params);
                Record record=result.single();
                return record.get("deviceId");
            });

        }
    }

    public List<DeviceDto> getDevices() {
        String cypher = """
        MATCH (d:Device)\s
        WHERE d.isDeleted = false
        RETURN collect(d) AS deviceList
       \s""";

        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result rs = tx.run(cypher);
                // Since Cypher used 'collect(d)', there is exactly one record containing the list
                if (rs.hasNext()) {
                    Record record = rs.single();
                    return record.get("deviceList").asList(v -> {
                        var node = v.asNode();
                        return new DeviceDto(
                                node.get("deviceId").asString(),
                                node.get("deviceName").asString(),
                                node.get("deviceType").asString(),
                                node.get("partNumber").asString(),
                                node.get("buildingName").asString(),
                                node.get("numberOfShelfPositions").asInt(),
                                // Check for null and provide a default or null
                                node.get("createdAt").isNull() ? null : node.get("createdAt").asOffsetDateTime(),
                                node.get("updatedAt").isNull() ? null : node.get("updatedAt").asOffsetDateTime()
                        );
                    });
                }
                return Collections.emptyList();
            });
        }
    }

    public Optional<Record> getDeviceSummary(String deviceId) {
        String cypher = """
                 MATCH (d:Device {deviceId: $deviceId})\s
                 WHERE d.isDeleted = false
    
                 OPTIONAL MATCH (d)-[:HAS]->(p:ShelfPosition)\s
                 WHERE p.isDeleted = false
    
                 OPTIONAL MATCH (p)-[r:HAS]->(s:Shelf)\s
                 WHERE  r.isDeleted = false AND s.isDeleted = false
    
                 // 1. Sort the rows by the desired property first
                 WITH d, p, s\s
                 ORDER BY p.index ASC
    
                 // 2. Now collect them into the final structure
                 RETURN d,
                   collect({
                     shelfPositionId: p.shelfPositionId,
                     index: p.index,
                     isOccupied: p.isOccupied,
                     shelf: CASE WHEN s IS NULL THEN null\s
                            ELSE {shelfId: s.shelfId, shelfName: s.shelfName, partName: s.partName} END
                   }) AS positions
      """;
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result res = tx.run(cypher, Map.of("deviceId", deviceId));
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
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(cypher, Map.of("deviceId", deviceId, "updates", updates));

                // This fully consumes the result if there is exactly one record
                // or throws an exception if there are none/multiple.
                if (result.hasNext()) {
                    result.single(); // Consumes the record
                    return true;
                }

                // Alternatively, use result.consume() to explicitly finish the stream
                result.consume();
                return false;
            });
        }
    }

    public boolean softDeleteDevice(String deviceId, String database) {
        String cypher = """
      MATCH (d:Device {deviceId:$deviceId}) WHERE d.isDeleted=false
      SET d.isDeleted=true, d.updatedAt=datetime()
      WITH d
      OPTIONAL MATCH (d)-[:HAS]->(p:ShelfPosition)
      SET p.isDeleted=true, p.isOccupied=false, p.shelfId=null, p.updatedAt=datetime()
      WITH d
      OPTIONAL MATCH (d)-[:HAS]->(:ShelfPosition)-[r:HAS]->(s:Shelf)
      SET s.updatedAt=datetime(),r.isDeleted = false
      RETURN d.deviceId AS deviceId
      """;
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> tx.run(cypher, Map.of("deviceId", deviceId)).hasNext());
        }
    }

    public Optional<Record> allocate(String deviceId, String shelfPositionId, String shelfId, String database) {
        String cypher = """
      MATCH (d:Device {deviceId:$deviceId}) WHERE d.isDeleted=false
      MATCH (d)-[:HAS]->(p:ShelfPosition {shelfPositionId:$spId})
        WHERE p.isDeleted=false AND p.isOccupied=false
      MATCH (s:Shelf {shelfId:$shelfId}) WHERE s.isDeleted=false
      AND NOT EXISTS {(s)<-[r:HAS]-(sp:ShelfPosition) WHERE r.isDeleted=false}
      CREATE (p)-[:HAS]->(s)
      SET p.isOccupied=true, p.shelfId=s.shelfId, p.updatedAt=datetime(),
          d.updatedAt=datetime(), s.updatedAt=datetime()
      RETURN d.deviceId AS deviceId, p.shelfPositionId AS shelfPositionId, p.index AS index,
             s.shelfId AS shelfId, s.shelfName AS shelfName
      """;
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Result result = tx.run(cypher, Map.of("deviceId", deviceId, "spId", shelfPositionId, "shelfId", shelfId));
                return result.hasNext() ? Optional.of(result.single()) : Optional.empty();
            });
        }
    }

    public boolean free(String deviceId, String shelfPositionId, String database) {
        String cypher = """
      MATCH (d:Device {deviceId:$deviceId})-[:HAS]->(p:ShelfPosition {shelfPositionId:$spId})
      MATCH (p)-[r:HAS]->(s:Shelf)
      SET r.isDeleted=true,p.isOccupied=false, p.shelfId=null, p.updatedAt=datetime(),
          d.updatedAt=datetime(), s.updatedAt=datetime()
      RETURN d.deviceId AS deviceId
      """;
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> tx.run(cypher, Map.of("deviceId", deviceId, "spId", shelfPositionId)).hasNext());
        }
    }
}