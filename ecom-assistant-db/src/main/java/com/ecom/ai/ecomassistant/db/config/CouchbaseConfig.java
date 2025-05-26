package com.ecom.ai.ecomassistant.db.config;

import com.couchbase.client.core.error.UnambiguousTimeoutException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.env.ClusterEnvironment;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.auditing.EnableCouchbaseAuditing;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

import java.time.Duration;

@Getter
@Setter
@Slf4j
@Configuration
@EnableCouchbaseAuditing
@EnableCouchbaseRepositories
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

    @Value("${spring.couchbase.connection-string}")
    private String connectionString;

    @Value("${spring.couchbase.username}")
    private String userName;

    @Value("${spring.couchbase.password}")
    private String password;

    @Value("${spring.data.couchbase.bucket-name}")
    private String bucketName;

    @Value("${spring.data.couchbase.scope-name}")
    private String scopeName;

    @Override
    @Bean(destroyMethod = "disconnect")
    public Cluster couchbaseCluster(ClusterEnvironment couchbaseClusterEnvironment) {
        try {
            log.info("Connecting to Couchbase cluster at {}", connectionString);
            Cluster cluster = Cluster.connect(connectionString, userName, password);
            cluster.waitUntilReady(Duration.ofSeconds(15));
            return cluster;
        } catch (UnambiguousTimeoutException e) {
            log.error("Connection to Couchbase cluster at {} timed out", connectionString);
            throw e;
        } catch (Exception e) {
            log.error(e.getClass().getName());
            log.error("Could not connect to Couchbase cluster at {}", connectionString);
            throw e;
        }
    }

}
