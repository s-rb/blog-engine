package main;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class SharedDatabaseContainer {

    // It's safe to store username and password here. They used only in tests
    @Container
    public static final PostgreSQLContainer<?> dbContainer = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("blogdb")
            .withUsername("testuser")
            .withPassword("testpassword");

    static {
        dbContainer.start();
    }
}