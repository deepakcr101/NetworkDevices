package com.deepak.NetworkDevices.config;

import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Neo4jConfig {


    @Bean
    public Driver driver(
            @Value("${neo4j.uri}") String uri,
            @Value("${neo4j.username}") String user,
            @Value("${neo4j.password}") String pwd) {
        return GraphDatabase.driver(uri, AuthTokens.basic(user, pwd),
                Config.builder().withMaxConnectionPoolSize(50).build());
    }

//    @Bean
//    public Clock clock() {
//        return Clock.systemUTC();
//    }
}
