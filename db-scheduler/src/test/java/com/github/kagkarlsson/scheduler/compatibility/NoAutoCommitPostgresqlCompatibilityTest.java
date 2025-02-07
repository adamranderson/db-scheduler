package com.github.kagkarlsson.scheduler.compatibility;

import com.github.kagkarlsson.scheduler.DbUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.DriverDataSource;
import java.util.Properties;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("compatibility")
@Testcontainers
public class NoAutoCommitPostgresqlCompatibilityTest extends CompatibilityTest {

  @Container private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer();
  private static HikariDataSource pooledDatasource;

  public NoAutoCommitPostgresqlCompatibilityTest() {
    super(true);
  }

  @BeforeAll
  static void initSchema() {
    final DriverDataSource datasource =
        new DriverDataSource(
            POSTGRES.getJdbcUrl(),
            "org.postgresql.Driver",
            new Properties(),
            POSTGRES.getUsername(),
            POSTGRES.getPassword());

    // init schema
    DbUtils.runSqlResource("/postgresql_tables.sql").accept(datasource);

    // Setup non auto-committing datasource
    final HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDataSource(datasource);
    hikariConfig.setAutoCommit(false);
    pooledDatasource = new HikariDataSource(hikariConfig);
  }

  @Override
  public DataSource getDataSource() {
    return pooledDatasource;
  }

  @Override
  public boolean commitWhenAutocommitDisabled() {
    return true;
  }
}
