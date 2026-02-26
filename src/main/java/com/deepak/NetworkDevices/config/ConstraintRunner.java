package com.deepak.NetworkDevices.config;

import org.neo4j.driver.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ConstraintRunner implements CommandLineRunner {

    private final Driver driver;

    public ConstraintRunner(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void run(String... args) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("""
            CREATE CONSTRAINT device_id_unique IF NOT EXISTS
            FOR (d:Device) REQUIRE d.deviceId IS UNIQUE
            """);
                tx.run("""
                    CREATE CONSTRAINT uniqueDevices IF NOT EXISTS
                    FOR (d:Device) REQUIRE (d.deviceName,d.deviceType,d.partNumber,d.buildingName,d.numberOfShelfPositions) IS UNIQUE
                    """);
                tx.run("""
            CREATE CONSTRAINT shelf_id_unique IF NOT EXISTS
            FOR (s:Shelf) REQUIRE s.shelfId IS UNIQUE
            """);
                tx.run("""
                        CREATE CONSTRAINT uniqueShelf IF NOT EXISTS
                        FOR (s:Shelf) REQUIRE (s.shelfName,s.partName) IS UNIQUE
                        """);
                tx.run("""
            CREATE CONSTRAINT shelfpos_id_unique IF NOT EXISTS
            FOR (sp:ShelfPosition) REQUIRE sp.shelfPositionId IS UNIQUE
            """);

                tx.run("""
            CREATE INDEX device_name_idx IF NOT EXISTS FOR (d:Device) ON (d.deviceName)
            """);
                tx.run("""
            CREATE INDEX shelf_name_idx IF NOT EXISTS FOR (s:Shelf) ON (s.shelfName)
            """);
                return null;
            });
        }
    }
}
