package com.deepak.NetworkDevices.repo;

import com.deepak.NetworkDevices.dto.DeviceDTOs.*;
import com.deepak.NetworkDevices.exception.NotFoundException;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

@Repository
public class DeviceRepository {

    private final Driver driver;

    public DeviceRepository(Driver driver) {
        this.driver = driver;
    }

    public String createDeviceWithPositions(
            String deviceId,
            CreateDeviceRequest req,
            List<Map<String, Object>> positions,
            long ts
    ) {
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                var res = tx.run("""
            CREATE (d:Device {
              deviceId: $deviceId,
              deviceName: $deviceName,
              partNumber: $partNumber,
              buildingName: $buildingName,
              deviceType: $deviceType,
              numberOfShelfPositions: $n,
              createdAt: $ts,
              updatedAt: $ts,
              deletedAt: NULL
            })
            WITH d, $positions AS positions, $ts AS ts
               FOREACH (pos IN $positions |
               CREATE (sp:ShelfPosition {
                 shelfPositionId: pos.id,
                 deviceId: d.deviceId,
                 index: pos.idx,
                 createdAt: $ts,
                 updatedAt: $ts,
                 deletedAt: NULL
               })
               CREATE (d)-[:HAS]->(sp)
             )
             RETURN d.deviceId AS deviceId
            """,
                        parameters(
                                "deviceId", deviceId,
                                "deviceName", req.deviceName(),
                                "partNumber", req.partNumber(),
                                "buildingName", req.buildingName(),
                                "deviceType", req.deviceType(),
                                "n", req.numberOfShelfPositions(),
                                "positions", positions,
                                "ts", ts
                        )
                );
                var rec = res.single();
                return rec.get("deviceId").asString();
            });
        }
    }

    public List<DeviceResponse> listDevices(boolean includeDeleted) {
        String cypher = """
      MATCH (d:Device)
      WHERE $includeDeleted = true OR d.deletedAt IS NULL
      RETURN d ORDER BY d.deviceName ASC
      """;
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                var result = tx.run(cypher, parameters("includeDeleted", includeDeleted));
                List<DeviceResponse> list = new ArrayList<>();
                while (result.hasNext()) {
                    var r = result.next();
                    var d = r.get("d").asNode();
                    list.add(mapDevice(d));
                }
                return list;
            });
        }
    }

    public DeviceSummaryResponse getDeviceSummary(String deviceId) {
        String cypher = """
      MATCH (d:Device {deviceId: $deviceId})
      WHERE d.deletedAt IS NULL
      OPTIONAL MATCH (d)-[:HAS]->(sp:ShelfPosition)
      WHERE sp.deletedAt IS NULL
      OPTIONAL MATCH (sp)-[:HAS]->(s:Shelf)
      WHERE s.deletedAt IS NULL
      RETURN d, sp, s
      ORDER BY sp.index ASC
      """;
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                var result = tx.run(cypher, parameters("deviceId", deviceId));
                DeviceResponse device = null;
                Map<String, ShelfPositionSummary> posMap = new LinkedHashMap<>();
                while (result.hasNext()) {
                    var rec = result.next();
                    if (device == null) {
                        var dNode = rec.get("d").asNode();
                        if (dNode == null) throw new NotFoundException("Device not found");
                        device = mapDevice(dNode);
                    }
                    if (!rec.get("sp").isNull()) {
                        var sp = rec.get("sp").asNode();
                        String spId = sp.get("shelfPositionId").asString();
                        int idx = (int) sp.get("index").asLong();
                        boolean occupied = !rec.get("s").isNull();
                        String shelfId = occupied ? rec.get("s").asNode().get("shelfId").asString() : null;
                        String shelfName = occupied ? rec.get("s").asNode().get("shelfName").asString(null) : null;

                        posMap.put(spId, new ShelfPositionSummary(spId, idx, occupied, shelfId, shelfName));
                    }
                }
                if (device == null) throw new NotFoundException("Device not found");
                var positions = posMap.values().stream().sorted(Comparator.comparingInt(ShelfPositionSummary::index)).collect(Collectors.toList());
                return new DeviceSummaryResponse(device, positions);
            });
        }
    }

    public DeviceResponse updateDevice(String deviceId, Map<String, Object> updates, long ts) {
        // Only allow updating these fields
        var allowed = Set.of("deviceName", "partNumber", "buildingName", "deviceType");
        Map<String, Object> filtered = new HashMap<>();
        updates.forEach((k, v) -> { if (allowed.contains(k) && v != null) filtered.put(k, v); });

        if (filtered.isEmpty()) return getDeviceById(deviceId); // nothing to change

        StringBuilder setClause = new StringBuilder("SET ");
        int i = 0;
        for (String key : filtered.keySet()) {
            if (i++ > 0) setClause.append(", ");
            setClause.append("d.").append(key).append(" = $").append(key);
        }
        setClause.append(", d.updatedAt = $ts");

        String cypher = String.format("""
      MATCH (d:Device {deviceId: $deviceId}) WHERE d.deletedAt IS NULL
      %s
      RETURN d
      """, setClause);

        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                var params = new HashMap<String, Object>(filtered);
                params.put("deviceId", deviceId);
                params.put("ts", ts);
                var res = tx.run(cypher, params);
                if (!res.hasNext()) throw new NotFoundException("Device not found or deleted");
                return mapDevice(res.single().get("d").asNode());
            });
        }
    }

    public DeviceResponse getDeviceById(String deviceId) {
        String cypher = """
      MATCH (d:Device {deviceId: $deviceId})
      WHERE d.deletedAt IS NULL
      RETURN d
      """;
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                var res = tx.run(cypher, parameters("deviceId", deviceId));
                if (!res.hasNext()) throw new NotFoundException("Device not found");
                return mapDevice(res.single().get("d").asNode());
            });
        }
    }

    public void softDeleteDeviceCascade(String deviceId, long ts) {
        String cypher = """
      MATCH (d:Device {deviceId: $deviceId}) WHERE d.deletedAt IS NULL
      OPTIONAL MATCH (d)-[:HAS]->(sp:ShelfPosition) WHERE sp.deletedAt IS NULL
      OPTIONAL MATCH (sp)-[r:HAS]->(s:Shelf) WHERE s.deletedAt IS NULL
      DELETE r
      SET d.deletedAt = $ts, d.updatedAt = $ts
      SET sp.deletedAt = $ts, sp.updatedAt = $ts
      RETURN d
      """;
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                var res = tx.run(cypher, parameters("deviceId", deviceId, "ts", ts));
                if (!res.hasNext()) throw new NotFoundException("Device not found or already deleted");
                return null;
            });
        }
    }

    private DeviceResponse mapDevice(Node d) {
        return new DeviceResponse(
                d.get("deviceId").asString(),
                d.get("deviceName").asString(),
                d.get("partNumber").asString(),
                d.get("buildingName").asString(),
                d.get("deviceType").asString(),
                (int) d.get("numberOfShelfPositions").asLong(),
                d.get("createdAt").asLong(),
                d.get("updatedAt").asLong()
        );
    }
}