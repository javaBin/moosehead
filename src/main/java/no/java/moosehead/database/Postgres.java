package no.java.moosehead.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.java.moosehead.web.Configuration;
import org.postgresql.ds.PGPoolingDataSource;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Postgres {
    private Postgres() {
    }

    private static volatile DataSource source;

    public static DataSource source() {
        if (source != null) {
            return source;
        }
        return createSource();
    }

    private synchronized static DataSource createSource() {
        if (source != null) {
            return source;
        }

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl("jdbc:postgresql://" + Configuration.dbServer() + ":" + Configuration.dbPort() + "/" + Configuration.dbName());
        hikariConfig.setUsername(Configuration.dbUser());
        hikariConfig.setPassword(Configuration.dbPassword());
        hikariConfig.setMaximumPoolSize(10);
        source = new HikariDataSource(hikariConfig);
        return source;
    }

    public static Connection openConnection() {
        try {
            return source().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
