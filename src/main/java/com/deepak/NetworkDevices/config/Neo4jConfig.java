package com.deepak.NetworkDevices.config;

import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class Neo4jConfig {

    /* As Spring Boot will auto configure it
     Boot will provide it.
    @Bean
    public Driver neo4jDriver(
            @Value("${neo4j.uri}") String uri,
            @Value("${neo4j.username}") String username,
            @Value("${neo4j.password}") String password) {
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }*/

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
