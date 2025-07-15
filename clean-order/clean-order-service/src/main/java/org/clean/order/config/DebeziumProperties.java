package org.clean.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "debezium")
public class DebeziumProperties {
    private String connectorName = "springboot-embedded-debezium";
    private String host = "localhost";
    private int port = 5432;
    private String user = "postgres";
    private String password = "123456";
    private String database = "postgres";
    private String serverName = "dbserver1";
    private String offsetFile = "./debezium/offset.dat";
    private String historyFile = "./debezium/history.dat";
    private String tableIncludeList = "public.local_category,public.test";
    private String slotName = "debezium_slot";
    private String publicationName = "dbz_publication";
    private String snapshotMode = "initial";
    private String topicPrefix;
}