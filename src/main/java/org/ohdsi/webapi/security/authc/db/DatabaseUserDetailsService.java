package org.ohdsi.webapi.security.authc.db;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import javax.sql.DataSource;

public class DatabaseUserDetailsService {

  private final JdbcTemplate jdbcTemplate;
  private final String authUserTable;

  public DatabaseUserDetailsService(DataSource ds, String schema) {
    this.jdbcTemplate = new JdbcTemplate(ds);
    this.authUserTable = qualifyTable("auth_user", schema);
  }

  /**
   * Returns a schema-qualified table name if schema is provided,
   * otherwise returns the unqualified table name.
   */
  private static String qualifyTable(String table, String schema) {
    if (schema != null && !schema.isBlank()) {
      return schema + "." + table;
    }
    return table;
  }

  public DatabaseUser loadUserByLogin(String login) {
    try {
      return jdbcTemplate.queryForObject(
          "SELECT login, first_name, middle_name, last_name, password_hash, enabled, failed_attempts, locked_until FROM " + authUserTable + " WHERE login = ?",
          (rs, rowNum) -> mapRow(rs),
          login);
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  private DatabaseUser mapRow(ResultSet rs) throws SQLException {
    return new DatabaseUser(
        rs.getString("login"),
        rs.getString("password_hash"),
        rs.getString("first_name"),
        rs.getString("middle_name"),
        rs.getString("last_name"),
        rs.getBoolean("enabled"),
        rs.getInt("failed_attempts"),
        rs.getTimestamp("locked_until") != null ? rs.getTimestamp("locked_until").toLocalDateTime() : null);
  }

  public void incrementFailedAttempts(String login) {
    jdbcTemplate.update(
        "UPDATE " + authUserTable + " SET failed_attempts = failed_attempts + 1 WHERE login = ?",
        login);
  }

  public void resetFailedAttempts(String login) {
    jdbcTemplate.update(
        "UPDATE " + authUserTable + " SET failed_attempts = 0, locked_until = NULL WHERE login = ?",
        login);
  }

  public void lockUser(String login, LocalDateTime until) {
    jdbcTemplate.update(
        "UPDATE " + authUserTable + " SET locked_until = ? WHERE login = ?",
        java.sql.Timestamp.valueOf(until),
        login);
  }
}
