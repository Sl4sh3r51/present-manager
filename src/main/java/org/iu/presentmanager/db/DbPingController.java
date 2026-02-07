package org.iu.presentmanager.db;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/db")
public class DbPingController {
  private final JdbcTemplate jdbc;
  private final Environment env;

  public DbPingController(JdbcTemplate jdbc, Environment env) {
    this.jdbc = jdbc;
    this.env = env;
  }

  @GetMapping("/ping")
  public ResponseEntity<Map<String, Object>> ping() {
    try {
      Integer one = jdbc.queryForObject("select 1", Integer.class);
      String version = jdbc.queryForObject("select version()", String.class);
      return ResponseEntity.ok(
          Map.of(
              "ok", one != null && one == 1,
              "db", "supabase-postgres",
              "version", version));
    } catch (Exception e) {
      Throwable root = rootCause(e);
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("ok", false);
      body.put("error", root.getMessage());
      body.put("errorType", root.getClass().getName());
      body.put("configuredUrl", env.getProperty("spring.datasource.url"));
      body.put("configuredUsername", env.getProperty("spring.datasource.username"));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
  }

  private static Throwable rootCause(Throwable t) {
    Throwable cur = t;
    while (cur.getCause() != null && cur.getCause() != cur) {
      cur = cur.getCause();
    }
    return cur;
  }
}

