package pl.mnekos.multicurrencies.data.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.function.Consumer;

public class MySQL {

    private HikariDataSource source;

    public MySQL(HikariConfig config) {
        this.source = new HikariDataSource(config);
    }

    public void executeUpdate(String update, Object... objects) throws SQLException {
        try(Connection connection = source.getConnection()) {
            try(PreparedStatement ps = connection.prepareStatement(update)) {
                if (countMatches(update, '?') == objects.length) {
                    for (int i = 0; i < objects.length; i++) {
                        ps.setObject(i + 1, objects[i]);
                    }
                    ps.execute();
                } else throw new IllegalArgumentException("Variable count in the query is not equal to the given parameters");
            }
        }
    }

    public void query(String query, Consumer<ResultSet> consumer, Object... objects) throws SQLException {
        try(Connection connection = source.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                if(countMatches(query, '?') == objects.length) {
                    for (int i = 0; i < objects.length; i++) {
                        ps.setObject(i + 1, objects[i]);
                    }
                    try (ResultSet result = ps.executeQuery()) {
                        consumer.accept(result);
                    }
                } else throw new IllegalArgumentException("Variable count in the query is not equal to the given parameters");
            }
        }
    }

    @Override
    public void finalize() {
        if(!source.isClosed()) source.close();
    }

    private int countMatches(String string, char c) {
        int i = 0;

        for(char character : string.toCharArray()) {
            if(character == c) {
                i++;
            }
        }

        return i;
    }
}